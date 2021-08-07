/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import py.edu.fiuni.dmop.dto.NFVdto.Link;
import py.edu.fiuni.dmop.service.DMOPService;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.util.Configurations;

/**
 *
 * @author Arnaldo
 */
public class GraphPlottingTest {

    private static Logger logger = Logger.getLogger(GraphPlottingTest.class);

    public static void main(String args[]) {

        try {
            System.setProperty("org.graphstream.ui", "swing");

            /*
        Graph graph = new SingleGraph("Tutorial 1");

        Node n = graph.addNode("A");
        //n.setAttribute("ui.label", "A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");

       

        graph.display();*/
            Graph graph = new SingleGraph("tutorial 1");

            graph.setAttribute("ui.stylesheet", "node {shape: box; size: 45px, 20px; fill-mode: plain; fill-color: lightgrey; stroke-mode: plain; stroke-color: #333;}");

            //graph.setStrict(false);
            //graph.setAutoCreate(true);
            /*graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");
        graph.addEdge("AD", "A", "D");
        graph.addEdge("DE", "D", "E");
        graph.addEdge("DF", "D", "F");
        graph.addEdge("EF", "E", "F");*/
            // graph.setAttribute("ui.stylesheet", "node#A {shape: box; size: 45px, 20px; fill-mode: plain; fill-color: lightgrey; stroke-mode: plain; stroke-color: grey;}");
///        node#A {shape: box; size: 15px, 20px; fill-mode: plain; fill-color: red; stroke-mode: plain; stroke-color: blue;}
            Configurations.loadProperties();
            DataService.loadData();

            Map<String, py.edu.fiuni.dmop.dto.NFVdto.Node> nodesMap = DataService.nodesMap;
            Map<String, Link> linksMap = DataService.linksMap;

            nodesMap.values().forEach(n -> {
                graph.addNode(n.getId());
            });

            linksMap.values().forEach(l -> {
                String linkId = l.getId();
                String nodesId = linkId.substring(0, linkId.indexOf("/"));
                String[] ids = nodesId.split("-");

                graph.addEdge(nodesId, ids[0], ids[1]);
            });

            for (Node node : graph) {
                node.setAttribute("ui.label", node.getId());
            }

            graph.display();

        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
