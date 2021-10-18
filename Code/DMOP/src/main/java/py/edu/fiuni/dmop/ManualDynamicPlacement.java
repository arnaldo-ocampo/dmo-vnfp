package py.edu.fiuni.dmop;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.util.TypedProperties;
import py.edu.fiuni.dmop.algorithm.DNSGAIIA;
import py.edu.fiuni.dmop.algorithm.Detection;
import py.edu.fiuni.dmop.algorithm.NetStatusDetection;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.problem.DynamicVNFPlacementProblem;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.service.DMOPService;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.GraphPlottingService;
import py.edu.fiuni.dmop.service.MCDMService;
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

    private static final int NUMBER_OF_WINDOWS = 10;
    private static final int LOWER_LIMIT = 10;
    private static final int UPPER_LIMIT = 100;
    private static final int NORMAL_UPPER_LIMIT = 50;
    private static final boolean RANDOMIZE_TRAFFICS = false;

    public static void main(String args[]) {

        logger.debug("Starting Manual Dynamic VNF Placement process");

        try {
            long inicio = System.currentTimeMillis();

            Configurations.loadProperties();
            DataService.loadData();

            int rounds = 3;
            int maxEvaluations = 550;

            TrafficService trafficService = new TrafficService();
            //List<Traffic> traffics = trafficService.readTraffics();

            Properties properties = new Properties();
            properties.put("frequencyOfChange", Configurations.frequencyOfChange);
            properties.put("severityOfChange", Configurations.severityOfChange);

            //TypedProperties typedProperties = new TypedProperties(properties);
            List<List<Traffic>> windowsTraffics = trafficService.readAllTraffics(NUMBER_OF_WINDOWS);

            //List<NondominatedPopulation> results = new ArrayList<>();
            Map<Integer, List<DTO>> resultsMap = new HashMap<Integer, List<DTO>>();

            //
            runSeeds(rounds, maxEvaluations, properties, trafficService, windowsTraffics, resultsMap);

            //List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);
            for (int wnd : resultsMap.keySet()) {
                System.out.println("WINDOWS ###: " + wnd);
                List<DTO> dtoList = resultsMap.get(wnd);
                List<NondominatedPopulation> theWindResults = dtoList.stream().map(dto -> dto.getResult()).collect(Collectors.toList());

                printSolutions(theWindResults);

                runAnalysis(theWindResults, dtoList.get(0).getProblem());

                //selectAndDeploySolution(theWindResults.get(0),  );
            }

        } catch (Exception ex) {
            logger.fatal("ERROR", ex);
        }
    }

    private static void printSolutions(List<NondominatedPopulation> results) {
        int seed = 1;
        for (NondominatedPopulation roundSolutions : results) {
            System.out.println("Round ###: " + seed++);
            int contSol = 1;
            for (Solution s : roundSolutions) {
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

    private static void selectAndDeploySolution(NondominatedPopulation solutions, List<Traffic> traffics, NetworkConditionEnum networkCondition, boolean plotResult) throws DecisionMakerException {
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
    }

    private static void runSeeds(int seed, int maxEvaluations, Properties properties, TrafficService trafficService, List<List<Traffic>> windowsTraffics, Map<Integer, List<DTO>> resultsMap) {
        int currentRun = 1;
        while (currentRun <= seed) {
            //TypedProperties typedProperties = new TypedProperties(properties);
            //int lower = (int) properties.getDouble("populationSize", POPULATION_SIZE);
            DynamicVNFPlacementProblem problem = new DynamicVNFPlacementProblem(properties, trafficService, windowsTraffics, LOWER_LIMIT, UPPER_LIMIT, NORMAL_UPPER_LIMIT, RANDOMIZE_TRAFFICS);

            int populationSize = POPULATION_SIZE;  // (int) properties.getDouble("populationSize", POPULATION_SIZE);
            Initialization initialization = new RandomInitialization(problem, populationSize);
            NondominatedSortingPopulation population = new NondominatedSortingPopulation();
            TournamentSelection selection = null;
            //if (properties.getBoolean("withReplacement", true)) {
            selection = new TournamentSelection(2,
                    new ChainedComparator(new ParetoDominanceComparator(), new CrowdingComparator()));
            //}

            Variation variation = OperatorFactory.getInstance().getVariation(null, properties, problem);

            Detection detection = new NetStatusDetection();

            double zeta = 0.2d; // properties.getDouble("zeta", 0.2d);

            DNSGAIIA algorithm = new DNSGAIIA(problem, population, null, selection, variation, initialization, detection, zeta);

            int counter = 0;
            int windows = 0;

            // run the algorithm for maxEvaluations evaluations
            while (algorithm.getNumberOfEvaluations() < maxEvaluations) {

                if (++counter > Configurations.frequencyOfChange) {

                    windows = problem.getCurrentWindows();

                    // Add to the collections of results for the given windows, to be used in the analyzer
                    if (!resultsMap.containsKey(windows)) {
                        resultsMap.put(windows, new ArrayList<DTO>());
                    }

                    Gson gson = new Gson();

                    //User deepCopy = gson.fromJson(gson.toJson(pm), User.class);
                    DynamicVNFPlacementProblem problemCopy = gson.fromJson(gson.toJson(problem), DynamicVNFPlacementProblem.class);

                    resultsMap.get(windows).add(new DTO(windows, problemCopy, problem.getCurrentTraffics(), problem.getNetworkCondition(), algorithm.getResult()));

                    counter = 0;
                    problem.changeEnvironment();
                }

                //
                algorithm.step();
            }

            //// Last incompleted windows
            windows = problem.getCurrentWindows();

            //NetworkConditionEnum networkCondition = problem.getNetworkCondition();
            // get the Pareto approximate results
            //NondominatedPopulation result = algorithm.getResult();
            // Add to the collections of results for the given windows, to be used in the analyzer
            if (!resultsMap.containsKey(windows)) {
                resultsMap.put(windows, new ArrayList<DTO>());
            }

            Gson gson = new Gson();

            //User deepCopy = gson.fromJson(gson.toJson(pm), User.class);
            DynamicVNFPlacementProblem problemCopy = gson.fromJson(gson.toJson(problem), DynamicVNFPlacementProblem.class);

            resultsMap.get(windows).add(new DTO(windows, problemCopy, problem.getCurrentTraffics(), problem.getNetworkCondition(), algorithm.getResult()));//.add(result);

            currentRun++;
        }
    }

    private static void runAnalysis(List<NondominatedPopulation> results, DynamicVNFPlacementProblem problem) {

        Analyzer analyzer = new Analyzer()
                .withProblem(problem)
                .includeHypervolume()
                .includeAdditiveEpsilonIndicator()
                .includeGenerationalDistance()
                //.includeInvertedGenerationalDistance()
                //.includeMaximumParetoFrontError()                    
                //.includeSpacing()
                //.includeR1()
                //.includeR2()
                //.includeR3()
                .showStatisticalSignificance();

        analyzer.addAll("DNSGAII-A", results);

        // Print the Analyzer results
        logger.info("Starting Analysis");
        long start = System.currentTimeMillis();
        analyzer.printAnalysis();
        long end = System.currentTimeMillis();
        logger.info("Analysis Completed " + Utility.getTime(end - start));

        // Plot the Analyzer results
        new Plot().add(analyzer).show();

    }

    /*
            DynamicVNFPlacementProblem problem = new DynamicVNFPlacementProblem(properties, trafficService, windowsTraffics, LOWER_LIMIT, UPPER_LIMIT, NORMAL_UPPER_LIMIT, RANDOMIZE_TRAFFICS);

            int populationSize = POPULATION_SIZE;  // (int) properties.getDouble("populationSize", POPULATION_SIZE);
            Initialization initialization = new RandomInitialization(problem, populationSize);
            NondominatedSortingPopulation population = new NondominatedSortingPopulation();
            TournamentSelection selection = null;
            //if (properties.getBoolean("withReplacement", true)) {
            selection = new TournamentSelection(2,
                    new ChainedComparator(new ParetoDominanceComparator(), new CrowdingComparator()));
            //}

            Variation variation = OperatorFactory.getInstance().getVariation(null, properties, problem);

            Detection detection = new NetStatusDetection();

            double zeta = 0.2d; // properties.getDouble("zeta", 0.2d);

            DNSGAIIA algorithm = new DNSGAIIA(problem, population, null, selection, variation, initialization, detection, zeta);

            int counter = 0;
            int windows = 0;

            // run the algorithm for 10,000 evaluations
            while (algorithm.getNumberOfEvaluations() < 2500) {

                if (++counter > Configurations.frequencyOfChange) {
                    
                    windows = problem.getCurrentWindows();

                     // get the Pareto approximate results
                    NondominatedPopulation windowsResult = algorithm.getResult();

                    // Add to the collections of results for the given windows, to be used in the analyzer
                    if (!resultsMap.containsKey(windows)) {
                        resultsMap.put(windows, new ArrayList<NondominatedPopulation>());
                    }
                    resultsMap.get(windows).add(windowsResult);

                    //List<ObjectiveFunctionEnum> windowsObjectives = SceneObjectiveFunctions.SceneMap.get(problem.getNetworkCondition());
//
                    //int index = 0;
                    //for (Solution sol : windowsResult) {
                    //    System.out.print("Solution #" + index++ + " = ");
                    //    for (int objIndex = 0; objIndex < sol.getNumberOfObjectives(); objIndex++) {
                    //        System.out.printf("%s=%6f ,", windowsObjectives.get(objIndex).getPropertyName(), sol.getObjective(objIndex));
                    //    }
//
                    //    // Shows the permutation used in the current solution.
                    //    System.out.println("Variable: " + Arrays.toString(((Permutation) sol.getVariable(0)).toArray()));
                    }
                    
                    counter = 0;
                    problem.changeEnvironment();
                }
                
                // 
                algorithm.step();
            }

            //// Last incompleted windows
            windows = problem.getCurrentWindows();

            // get the Pareto approximate results
            NondominatedPopulation result = algorithm.getResult();
            NetworkConditionEnum networkCondition = problem.getNetworkCondition();
            List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

            // Add to the collections of results for the given windows, to be used in the analyzer
            if (!resultsMap.containsKey(windows)) {
                resultsMap.put(windows, new ArrayList<NondominatedPopulation>());
            }
            resultsMap.get(windows).add(result);
            
     */
}

@Data
class DTO {

    private int windows;
    private DynamicVNFPlacementProblem problem;
    private List<Traffic> traffics;
    private NetworkConditionEnum networkCondition;
    private NondominatedPopulation result;

    public DTO(int windows, DynamicVNFPlacementProblem problem, List<Traffic> traffics, NetworkConditionEnum condition, NondominatedPopulation result) {
        this.windows = windows;
        this.problem = problem;
        this.traffics = traffics;
        this.networkCondition = condition;
        this.result = result;
    }
}
