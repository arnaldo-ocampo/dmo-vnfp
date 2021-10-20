package py.edu.fiuni.dmop;

import java.util.Map;
import org.apache.log4j.Logger;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import py.edu.fiuni.dmop.dto.NFVdto.Link;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.GraphPlottingService;
import py.edu.fiuni.dmop.util.Configurations;

/**
 * Reads the network topology and then it plots the Graph
 * 
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class GraphPlottingTest {

    private static Logger logger = Logger.getLogger(GraphPlottingTest.class);

    public static void main(String args[]) {

        try {
            
            Configurations.loadProperties();
            DataService.loadData();

            Map<String, py.edu.fiuni.dmop.dto.NFVdto.Node> nodesMap = DataService.nodesMap;
            Map<String, Link> linksMap = DataService.linksMap;
            
            GraphPlottingService plotService = new GraphPlottingService();
            plotService.plotGraph(nodesMap, linksMap);            
            
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
