package py.edu.fiuni.dmop;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.spi.AlgorithmFactory;

import py.edu.fiuni.dmop.algorithm.DynamicAlgorithmsProvider;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.problem.DynamicVNFPlacementProblem;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.SolutionService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.Constants;
import py.edu.fiuni.dmop.util.Utility;

/**
 * Corre una ronda de DNSGA-II sobre todas las ventanas, como DMOPService.runSolver.
 * Guarda el frente de cada ventana apenas termina.
 *
 * Uso: DynamicSolverRunner algoritmo ronda [generaciones_por_ventana] [ventanas]
 */
public class DynamicSolverRunner {

    private static final Logger logger = Logger.getLogger(DynamicSolverRunner.class);

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Uso: DynamicSolverRunner algoritmo ronda [generaciones_por_ventana] [ventanas]");
            return;
        }

        String algorithmName = args[0];
        int round = Integer.parseInt(args[1]);

        try {
            AlgorithmFactory.getInstance().addProvider(new DynamicAlgorithmsProvider());
            Configurations.loadProperties();
            DataService.loadData();

            int generationsPerWindow = args.length > 2 ? Integer.parseInt(args[2]) : Configurations.frequencyOfChange;
            int windows = args.length > 3 ? Integer.parseInt(args[3]) : Constants.MAX_WINDOWS;

            //La semilla es el numero de ronda
            PRNG.setSeed(round);

            //Mismos parametros que runSolver
            Properties properties = new Properties();
            properties.put("frequencyOfChange", generationsPerWindow);
            properties.put("severityOfChange", Configurations.severityOfChange);
            properties.put("zeta", 0.2d);

            TrafficService trafficService = new TrafficService();
            List<List<Traffic>> windowsTraffics = trafficService.readAllTraffics(Constants.NUMBER_OF_WINDOWS);

            DynamicVNFPlacementProblem problem = new DynamicVNFPlacementProblem(properties, trafficService, windowsTraffics,
                    Constants.LOWER_LIMIT, Constants.UPPER_LIMIT, Constants.NORMAL_UPPER_LIMIT, Constants.RANDOMIZE_TRAFFICS);
            Algorithm algorithm = AlgorithmFactory.getInstance().getAlgorithm(algorithmName, properties, problem);

            SolutionService solutionService = new SolutionService();
            long start = System.currentTimeMillis();
            int counter = 0;

            while (true) {
                if (++counter > generationsPerWindow) {
                    int window = problem.getCurrentWindows();
                    String fileName = String.format(Constants.SOLUTION_FILENAME_TEMPLATE, algorithmName, round, window);
                    solutionService.writeSolutions(algorithm.getResult(), fileName);
                    logger.warn(String.format("%s r%d: ventana %d guardada (%d evaluaciones, %s)", algorithmName, round, window,
                            algorithm.getNumberOfEvaluations(), Utility.getTime(System.currentTimeMillis() - start)));

                    if (window == windows - 1) {
                        break;
                    }
                    counter = 1;
                    problem.changeEnvironment();
                }
                algorithm.step();
            }

        } catch (Exception ex) {
            logger.fatal("Error en la ronda " + round + " de " + algorithmName, ex);
        }
    }
}
