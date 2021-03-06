package py.edu.fiuni.dmop.service;

import java.util.List;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.core.variable.Permutation;

import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.algorithm.DynamicAlgorithmsProvider;
import py.edu.fiuni.dmop.problem.DynamicVNFPlacementProblem;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;
import py.edu.fiuni.dmop.util.Utility;

/**
 * Dynamic Multi-Objective Placement Service
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class DMOPServiceTemp {

    private static final Logger logger = Logger.getLogger(DMOPServiceTemp.class);

    /**
     *
     * @throws Exception
     */
    public DMOPServiceTemp() throws Exception {

        AlgorithmFactory.getInstance().addProvider(new DynamicAlgorithmsProvider());

        Configurations.loadProperties();
        DataService.loadData();
    }

    public void runDynamicSolver() throws Exception {

        String algorithm = "DNSGAII-A";
        TrafficService trafficService = new TrafficService();
        List<Traffic> traffics = trafficService.readTraffics();

        NetworkConditionEnum networkCondition = NetworkConditionEnum.Normal;

        List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

        logger.info("Starting algorithm execution: " + algorithm);

        long inicio = System.currentTimeMillis();

        Properties properties = new Properties();
        properties.put("frequencyOfChange", Configurations.frequencyOfChange);
        properties.put("severityOfChange", Configurations.severityOfChange);

        NondominatedPopulation result = new Executor()
                .withProblemClass(DynamicVNFPlacementProblem.class, properties, trafficService, traffics, networkCondition)
                .withAlgorithm(algorithm)
                //.distributeOnAllCores()
                //.withMaxTime(6000)
                .withMaxEvaluations(500)
                .run();

        long fin = System.currentTimeMillis();

        logger.info("Execution completed for algorithm " + algorithm);
        logger.info("Non dominated solutions: " + result.size());
        logger.info("Execution time: " + Utility.getTime(fin - inicio));

        int index = 0;
        for (Solution sol : result) {
            System.out.print("Solution #" + index++ + " = ");
            for (int objIndex = 0; objIndex < sol.getNumberOfObjectives(); objIndex++) {
                System.out.printf("%s=%6f ,", sceneObjectives.get(objIndex).getPropertyName(), sol.getObjective(objIndex));
            }

            // Shows the permutation used in the current solution.
            System.out.println("Variable: " + Arrays.toString(((Permutation) sol.getVariable(0)).toArray()));
        }

        // Decision Making
        MCDMService decisionService = new MCDMService();
        Solution bestSolution = decisionService.calculateOptimalSolution(result, networkCondition);

        Permutation bestSolutionVariable = (Permutation) bestSolution.getVariable(0);
        System.out.println("Permutation Variable: " + Arrays.toString(bestSolutionVariable.toArray()));

        // Implement the best solution
        VnfService vnfService = new VnfService();
        ResultGraphMap resultGraph = vnfService.placementGraph(traffics, bestSolutionVariable);

        //Plot the graph after implementing the best solution for the given traffic list.
        GraphPlottingService plotService = new GraphPlottingService();
        plotService.plotGraph(resultGraph);
    }
}
