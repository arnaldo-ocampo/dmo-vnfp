package py.edu.fiuni.dmop.service;

import com.google.gson.Gson;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.core.variable.Permutation;

import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.algorithm.DynamicAlgorithmsProvider;
import py.edu.fiuni.dmop.dto.RoundData;
import py.edu.fiuni.dmop.dto.WindowsData;
import py.edu.fiuni.dmop.problem.DynamicVNFPlacementProblem;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.util.Constants;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;
import py.edu.fiuni.dmop.util.Utility;

/**
 * Dynamic Multi-Objective Placement Service
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class DMOPService {

    private static final Logger logger = Logger.getLogger(DMOPService.class);
    
    private SolutionService solutionsService = null;
    private TrafficService trafficService = null;

    /**
     * 
     * @param solService
     * @param trafficService
     * @throws Exception 
     */
    public DMOPService(SolutionService solService, TrafficService trafficService) throws Exception {

        AlgorithmFactory.getInstance().addProvider(new DynamicAlgorithmsProvider());

        Configurations.loadProperties();
        DataService.loadData();
        
        this.solutionsService = solService;
        this.trafficService = trafficService;
    }
    
    
    /**
     *
     */
    public void runSolver() {

        try {
            logger.debug("Starting Manual Dynamic VNF Placement process");

            long inicio = System.currentTimeMillis();

            String algorithmName = "DNSGAII-B";

            int maxEvaluations = 50000;      // 10 VENTANAS DE 2 C/U - 2000

            Properties properties = new Properties();
            //properties.put("populationSize", Constants.POPULATION_SIZE);
            properties.put("frequencyOfChange", Configurations.frequencyOfChange);
            properties.put("severityOfChange", Configurations.severityOfChange);
            properties.put("zeta", 0.2d);

            List<List<Traffic>> windowsTraffics = trafficService.readAllTraffics(Constants.NUMBER_OF_WINDOWS);

            //List<NondominatedPopulation> results = new ArrayList<>();
            Map<Integer, WindowsData> resultsMap = new HashMap<>();

            //
            runSeeds(algorithmName, Constants.MAX_ROUNDS, maxEvaluations, properties, trafficService, windowsTraffics, resultsMap);

            //List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);
            for (int wnd : resultsMap.keySet()) {
                logger.info("WINDOWS ###: " + wnd);
                WindowsData winData = resultsMap.get(wnd);

                List<RoundData> theWindResults = winData.getResults();

                // Save the solutions into a file
                for (RoundData data : theWindResults) {
                    String fileName = String.format(Constants.SOLUTION_FILENAME_TEMPLATE, algorithmName, data.getRoundNumber(), wnd);
                    solutionsService.writeSolutions(data.getSolutions(), fileName);
                }

                List<NondominatedPopulation> solutionsList = theWindResults.stream().map(rd -> rd.getSolutions()).collect(Collectors.toList());

                solutionsService.printSolutionsList(solutionsList);

                runAnalysis(winData, solutionsList, algorithmName);

                //selectAndDeploySolution(theWindResults.get(0),  );
            }

        } catch (Exception ex) {
            logger.fatal("ERROR: ", ex);
        }
    }

    /**
     *
     * @param algorithmName
     * @param maxRounds
     * @param maxEvaluations
     * @param properties
     * @param trafficService
     * @param windowsTraffics
     * @param resultsMap
     */
    private void runSeeds(String algorithmName, int maxRounds, int maxEvaluations, Properties properties, TrafficService trafficService, List<List<Traffic>> windowsTraffics, Map<Integer, WindowsData> resultsMap) {

        final int frequencyOfChange = (Integer) properties.get("frequencyOfChange");

        int currentRun = 1;
        while (currentRun <= maxRounds) {
            logger.info("############ ROUND " + currentRun + " ################");

            //
            //int lower = (int) properties.getDouble("populationSize", POPULATION_SIZE);
            DynamicVNFPlacementProblem problem = new DynamicVNFPlacementProblem(properties, trafficService, windowsTraffics, Constants.LOWER_LIMIT, Constants.UPPER_LIMIT, Constants.NORMAL_UPPER_LIMIT, Constants.RANDOMIZE_TRAFFICS);

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

    private void addSolutionsToMap(String algorithm, Map<Integer, WindowsData> resultsMap, NondominatedPopulation result, int roundNumber, int windowsNumber, DynamicVNFPlacementProblem problem) {

        // Create a new WindowsData for the given windowsNumber if not present yet
        if (!resultsMap.containsKey(windowsNumber)) {
            Gson gson = new Gson();
            //Deep copy of the problem instance
            DynamicVNFPlacementProblem problemCopy = gson.fromJson(gson.toJson(problem), DynamicVNFPlacementProblem.class);
            resultsMap.put(windowsNumber, new WindowsData(problemCopy, windowsNumber, problem.getCurrentTraffics(), problem.getNetworkCondition()));
        }

        resultsMap.get(windowsNumber).addRoundResult(algorithm, roundNumber, result);
    }
    

    private void selectAndDeploySolution(NondominatedPopulation solutions, List<Traffic> traffics, NetworkConditionEnum networkCondition, boolean plotResult) {
        try {
            // Decision Making
            MCDMService decisionService = new MCDMService();
            Solution bestSolution = decisionService.calculateOptimalSolution(solutions, networkCondition);

            Permutation bestSolutionVariable = (Permutation) bestSolution.getVariable(0);
            logger.info("Permutation Variable: " + Arrays.toString(bestSolutionVariable.toArray()));

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
            logger.fatal("Error: ", ex);
        }
    }
}
