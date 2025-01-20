package py.edu.fiuni.dmop;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.problem.StaticVNFPlacementProblem;
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

public class Testing {

    private static final Logger logger = Logger.getLogger(Testing.class);

    public static void main(String[] args) {

        try {
            long inicioTotal = System.currentTimeMillis();

            Configurations.loadProperties();
            DataService.loadData();

            SolutionService solutionService = new SolutionService();
            TrafficService trafficService = new TrafficService();
            List<Traffic> traffics = trafficService.readTraffics();

            NetworkConditionEnum networkCondition = NetworkConditionEnum.Normal;
            List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

            String[] algorithms = {"NSGAIII"/*,"MOEAD", "RVEA"*/};

            /*
            // Setup the experiment
            Executor executor = new Executor()
                    .withProblemClass(StaticVNFPlacementProblem.class, traffics, networkCondition)
                    //.withMaxTime(60000)                    
                    .withMaxEvaluations(1000)
                    .distributeOnAllCores();
             */
            Analyzer analyzer = new Analyzer()
                    .withProblemClass(StaticVNFPlacementProblem.class, traffics, networkCondition)
                    .includeAllMetrics()
                    .showAll();
            //.withSameProblemAs(executor)
            //.includeHypervolume()
            //.includeAdditiveEpsilonIndicator()
            //.includeGenerationalDistance()
            //.includeInvertedGenerationalDistance()
            //.includeMaximumParetoFrontError()                    
            //.includeSpacing()
            //.includeR1()
            //.includeR2()
            //.includeR3()
            //.showStatisticalSignificance();

            NondominatedPopulation result1 = solutionService.readSolutions("solutions_NSGAIII_round0.dat");
            NondominatedPopulation result2 = solutionService.readSolutions("solutions_NSGAIII_round1.dat");
            NondominatedPopulation result3 = solutionService.readSolutions("solutions_NSGAIII_round2.dat");

            String algorithm = "NSGAiii";

            analyzer.addAll(algorithm, Arrays.asList(result1, result2, result3));

            // Print the Analyzer results
            logger.info("Starting Analysis");
            long inicio = System.currentTimeMillis();
            //analyzer.showAll();
            analyzer.printAnalysis(new PrintStream(new File(Utility.buildFilePath(Configurations.solutionsFolder + "/analyzer_print.txt"))));
            analyzer.saveAs(algorithm, new File(Utility.buildFilePath(Configurations.solutionsFolder + "/analyzer_result.txt")));
            analyzer.printAnalysis();

            long fin = System.currentTimeMillis();
            logger.info("Analysis Completed " + Utility.getTime(fin - inicio));

            // Plot the Analyzer results
            new Plot().add(analyzer).show();
            long finTotal = System.currentTimeMillis();
            logger.info("Total Execution time: " + Utility.getTime(finTotal - inicioTotal));

            // Decision Making
            MCDMService decisionService = new MCDMService();

// Definir el algoritmo a usar dinámicamente
            String selectedAlgorithm = "AHP"; // Cambiar por el algoritmo deseado

            Solution bestSolution1 = decisionService.calculateOptimalSolution(result1, networkCondition, selectedAlgorithm);
            Solution bestSolution2 = decisionService.calculateOptimalSolution(result2, networkCondition, selectedAlgorithm);
            Solution bestSolution3 = decisionService.calculateOptimalSolution(result3, networkCondition, selectedAlgorithm);


            Permutation bestVar1 = (Permutation) bestSolution1.getVariable(0);
            System.out.println("Permutation Variable 1: " + Arrays.toString(bestVar1.toArray()) + " Fitness:: " + Arrays.toString(bestSolution1.getObjectives()));

            Permutation bestVar2 = (Permutation) bestSolution1.getVariable(0);
            System.out.println("Permutation Variable 2: " + Arrays.toString(bestVar2.toArray()) + " Fitness:: " + Arrays.toString(bestSolution2.getObjectives()));

            Permutation bestVar3 = (Permutation) bestSolution1.getVariable(0);
            System.out.println("Permutation Variable 3: " + Arrays.toString(bestVar3.toArray()) + " Fitness:: " + Arrays.toString(bestSolution3.getObjectives()));

            // Implement the best solution
            VnfService vnfService = new VnfService();
            ResultGraphMap resultGraph1 = vnfService.placementGraph(traffics, bestVar1);

            ResultGraphMap resultGraph2 = vnfService.placementGraph(traffics, bestVar2);

            ResultGraphMap resultGraph3 = vnfService.placementGraph(traffics, bestVar3);

            //Plot the graph after implementing the best solution for the given traffic list.
            GraphPlottingService plotService = new GraphPlottingService();
            plotService.plotGraph(resultGraph1);
            plotService.plotGraph(resultGraph2);
            plotService.plotGraph(resultGraph3);

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        /*
        double[] newObjectives = DoubleStream.generate(() -> Double.MAX_VALUE).limit(6).toArray();
        System.out.println("Objectives: " + Arrays.toString(newObjectives));
        
        int[] borrados = {3, 5, 7, 9, 2, 34, 56, 77, 65};
        Set<Integer> c = IntStream.of(borrados).boxed().collect(Collectors.toSet()); // Arrays.asList(borrados);
        
        //Set<Integer> set = new HashSet<Integer>(c);
        
        int[] noModificados = IntStream.range(0, 100).filter(i -> !c.contains(i)).toArray();
        
        //Arrays.asList(noModificados).stream().co.forEach(System.out::println);
        System.out.println(Arrays.toString(noModificados));
         */
        
        /*
        int[] borrados = {3, 5, 7, 9, 2, 34, 56, 77, 65};
        Collection<Integer> c =  Arrays.asList(3, 5, 7, 9, 2, 34, 56, 77, 65);
        Set<Integer> set = new HashSet<Integer>(c);
        
        int[] noModificados = IntStream.range(0, 100).filter(i -> !set.contains(i)).toArray();
        
        //Arrays.asList(noModificados).stream().co.forEach(System.out::println);
        System.out.println(Arrays.toString(noModificados));
         */

        /*
        //configure and run the DTLZ2 function
        NondominatedPopulation result = new Executor()
                .withProblemClass(MyDTLZ2.class)
                .withAlgorithm("NSGAII")
                .withMaxEvaluations(10000)
                .run();

        //display the results
        System.out.format("Objective1  Objective2%n");

        for (Solution solution : result) {
            System.out.format("%.4f      %.4f%n",
                    solution.getObjective(0),
                    solution.getObjective(1));
            double[] x = EncodingUtils.getReal(solution);
            System.out.println("Variables: " + Arrays.toString(x));
        }
         */
        /*
        Configurations.loadProperties();
        DataService.loadData();

        KShortestPaths<Node, Link> pathInspector
                = new KShortestPaths<>(DataService.graph, 3, Integer.MAX_VALUE);
        // 
        List<GraphPath<Node, Link>> paths = pathInspector.getPaths(DataService.nodesMap.get("node0"), DataService.nodesMap.get("node1"));

        System.out.println("Number of paths: " + paths.size());
        */
    }

    /**
     * Implementation of the DTLZ2 function.
     */
    public static class MyDTLZ2 extends AbstractProblem {

        /**
         * Constructs a new instance of the DTLZ2 function, defining it to
         * include 11 decision variables and 2 objectives.
         */
        public MyDTLZ2() {
            super(11, 2);
        }

        /**
         * Constructs a new solution and defines the bounds of the decision
         * variables.
         */
        @Override
        public Solution newSolution() {
            Solution solution = new Solution(getNumberOfVariables(),
                    getNumberOfObjectives());

            for (int i = 0; i < getNumberOfVariables(); i++) {
                solution.setVariable(i, new RealVariable(0.0, 1.0));
            }

            return solution;
        }

        /**
         * Extracts the decision variables from the solution, evaluates the
         * Rosenbrock function, and saves the resulting objective value back to
         * the solution.
         */
        @Override
        public void evaluate(Solution solution) {
            double[] x = EncodingUtils.getReal(solution);
            //System.out.println("Evaluando: " + Arrays.toString(x));            
            double[] f = new double[numberOfObjectives];

            int k = numberOfVariables - numberOfObjectives + 1;

            double g = 0.0;
            for (int i = numberOfVariables - k; i < numberOfVariables; i++) {
                g += Math.pow(x[i] - 0.5, 2.0);
            }

            for (int i = 0; i < numberOfObjectives; i++) {
                f[i] = 1.0 + g;

                for (int j = 0; j < numberOfObjectives - i - 1; j++) {
                    f[i] *= Math.cos(0.5 * Math.PI * x[j]);
                }

                if (i != 0) {
                    f[i] *= Math.sin(0.5 * Math.PI * x[numberOfObjectives - i - 1]);
                }
            }

            solution.setObjectives(f);
        }

    }
}
