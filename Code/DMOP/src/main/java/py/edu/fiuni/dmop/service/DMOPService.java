package py.edu.fiuni.dmop.service;

import com.sun.tools.javac.tree.Pretty;
import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.util.Configurations;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.moeaframework.analysis.plot.Plot;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;
import py.edu.fiuni.dmop.decision.topsis.Topsis;
import py.edu.fiuni.dmop.problem.VNFPlacementProblem;

public class DMOPService {

    Logger logger = Logger.getLogger(DMOPService.class);

    public void maoeaMetrics() {
        try {
            Configurations.loadProperties();
            DataService.loadData();
            TrafficService.readTraffics();

            logger.info("Inicio de ejecución: ");
            long inicioTotal = System.currentTimeMillis();

            String[] algorithms = {"NSGAIII"/*, "MOEAD", "RVEA"*/};

            //setup the experiment
            Executor executor = new Executor()
                    .withProblemClass(VNFPlacementProblem.class)
                    //.withMaxTime(60000)
                    .distributeOnAllCores()
            .withMaxEvaluations(10000);

            Analyzer analyzer = new Analyzer()
                    .withSameProblemAs(executor)
                    .includeHypervolume()
                    .showStatisticalSignificance();

            /* 
            Analyzer analyzer = new Analyzer()
                    .withProblemClass(VNFPlacementProblem.class)
                    .includeGenerationalDistance()
                    .includeInvertedGenerationalDistance()
                    //.includeMaximumParetoFrontError()
                    //.includeAdditiveEpsilonIndicator()
                    //.includeSpacing()
                    //.includeR1()
                    //.includeR2()
                    //.includeR3()
                    .showStatisticalSignificance();
             */
            //run each algorithm for seeds
            for (String algorithm : algorithms) {
                logger.info("Inicio de ejecución " + algorithm);
                long inicio = System.currentTimeMillis();

                int seed = 1;
                List<NondominatedPopulation> results = executor.withAlgorithm(algorithm).runSeeds(3);
                for (NondominatedPopulation result : results) {
                    logger.info("Frente pareto (seed) " + seed++ + ": " + result.size() + " soluciones");
                }

                analyzer.addAll(algorithm, results);
                long fin = System.currentTimeMillis();
                logger.info("Fin de ejecución " + algorithm + " " + getTime(fin - inicio));
            }

            //print the results
            long inicio = System.currentTimeMillis();
            logger.info("Inicio Analysis");
            analyzer.printAnalysis();
            long fin = System.currentTimeMillis();
            logger.info("Fin Analysis " + getTime(fin - inicio));

            //plot the results
            new Plot()
                    .add(analyzer)
                    .show();

            long finTotal = System.currentTimeMillis();
            logger.info("Tiempo de ejecución Total: " + getTime(finTotal - inicioTotal));

        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }

    }

    /**
     *
     * @throws Exception
     */
    public void maoeaSolutions() throws Exception {

        Topsis topsis = new Topsis();

        Criteria bandwidth = new Criteria("Bandwidth", 0.4);
        Criteria resources = new Criteria("Resources", 0.3);
        Criteria energy = new Criteria("Energy", 0.3, true);

        Configurations.loadProperties();
        DataService.loadData();

        List<Traffic> traffics = TrafficService.readTraffics();

        String algorithm = "NSGAIII";
        logger.info("Inicio de ejecución " + algorithm);
        long inicio = System.currentTimeMillis();

        NondominatedPopulation result = new Executor()
                .withProblemClass(VNFPlacementProblem.class)
                .withAlgorithm(algorithm)
                .distributeOnAllCores()
                .withMaxTime(60000)
                .run();

        long fin = System.currentTimeMillis();
        logger.info("Fin de ejecución " + algorithm);
        logger.info("Frente pareto: " + result.size() + " soluciones");
        logger.info("Tiempo de ejecución: " + getTime(fin - inicio));

        //logger.info("Throughput: ");
        
        
        /** Init Decision Maker */
        int i = 1;
        for (Solution solution : result) {
            //logger.info(i++ + ") " + solution.getObjective(11));

            Alternative alt = new Alternative(("Solution Num.: ").concat(Integer.toString(i)) );
            alt.addCriteriaValue(bandwidth, solution.getObjective(0));
            alt.addCriteriaValue(energy, solution.getObjective(1));
            alt.addCriteriaValue(resources, solution.getObjective(9));
            topsis.addAlternative(alt);
            i++;
        }

        try {
            topsis.calculateOptimalSolution();
            Alternative topsisOptimal = topsis.getBestAlternative();
            
            System.out.println("The optimal solution is: " + topsisOptimal.getName());
            System.out.println("The optimal solution score is: " + topsisOptimal.getCalculatedPerformanceScore());
            
            //topsis.printDetailedResults();

        } catch (UnsupportedOperationException e) {
            logger.error(e.getMessage());
            System.err.println(e.getMessage());
        }

       
        /*
       VnfService vnfService = new VnfService();
       List<ResultGraphMap> resultGraphMaps = new ArrayList<>();
            //display the results
            System.out.format("Nro.     Bandwidth       Energy          Delay           Distance        " +
                    "Fragmentation       Licence        LoadTrafic      MaxUseLink      NumberIntances" +
                    "    Resources     SLO        Throughput%n");

            int i = 1;
            for (Solution solution : result) {
                System.out.format(i++ + "       %.4f        %.4f        %.4f        %.4f        %.4f" +
                                "       %.4f        %.4f        %.4f        %.4f        %.4f" +
                                "       %.4f        %.4f%n",
                        solution.getObjective(0),
                        solution.getObjective(1),
                        solution.getObjective(2),
                        solution.getObjective(3),
                        solution.getObjective(4),
                        solution.getObjective(5),
                        solution.getObjective(6),
                        solution.getObjective(7),
                        solution.getObjective(8),
                        solution.getObjective(9),
                        solution.getObjective(10),
                        solution.getObjective(11));

                //Cada pareto llama de nuevo a placement para obtener las ubicaciones
             //   resultGraphMaps.add(vnfService.placementGraph(traffics, (Permutation) solution.getVariable(0)));
            }
           // logger.info(resultGraphMaps);
         */
    }

    public String getTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
