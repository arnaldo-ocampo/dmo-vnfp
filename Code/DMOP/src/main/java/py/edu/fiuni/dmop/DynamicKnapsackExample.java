package py.edu.fiuni.dmop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.spi.AlgorithmFactory;
import org.moeaframework.util.Vector;
import org.apache.log4j.Logger;

import py.edu.fiuni.dmop.DynamicPlacement;
import py.edu.fiuni.dmop.algorithm.DynamicAlgorithmsProvider;
import py.edu.fiuni.dmop.problem.DynamicKnapsack;
import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.Utility;

/**
 * Example of binary optimization using the {@link Knapsack} problem on the
 * {@code knapsack.100.2} instance.
 */
public class DynamicKnapsackExample {
    
    private static final Logger logger = Logger.getLogger(DynamicKnapsackExample.class);

    /**
     * Starts the example running the knapsack problem.
     *
     * @param args the command line arguments
     * @throws IOException if an I/O error occurred
     */
    public static void main(String[] args) throws IOException {
        
        try {            
            
            Configurations.loadProperties();

            // open the file containing the knapsack problem instance
            //InputStream input = new InputStr  Knapsack.class.getResourceAsStream("knapsack.100.2");
            InputStream input = new FileInputStream(new File(Utility.buildFilePath(Configurations.problemPackage + "knapsack.100.2")));            
            
            if (input == null) {
                System.err.println("Unable to find the file knapsack.100.2");
                System.exit(-1);
            }
            
            Properties properties = new Properties();
            properties.put("dataset", input);
            properties.put("frequencyOfChange", 5000);
            properties.put("severityOfChange", 1d);
            
            AlgorithmFactory.getInstance().addProvider(new DynamicAlgorithmsProvider());
            
            // solve using NSGA-II
            NondominatedPopulation result = new Executor()
                    .withProblemClass(DynamicKnapsack.class, properties)
                    .withAlgorithm("DNSGAIIA")
                    .withMaxEvaluations(500000)
                    .distributeOnAllCores()
                    .run();
            // solve using AMOSA
//		OperatorFactory.getInstance().addProvider(new HLPProvider());
//		TemperatureTerminationCondition temperatureTerminationCondition = new TemperatureTerminationCondition();
//		NondominatedPopulation result = new Executor().withProblemClass(Knapsack.class, input).withAlgorithm("AMOSA")
//				.withMaxEvaluations(50000).distributeOnAllCores().withTerminationCondition(temperatureTerminationCondition).run();
//

            // print the results
            for (int i = 0; i < result.size(); i++) {
                Solution solution = result.get(i);
                double[] objectives = solution.getObjectives();

                // negate objectives to return them to their maximized form
                objectives = Vector.negate(objectives);
                
                System.out.println("Solution " + (i + 1) + ":");
                System.out.println("    Sack 1 Profit: " + objectives[0]);
                System.out.println("    Sack 2 Profit: " + objectives[1]);
                System.out.println("    Binary String: " + solution.getVariable(0));
            }
            
        } catch (Exception e) {
            logger.error("Error on Dynamic Knapsack problem solving", e);
        }
    }
    
}
