package py.edu.fiuni.dmop;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import jmetal.operators.selection.BestSolutionSelection;
import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.util.TypedProperties;

import py.edu.fiuni.dmop.algorithm.DynamicAlgorithmsProvider;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.dto.RoundBestSolution;
import py.edu.fiuni.dmop.dto.RoundData;
import py.edu.fiuni.dmop.dto.WindowsData;
import py.edu.fiuni.dmop.problem.DynamicVNFPlacementProblem;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.GraphPlottingService;
import py.edu.fiuni.dmop.service.MCDMService;
import py.edu.fiuni.dmop.service.SolutionService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.service.VnfService;
import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;
import py.edu.fiuni.dmop.util.Utility;

/**
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class ManualDynamicPlacement {

    private static final Logger logger = Logger.getLogger(ManualDynamicPlacement.class);

    private static final int POPULATION_SIZE = 100;

    private static final int NUMBER_OF_WINDOWS = 12;
    private static final int LOWER_LIMIT = 10;
    private static final int UPPER_LIMIT = 130;
    private static final int NORMAL_UPPER_LIMIT = 75;
    private static final boolean RANDOMIZE_TRAFFICS = false;

    private static final int MAX_ROUNDS = 10;
    private static final int MAX_WINDOWS = 10;

    // Solution fileName format is::   solution_{ALGORITHM}_r{RoundNumber}_w{WindowsNumber}.dat
    private static final String SOLUTION_FILENAME_TEMPLATE = "solution_%s_r%d_w%d.dat";

    public static void main(String args[]) {

        try {
            AlgorithmFactory.getInstance().addProvider(new DynamicAlgorithmsProvider());

            Configurations.loadProperties();
            DataService.loadData();

            // 
            //runSolver();
            // Create files to be used by Charts generator
            generateCSVFiles();

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    private static void generateCSVFiles() {

        try {
            SolutionService solutionService = new SolutionService();
            MCDMService decisionService = new MCDMService();

            String[] algorithms = {"DNSGAII-A", "DNSGAII-B"};

            Map<String, Map<Integer, List<RoundData>>> algorithmsSolutionsMap = new HashMap<>();

            for (String alg : algorithms) {
                algorithmsSolutionsMap.put(alg, readSolutionsForAlgorithm(solutionService, alg));
            }

            logger.info("############ Calculte the best solution in every case ############");

            // Create a map to hold the best solution for every round and windows
            // It is added to List of best solutions becuase they are already in order of Round and Windows
            Map<String, List<RoundBestSolution>> algorithmsBestSolutionsMap = new HashMap<>();
            
            // Key: windows number
            // Value: List of the best-solution per round
            Map<Integer, List<RoundBestSolution>> bestResultsMapByWindows = new HashMap<>();

            for (String alg : algorithmsSolutionsMap.keySet()) {

                // add the collection for the specified algorithm
                algorithmsBestSolutionsMap.put(alg, new ArrayList<>());

                Map<Integer, List<RoundData>> roundsBestByWindows = algorithmsSolutionsMap.get(alg);
                for (int wnd : roundsBestByWindows.keySet()) {
                    
                    if(!bestResultsMapByWindows.containsKey(wnd)) bestResultsMapByWindows.put(wnd, new ArrayList<>());
                    
                    List<RoundData> roundDataList = roundsBestByWindows.get(wnd);
                    
                    for (RoundData data : roundDataList) {

                        NetworkConditionEnum networkCondition = getNetConditionForWindows(wnd);

                        // Decision Making
                        Solution bestSolution = decisionService.calculateOptimalSolution(data.getSolutions(), networkCondition);
                        
                        RoundBestSolution sol = new RoundBestSolution(data.getAlgorithm(), data.getRoundNumber(), wnd, bestSolution, networkCondition);
                        
                        algorithmsBestSolutionsMap.get(alg).add(sol);
                        
                        bestResultsMapByWindows.get(wnd).add(sol);
                    }
                }
            }

            
            //#####################################################################
            logger.info("############  GENERATING CSV FILES::: Algorithm-OF  ####################");

            List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(NetworkConditionEnum.Normal);

            for (String alg : algorithmsBestSolutionsMap.keySet()) {
                System.out.println("Solutions for Algorithm " + alg);

                List<RoundBestSolution> bestSolutionsList = algorithmsBestSolutionsMap.get(alg);

                Map<String, List<double[]>> toFile = new HashMap<>();
                for (int round = 1; round <= MAX_ROUNDS; round++) {

                    final int roundNumber = round;
                    // conjunto de mejores soluciones del round actual
                    List<RoundBestSolution> roundBestSolutions = bestSolutionsList.stream().filter(rbs -> roundNumber == rbs.getRoundNumber()).toList();

                    // Order the solutions by windows number (asc order)
                    //roundBestSolutions.sort((o1, o2) -> o1.getWindows() - o2.getWindows());
                    System.out.println("Best for the Round #" + round);
                    for (RoundBestSolution best : roundBestSolutions) {
                        Solution bestSol = best.getSolution();
                        System.out.println(String.format("Windows #%3d  Solution Fitness::: %s", best.getWindows(), Arrays.toString(bestSol.getObjectives())));
                    }

                    for (int index = 0; index < sceneObjectives.size(); index++) {
                        double[] values = extractValuesFor(index, roundBestSolutions, MAX_WINDOWS);

                        String key = String.format("%s_-_%s", alg, sceneObjectives.get(index).getName());

                        if (!toFile.containsKey(key)) {
                            toFile.put(key, new ArrayList<>());
                        }
                        toFile.get(key).add(values);

                        System.out.println(" *** " + Arrays.toString(values));
                    }
                }

                for (String key : toFile.keySet()) {
                    List<double[]> values = toFile.get(key);
                    System.out.println("archivo " + key);
                    values.forEach(objs -> System.out.println(Arrays.toString(objs)));

                    createCSVFile(key, values);
                }
            }
            
            
            
            //#####################################################################
            logger.info("############  GENERATING CSV FILES::: Windows#-OF  ####################");

            //List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(NetworkConditionEnum.Normal);

            for (int windows : bestResultsMapByWindows.keySet()) {                

                List<RoundBestSolution> currWndBestSolutionsList = bestResultsMapByWindows.get(windows);
                
                NetworkConditionEnum networkCondition = getNetConditionForWindows(windows);
                List<ObjectiveFunctionEnum> currWindowsObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

                Map<String, List<double[]>> toFile = new HashMap<>();
                
                for (int round = 1; round <= MAX_ROUNDS; round++) {

                    final int roundNumber = round;
                    
                    // conjunto de mejores soluciones del round actual
                    List<RoundBestSolution> roundBestSolutions = currWndBestSolutionsList.stream().filter(rbs -> roundNumber == rbs.getRoundNumber()).toList();

                    
                    for (int index = 0; index < sceneObjectives.size(); index++) {
                        double[] values = extractAlgorithmsValuesFor(index, roundBestSolutions, algorithms);

                        String key = String.format("Ventana_#%d_-_%s", windows, sceneObjectives.get(index).getName());

                        if (!toFile.containsKey(key)) { toFile.put(key, new ArrayList<>()); }
                        toFile.get(key).add(values);

                        System.out.println(" *** " + Arrays.toString(values));
                    }
                }

                for (String key : toFile.keySet()) {
                    List<double[]> values = toFile.get(key);
                    System.out.println("archivo " + key);
                    values.forEach(objs -> System.out.println(Arrays.toString(objs)));

                    createCompareAlgsCSVFile(key, values, algorithms);
                }
            }

            //Permutation bestVar1 = (Permutation) bestSolution1.getVariable(0);
            //System.out.println("Permutation Variable 1: " + Arrays.toString(bestVar1.toArray()) + " Fitness:: " + Arrays.toString(bestSolution1.getObjectives()));
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    /**
     * 
     * @param solutionService
     * @param algorithm
     * @return
     * @throws IOException 
     */
    private static Map<Integer, List<RoundData>> readSolutionsForAlgorithm(SolutionService solutionService, String algorithm) throws IOException {
        
        // key: windows number
        // value: List of best-solution of every round
        Map<Integer, List<RoundData>> resultsMap = new HashMap<>();
        for (int round = 1; round <= MAX_ROUNDS; round++) {
            for (int windows = 0; windows < MAX_WINDOWS; windows++) {
                String fileName = String.format(SOLUTION_FILENAME_TEMPLATE, algorithm, round, windows);
                NondominatedPopulation result = solutionService.readSolutions(fileName);

                if (!resultsMap.containsKey(windows)) {
                    resultsMap.put(windows, new ArrayList<>());
                }

                resultsMap.get(windows).add(new RoundData(algorithm, round, result));
            }
        }
        return resultsMap;
    }

    private static double[] extractValuesFor(int ofIndex, List<RoundBestSolution> solutions, int maxWindows) {
        double[] values = DoubleStream.generate(() -> Double.MAX_VALUE).limit(maxWindows).toArray();
        //List<Double> result = new ArrayList<>();
        int win = 0;
        for (RoundBestSolution best : solutions) {
            double[] objectives = best.getSolution().getObjectives();
            if (ofIndex < objectives.length) {
                //result.add(objectives[ofIndex]);
                values[win] = objectives[ofIndex];
            }
            win++;
        }
        return values; //result.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    private static double[] extractAlgorithmsValuesFor(int ofIndex, List<RoundBestSolution> solutions, String [] algorithms) {
        
        double[] values = DoubleStream.generate(() -> Double.MAX_VALUE).limit(algorithms.length).toArray();
        
        int cont = 0;
        for(String algorithm : algorithms){
            Optional<RoundBestSolution> sol = solutions.stream().filter(s -> s.getAlgorithm().equals(algorithm)).findFirst();
            if(sol.isPresent()){
               double[] objectives = sol.get().getSolution().getObjectives();
               values[cont] = objectives[ofIndex];
            }
            cont++;
        }
        
        return values;
    }    

    private static void createCSVFile(String fileName, List<double[]> rows) {

        try (PrintWriter writer = new PrintWriter(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/CSVs/" + fileName + ".csv")))) {

            // Get the first row values in order to generate the header correctly
            double[] roundValues = rows.get(0);

            List<String> header = new ArrayList<>();
            for (int i = 0; i < roundValues.length; i++) {
                if (roundValues[i] < Double.MAX_VALUE) {
                    header.add("w" + (i + 1));
                }
            }
            writer.println(header.stream().collect(Collectors.joining(",")));

            for (double[] row : rows) {
                String valuesAsString = Arrays.stream(row).filter(d -> d < Double.MAX_VALUE).mapToObj(String::valueOf).collect(Collectors.joining(","));
                writer.println(valuesAsString);
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }
    
    private static void createCompareAlgsCSVFile(String fileName, List<double[]> rows, String[] algorithms) {

        try (PrintWriter writer = new PrintWriter(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/CSVs/" + fileName + ".csv")))) {
            
            writer.println(Arrays.stream(algorithms).collect(Collectors.joining(",")));

            for (double[] row : rows) {
                String valuesAsString = Arrays.stream(row).filter(d -> d < Double.MAX_VALUE).mapToObj(String::valueOf).collect(Collectors.joining(","));
                writer.println(valuesAsString);
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    private static NetworkConditionEnum getNetConditionForWindows(int winNumber) {
        int[] windowsTrafficsNumber = {30, 82, 44, 109, 77, 56, 33, 89, 125, 41, 73, 90};

        int winTrafficsCount = windowsTrafficsNumber[winNumber];

        return winTrafficsCount > NORMAL_UPPER_LIMIT ? NetworkConditionEnum.Overloaded : NetworkConditionEnum.Normal;
    }

    
    
    
    
    
    /**
     *
     */
    private static void runSolver() {

        try {
            logger.debug("Starting Manual Dynamic VNF Placement process");

            long inicio = System.currentTimeMillis();

            SolutionService solutionsService = new SolutionService();
            TrafficService trafficService = new TrafficService();

            String algorithmName = "DNSGAII-B";

            int maxEvaluations = 50000;      // 10 VENTANAS DE 2 C/U - 2000

            Properties properties = new Properties();
            properties.put("populationSize", POPULATION_SIZE);
            properties.put("frequencyOfChange", Configurations.frequencyOfChange);
            properties.put("severityOfChange", Configurations.severityOfChange);
            properties.put("zeta", 0.2d);

            List<List<Traffic>> windowsTraffics = trafficService.readAllTraffics(NUMBER_OF_WINDOWS);

            //List<NondominatedPopulation> results = new ArrayList<>();
            Map<Integer, WindowsData> resultsMap = new HashMap<>();

            //
            runSeeds(algorithmName, MAX_ROUNDS, maxEvaluations, properties, trafficService, windowsTraffics, resultsMap);

            //List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);
            for (int wnd : resultsMap.keySet()) {
                System.out.println("WINDOWS ###: " + wnd);
                WindowsData winData = resultsMap.get(wnd);

                List<RoundData> theWindResults = winData.getResults();

                // Save the solutions into a file
                for (RoundData data : theWindResults) {
                    String fileName = String.format(SOLUTION_FILENAME_TEMPLATE, algorithmName, data.getRoundNumber(), wnd);
                    solutionsService.writeSolutions(data.getSolutions(), fileName);
                }

                List<NondominatedPopulation> solutionsList = theWindResults.stream().map(rd -> rd.getSolutions()).collect(Collectors.toList());

                printSolutions(solutionsList);

                runAnalysis(winData, solutionsList, algorithmName);

                //selectAndDeploySolution(theWindResults.get(0),  );
            }

        } catch (Exception ex) {
            logger.fatal("ERROR", ex);
        }
    }

    /**
     *
     * @param algorithmName
     * @param seed
     * @param maxEvaluations
     * @param properties
     * @param trafficService
     * @param windowsTraffics
     * @param resultsMap
     */
    private static void runSeeds(String algorithmName, int seed, int maxEvaluations, Properties properties, TrafficService trafficService, List<List<Traffic>> windowsTraffics, Map<Integer, WindowsData> resultsMap) {

        //TypedProperties typedProperties = new TypedProperties(properties);
        int frequencyOfChange = (Integer) properties.get("frequencyOfChange");// typedProperties.getDouble("frequencyOfChange", 10);

        int currentRun = 1;
        while (currentRun <= seed) {
            System.out.println("############ ROUND " + currentRun + " ################");

            //
            //int lower = (int) properties.getDouble("populationSize", POPULATION_SIZE);
            DynamicVNFPlacementProblem problem = new DynamicVNFPlacementProblem(properties, trafficService, windowsTraffics, LOWER_LIMIT, UPPER_LIMIT, NORMAL_UPPER_LIMIT, RANDOMIZE_TRAFFICS);

            Algorithm algorithm = AlgorithmFactory.getInstance().getAlgorithm(algorithmName, properties, problem);

            int counter = 0;
            //int windows = 0;

            // run the algorithm for maxEvaluations evaluations
            while (algorithm.getNumberOfEvaluations() < maxEvaluations) {

                if (++counter > frequencyOfChange) {

                    // Retrieve all the data for the current run and windows and add them to the map
                    //windows = problem.getCurrentWindows();
                    addSolutionsToMap(algorithmName, resultsMap, algorithm.getResult(), currentRun, problem.getCurrentWindows(), problem);

                    // reset the counter
                    counter = 1;
                    problem.changeEnvironment();
                }

                //
                algorithm.step();
            }

            //// Last incompleted windows result and data added to the map
            //windows = problem.getCurrentWindows();
            addSolutionsToMap(algorithmName, resultsMap, algorithm.getResult(), currentRun, problem.getCurrentWindows(), problem);

            currentRun++;
        }
    }

    private static void addSolutionsToMap(String algorithm, Map<Integer, WindowsData> resultsMap, NondominatedPopulation result, int roundNumber, int windowsNumber, DynamicVNFPlacementProblem problem) {

        // Create a new WindowsData for the given windowsNumber if not present yet
        if (!resultsMap.containsKey(windowsNumber)) {
            Gson gson = new Gson();
            //Deep copy of the problem instance
            DynamicVNFPlacementProblem problemCopy = gson.fromJson(gson.toJson(problem), DynamicVNFPlacementProblem.class);
            resultsMap.put(windowsNumber, new WindowsData(problemCopy, windowsNumber, problem.getCurrentTraffics(), problem.getNetworkCondition()));
        }

        resultsMap.get(windowsNumber).addRoundResult(algorithm, roundNumber, result);
    }

    private static void printSolutions(List<NondominatedPopulation> results) {
        int seed = 1;
        for (NondominatedPopulation roundSolutions : results) {
            System.out.println("Round ###: " + seed++);
            int contSol = 1;
            for (Solution s : roundSolutions) {
                //String fitness = DoubleStream.of(s.getObjectives()).map(val  -> String.valueOf(val))
                System.out.println(String.format("Solution #%3d Fitness::: %s Variable:: %s", contSol++, Arrays.toString(s.getObjectives()), Arrays.toString(((Permutation) s.getVariable(0)).toArray())));
            }
        }
        /*
            int index = 0;
            for (Solution sol : result) {
                System.out.print("Solution #" + index++ + " = ");
                for (int objIndex = 0; objIndex < sol.getNumberOfObjectives(); objIndex++) {
                    System.out.printf("%s=%6f ,", sceneObjectives.get(objIndex).getPropertyName(), sol.getObjective(objIndex));
                }

                // Shows the permutation used in the current solution.
                System.out.println("Variable: " + Arrays.toString(((Permutation) sol.getVariable(0)).toArray()));
            }
         */
    }

    private static void selectAndDeploySolution(NondominatedPopulation solutions, List<Traffic> traffics, NetworkConditionEnum networkCondition, boolean plotResult) {
        try {
            // Decision Making
            MCDMService decisionService = new MCDMService();
            Solution bestSolution = decisionService.calculateOptimalSolution(solutions, networkCondition);

            Permutation bestSolutionVariable = (Permutation) bestSolution.getVariable(0);
            System.out.println("Permutation Variable: " + Arrays.toString(bestSolutionVariable.toArray()));

            // Implement the best solution
            VnfService vnfService = new VnfService();
            ResultGraphMap resultGraph = vnfService.placementGraph(traffics, bestSolutionVariable);

            if (plotResult) {
                //Plot the graph after implementing the best solution for the given traffic list.
                GraphPlottingService plotService = new GraphPlottingService();
                plotService.plotGraph(resultGraph);
            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    private static void runAnalysis(WindowsData winData, List<NondominatedPopulation> results, String algorithmName) {

        try {
            // Results added to analyzer only if all of NondominatedPopulation have more than one solution.
            if (results.stream().allMatch(ndp -> ndp.size() > 1)) {

                Analyzer analyzer = new Analyzer()
                        .withProblem(winData.getProblem())
                        .includeAllMetrics()
                        .showAll()
                        //.includeHypervolume()
                        //.includeAdditiveEpsilonIndicator()
                        //.includeGenerationalDistance()
                        //.includeInvertedGenerationalDistance()
                        //.includeMaximumParetoFrontError()                    
                        //.includeSpacing()
                        //.includeR1()
                        //.includeR2()
                        //.includeR3()
                        .showStatisticalSignificance();

                analyzer.addAll(algorithmName, results);

                // Print the Analyzer results
                logger.info("Starting Analysis for windows #" + winData.getWindowsNumber());
                long start = System.currentTimeMillis();
                analyzer.printAnalysis(new PrintStream(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/analyzer_results_" + algorithmName + "_win" + winData.getWindowsNumber() + ".txt"))));
                analyzer.saveAs(algorithmName, new File(Utility.buildFilePath(Configurations.solutionsFolder + "/analyzer_data_" + algorithmName + "_win" + winData.getWindowsNumber() + ".txt")));

                analyzer.printAnalysis();
                long end = System.currentTimeMillis();
                logger.info("Analysis Completed " + Utility.getTime(end - start));

                // Plot the Analyzer results
                new Plot().add(analyzer).setTitle("Windows #" + winData.getWindowsNumber()).show();

            } else {
                System.out.println("Windows #" + winData.getWindowsNumber() + " results don't added to Analyzer, because not all of them have more than one solution");
            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }
}
