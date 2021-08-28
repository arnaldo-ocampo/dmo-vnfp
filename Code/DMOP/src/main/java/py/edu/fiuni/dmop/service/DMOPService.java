package py.edu.fiuni.dmop.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.log4j.Logger;
import org.moeaframework.Analyzer;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.util.Configurations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.variable.Permutation;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;
import py.edu.fiuni.dmop.decision.topsis.Topsis;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.problem.VNFPlacementProblem;
import py.edu.fiuni.dmop.dto.NFVdto.*;

public class DMOPService {

    private static final Logger logger = Logger.getLogger(DMOPService.class);

    /**
     *
     */
    public void moeaMetrics() {
        try {
            Configurations.loadProperties();
            DataService.loadData();
            TrafficService.readTraffics();

            logger.info("Inicio de ejecución: ");
            long inicioTotal = System.currentTimeMillis();

            String[] algorithms = {"NSGAIII","MOEAD", "RVEA"};

            //setup the experiment
            Executor executor = new Executor()
                    .withProblemClass(VNFPlacementProblem.class)
                    //.withMaxTime(60000)                    
                    .withMaxEvaluations(1000)
                    .distributeOnAllCores();

            Analyzer analyzer = new Analyzer()
                    .withSameProblemAs(executor)
                    .includeHypervolume()
                    .includeAdditiveEpsilonIndicator()
                    //.includeGenerationalDistance()
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

                int seed = 3;
                List<NondominatedPopulation> results = executor.withAlgorithm(algorithm).runSeeds(seed);
                for (NondominatedPopulation result : results) {
                    logger.info("Frente pareto (seed) " + seed++ + ": " + result.size() + " soluciones");
/*
                    int index = 0;
                    for (Solution sol : result) {
                        System.out.print("Soluction #" + index++ + " = ");
                        for (int obj = 0; obj < 8; obj++) {
                            System.out.printf("%6f ,", sol.getObjective(obj));
                        }
                        System.out.println("");
                    }
*/
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
            new Plot().add(analyzer).show();
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
    public void moeaSolutions() throws Exception {

        /*Topsis topsis = new Topsis();

        Criteria bandwidth = new Criteria("Bandwidth", 0.4);
        Criteria resources = new Criteria("Resources", 0.3);
        Criteria energy = new Criteria("Energy", 0.3, true);*/

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
                //.withMaxTime(6000)
                .withMaxEvaluations(500)
                .run();

        long fin = System.currentTimeMillis();
        logger.info("Fin de ejecución " + algorithm);
        logger.info("Frente pareto: " + result.size() + " soluciones");
        logger.info("Tiempo de ejecución: " + getTime(fin - inicio));

        //logger.info("Throughput: ");
        /**
         * Init Decision Maker
         */
        /*int i = 0;
        for (Solution solution : result) {
            //logger.info(i++ + ") " + solution.getObjective(0));

            Alternative alt = new Alternative(Integer.toString(i) // ("Solution Num.: ").concat(Integer.toString(i)));
            alt.addCriteriaValue(bandwidth, solution.getObjective(0));
            alt.addCriteriaValue(energy, solution.getObjective(1));
            alt.addCriteriaValue(resources, solution.getObjective(9));
            topsis.addAlternative(alt);
            i++;
        }*/

        try {
            /*topsis.calculateOptimalSolution();
            Alternative topsisOptimal = topsis.getBestAlternative();

            Solution winner = result.get(Integer.parseInt(topsisOptimal.getName()));*/
            
            MCDMService decisionMaker = new MCDMService();
            
            Solution winner = decisionMaker.calculateOptimalSolution(result);
                        
            Permutation variable = (Permutation) winner.getVariable(0);

            //System.out.println("The optimal solution is: " + topsisOptimal.getName());
            System.out.println("Valor de la variable de Permutación: " + Arrays.toString(variable.toArray()));
            //System.out.println("The optimal solution score is: " + topsisOptimal.getCalculatedPerformanceScore());

            
            /*
            //topsis.printDetailedResults();
            VnfService vnfService = new VnfService();
            //List<ResultGraphMap> resultGraphMaps = new ArrayList<>();
            ResultGraphMap resultGraph = vnfService.placementGraph(traffics, variable);
            
            */

            /*System.setProperty("org.graphstream.ui", "swing");
            Graph graph = new SingleGraph("Network Topology");
            graph.setAttribute("ui.stylesheet", "node {shape: box; size: 100px, 40px; fill-mode: plain; fill-color: lightgrey; stroke-mode: plain; stroke-color: #333; font-weight: bold;}");

            Map<String, String> labelMap = new HashMap<>();

            // Adds every node to the graph
            resultGraph.getNodesMap().values().forEach(n -> {
                String id = n.getId();
                StringBuilder sb = new StringBuilder(n.getId());
                if (n.getServer() != null) {

                    //labelMap.put(id, id+":"+n.getServer().getVnfs().values().size());
                    //sb.append(":"+n.getServer().getVnfs().values().size());
                    Set<Vnf> vnfs = new HashSet<>();

                    n.getServer().getVnfs().values().forEach((t) -> {
                        t.forEach((v) -> {
                            v.getVnfs().forEach((vnf) -> {
                                vnfs.add(vnf);
                            });
                        });
                    });

                    vnfs.forEach((v) -> {
                        sb.append(":" + v.getId() + "[" + v.getType() + "]\n");
                    });
                    labelMap.put(id, sb.toString());
                }

                graph.addNode(id);
            });

            // Adds every edge (link) between neighbour nodes to the graph
            resultGraph.getLinksMap().values().forEach(link -> {
                String linkId = link.getId();
                String nodesId = linkId.substring(0, linkId.indexOf("/"));
                String[] ids = nodesId.split("-");

                graph.addEdge(nodesId, ids[0], ids[1]);
            });

            // Configure the graph to show a label next to every node
            for (Node node : graph) {
                node.setAttribute("ui.label", labelMap.get(node.getId()));
            }

            graph.display();*/

        } catch (UnsupportedOperationException e) {
            logger.error(e.getMessage());
            System.err.println(e.getMessage());
        }

        /*VnfService vnfService = new VnfService();
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
         */
        //Cada pareto llama de nuevo a placement para obtener las ubicaciones
        //resultGraphMaps.add(vnfService.placementGraph(traffics, (Permutation) solution.getVariable(0)));
        //}
        // logger.info(resultGraphMaps);
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
