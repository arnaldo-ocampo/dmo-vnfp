/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.Algorithm;
import py.edu.fiuni.dmop.dto.NFVdto.Link;
import py.edu.fiuni.dmop.dto.NFVdto.Node;
import py.edu.fiuni.dmop.service.DMOPService;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.util.Configurations;

/**
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class VNFPlacement {

    private static final Logger logger = Logger.getLogger(VNFPlacement.class);

    public static void main(String args[]) {

        logger.debug("Starting VNF Placement process");
        
        //String app = System.getProperty("app.home");
        //System.out.println(app);
        //Algorithm alg = new NSGAII();
        //org.moeaframework.core.spi.AlgorithmFactory
        
        try {
            //DMOPService dmopService = new DMOPService();
            //dmopService.moeaSolutions();
            
            Configurations.loadProperties();
            DataService.loadData();

            KShortestPaths<Node, Link> pathInspector
                    = new KShortestPaths<>(DataService.graph, 3, Integer.MAX_VALUE);

            // 
            List<GraphPath<Node, Link>> paths = pathInspector.getPaths(DataService.nodesMap.get("node0"), DataService.nodesMap.get("node1"));

            System.out.println("Number of paths: " + paths.size());
            
        } catch (Exception ex) {
            logger.fatal("ERROR", ex);            
        }
    }
}
