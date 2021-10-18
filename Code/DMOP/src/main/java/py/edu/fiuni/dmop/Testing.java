package py.edu.fiuni.dmop;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import sun.jvm.hotspot.utilities.IntArray;

public class Testing {
    
    public static void main(String[] args) {
        
        double[] newObjectives = DoubleStream.generate(() -> Double.MAX_VALUE).limit(6).toArray();
        System.out.println("Objectives: " + Arrays.toString(newObjectives));
        
        int[] borrados = {3, 5, 7, 9, 2, 34, 56, 77, 65};
        Set<Integer> c = IntStream.of(borrados).boxed().collect(Collectors.toSet()); // Arrays.asList(borrados);
        
        //Set<Integer> set = new HashSet<Integer>(c);
        
        int[] noModificados = IntStream.range(0, 100).filter(i -> !c.contains(i)).toArray();
        
        //Arrays.asList(noModificados).stream().co.forEach(System.out::println);
        System.out.println(Arrays.toString(noModificados));
        
        
        /*int[] borrados = {3, 5, 7, 9, 2, 34, 56, 77, 65};
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
        
        
        /*Configurations.loadProperties();
            DataService.loadData();

            KShortestPaths<Node, Link> pathInspector
                    = new KShortestPaths<>(DataService.graph, 3, Integer.MAX_VALUE);
            // 
            List<GraphPath<Node, Link>> paths = pathInspector.getPaths(DataService.nodesMap.get("node0"), DataService.nodesMap.get("node1"));

            System.out.println("Number of paths: " + paths.size());*/
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
