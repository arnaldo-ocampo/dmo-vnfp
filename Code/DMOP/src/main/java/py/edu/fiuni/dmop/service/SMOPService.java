package py.edu.fiuni.dmop.service;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.variable.Permutation;

import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.problem.StaticVNFPlacementProblem;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.util.Constants;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;
import py.edu.fiuni.dmop.util.Utility;

/**
 * Static Multi-Objective Placement Service
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class SMOPService {
    
    private static final Logger logger = Logger.getLogger(SMOPService.class);
    
    /**
     *
     * @throws Exception
     */
    public SMOPService() throws Exception {
        Configurations.loadProperties();
        DataService.loadData();
    }

    /**
     *
     */
    public void runSolutionsAnalyzer() {
        try {
            logger.info("Execution started: ");
            long inicioTotal = System.currentTimeMillis();
            
            SolutionService solutionService = new SolutionService();
            
            TrafficService trafficService = new TrafficService();
            List<Traffic> traffics = trafficService.readTraffics();
            
            NetworkConditionEnum networkCondition = NetworkConditionEnum.Normal;
            
            List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);
            
            String[] algorithms = {"NSGAIII","MOEAD", "RVEA"};

            // Number of rounds to run with every algorithm
            int seed = 3;

            // Setup the experiment
            Executor executor = new Executor()
                    .withProblemClass(StaticVNFPlacementProblem.class, traffics, networkCondition)
                    //.withMaxTime(60000)                    
                    .withMaxEvaluations(250)
                    .distributeOnAllCores();
            
            Analyzer analyzer = new Analyzer()
                    .withSameProblemAs(executor)
                    //.includeHypervolume()
                    .includeAdditiveEpsilonIndicator()
                    .includeGenerationalDistance()
                    //.includeInvertedGenerationalDistance()
                    //.includeMaximumParetoFrontError()                                        
                    .showStatisticalSignificance();

            // Run each algorithm for "seed" rounds
            for (String algorithm : algorithms) {
                logger.info("Starting execution for Algorithm " + algorithm);
                long inicio = System.currentTimeMillis();
                
                List<NondominatedPopulation> results = executor.withAlgorithm(algorithm).runSeeds(seed);

                // Results added to analyzer only if all of NondominatedPopulation have more than one solution.
                if (results.stream().allMatch(ndp -> ndp.size() > 1)) {
                    analyzer.addAll(algorithm, results);
                } else {
                    System.out.println(algorithm + " results don't added to Analyzer, not all of them have more than one solution");
                }
                
                int round = 0;
                for (NondominatedPopulation result : results) {
                    
                    solutionService.writeSolutions(result, "solutions_" + algorithm + "_round" + round + ".dat");
                    
                    logger.info(String.format("Pareto Front (Round) %d: %d solutions", round, result.size()));
                    int index = 0;
                    for (Solution sol : result) {
                        System.out.print("Solution #" + index++ + " = ");
                        for (int objIndex = 0; objIndex < sol.getNumberOfObjectives(); objIndex++) {
                            System.out.printf("%s=%6f ,", sceneObjectives.get(objIndex).getPropertyName(), sol.getObjective(objIndex));
                        }

                        // Shows the permutation used in the current solution.
                        System.out.println("Variable: " + Arrays.toString(((Permutation) sol.getVariable(0)).toArray()));
                    }
                    
                    round++;
                }
                
                long fin = System.currentTimeMillis();
                logger.info(String.format("Execution for algorithm %s completed: %s", algorithm, Utility.getTime(fin - inicio)));
                
            }

            // Print the Analyzer results
            logger.info("Starting Analysis");
            long inicio = System.currentTimeMillis();
            analyzer.printAnalysis();
            long fin = System.currentTimeMillis();
            logger.info("Analysis Completed " + Utility.getTime(fin - inicio));

            // Plot the Analyzer results
            new Plot().add(analyzer).show();
            long finTotal = System.currentTimeMillis();
            logger.info("Total Execution time: " + Utility.getTime(finTotal - inicioTotal));
            
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    /**
     *
     * @throws Exception
     */
    public void runSolutionDeployer() throws Exception {
        
        String algorithm = "NSGAIII";
        
        TrafficService trafficService = new TrafficService();
        List<Traffic> traffics = trafficService.readTraffics();
        
        NetworkConditionEnum networkCondition = NetworkConditionEnum.Normal;
        
        List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);
        
        logger.info(String.format("Execution for algorithm %s started", algorithm));
        long inicio = System.currentTimeMillis();
        
        NondominatedPopulation result = new Executor()
                .withProblemClass(StaticVNFPlacementProblem.class, traffics, networkCondition)
                .withAlgorithm(algorithm)
                .distributeOnAllCores()
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

        /*//display the results
        System.out.format("Nro.     Bandwidth       Energy          Delay           Distance        " +
                "Fragmentation       Licence        LoadTrafic      MaxUseLink      NumberIntances" +
                "    Resources     SLO        Throughput%n");
        int i = 1;
        for (Solution solution : result) {
            System.out.format(i++ + "       %.4f        %.4f        %.4f        %.4f        %.4f" +
                            "       %.4f        %.4f        %.4f        %.4f        %.4f" +
                            "       %.4f        %.4f%n",
                    solution.getObjective(0), solution.getObjective(1),
         */
    }
    
    /**
     * 
     */
    public void runMultiWindowsSolutionsAnalyzer() {
        try {
            logger.info("Execution started: ");
            long inicioTotal = System.currentTimeMillis();
            
            SolutionService solutionService = new SolutionService();
            TrafficService trafficService = new TrafficService();
            
            List<List<Traffic>> windowsTraffics = trafficService.readAllTraffics(Constants.NUMBER_OF_WINDOWS);
            
            String[] algorithms = {"NSGAIII", "MOEAD", "RVEA"};
            
            for (int windows = 0; windows < Constants.MAX_WINDOWS; windows++) {
                
                List<Traffic> traffics = windowsTraffics.get(windows);
                NetworkConditionEnum networkCondition = traffics.size() > Constants.NORMAL_UPPER_LIMIT ? NetworkConditionEnum.Overloaded : NetworkConditionEnum.Normal;
                
                List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

                // Run each algorithm for "seed" rounds
                for (String algorithm : algorithms) {
                    logger.info("Starting execution for Algorithm " + algorithm + " Windows #" + windows);

                    // Setup the experiment
                    Executor executor = new Executor()
                            .withProblemClass(StaticVNFPlacementProblem.class, traffics, networkCondition)
                            .withMaxEvaluations(5000)
                            .distributeOnAllCores();
                    
                    Analyzer analyzer = new Analyzer()
                            .withSameProblemAs(executor)
                            .includeAllMetrics()
                            .showAll()
                            .showStatisticalSignificance();
                    
                    long inicio = System.currentTimeMillis();
                    
                    List<NondominatedPopulation> results = executor.withAlgorithm(algorithm).runSeeds(Constants.MAX_ROUNDS);

                    // Results added to analyzer only if all of NondominatedPopulation have more than one solution.
                    if (results.stream().allMatch(ndp -> ndp.size() > 1)) {
                        analyzer.addAll(algorithm, results);
                    } else {
                        System.out.println(algorithm + " results don't added to Analyzer, not all of them have more than one solution");
                    }
                    
                    int round = 1;
                    for (NondominatedPopulation result : results) {
                        
                        try {
                            String fileName = String.format(Constants.SOLUTION_FILENAME_TEMPLATE, algorithm, round, windows);
                            solutionService.writeSolutions(result, fileName);
                        } catch (Exception ex) {
                            logger.fatal("Error:", ex);
                        }
                        
                        logger.info(String.format("Pareto Front (Round) %d: %d solutions", round, result.size()));
                        int index = 0;
                        for (Solution sol : result) {
                            System.out.print("Solution #" + index++ + " = ");
                            for (int objIndex = 0; objIndex < sol.getNumberOfObjectives(); objIndex++) {
                                System.out.printf("%s=%6f ,", sceneObjectives.get(objIndex).getPropertyName(), sol.getObjective(objIndex));
                            }

                            // Shows the permutation used in the current solution.
                            System.out.println("Variable: " + Arrays.toString(((Permutation) sol.getVariable(0)).toArray()));
                        }
                        
                        round++;
                    }
                    
                    long fin = System.currentTimeMillis();
                    logger.info(String.format("Execution for algorithm %s completed: %s", algorithm, Utility.getTime(fin - inicio)));
                    
                    try {
                        // Print the Analyzer results
                        logger.info("Starting Analysis");
                        long inicioAnalisis = System.currentTimeMillis();
                        analyzer.printAnalysis(new PrintStream(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/analyzer_results_" + algorithm + "_win" + windows + ".txt"))));
                        analyzer.saveAs(algorithm, new File(Utility.buildFilePath(Configurations.solutionsFolder + "/analyzer_data_" + algorithm + "_win" + windows + ".txt")));

                        analyzer.printAnalysis();
                        long finAnalisis = System.currentTimeMillis();
                        logger.info("Analysis Completed " + Utility.getTime(finAnalisis - inicioAnalisis));

                        // Plot the Analyzer results
                        new Plot().add(analyzer).show();
                        long finTotal = System.currentTimeMillis();
                        logger.info("Total Execution time: " + Utility.getTime(finTotal - inicioTotal));
                    } catch (Exception ex) {
                        logger.fatal("Error:", ex);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }
    
}
