package py.edu.fiuni.dmop.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.jgrapht.DirectedGraph;
import py.edu.fiuni.dmop.dto.KPath;

import py.edu.fiuni.dmop.dto.NFVdto.Link;
import py.edu.fiuni.dmop.dto.NFVdto.Vnf;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.util.Configurations;

/**
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class GraphPlottingService {

    private static Logger logger = Logger.getLogger(GraphPlottingService.class);

    /**
     *
     * @param nodesMap
     * @param linksMap
     */
    public void plotGraph(Map<String, py.edu.fiuni.dmop.dto.NFVdto.Node> nodesMap, Map<String, Link> linksMap) {

        System.setProperty("org.graphstream.ui", "swing");
        Graph graph = new SingleGraph("Network Topology");
        graph.setAttribute("ui.stylesheet", "node {shape: box; size: 45px, 20px; fill-mode: plain; fill-color: lightgrey; stroke-mode: plain; stroke-color: #333;}");

        // Adds every node to the graph
        nodesMap.values().forEach(n -> {
            graph.addNode(n.getId());
        });

        // Adds every edge (link) between neighbour nodes to the graph
        linksMap.values().forEach(l -> {
            String linkId = l.getId();
            String nodesId = linkId.substring(0, linkId.indexOf("/"));
            String[] ids = nodesId.split("-");

            graph.addEdge(nodesId, ids[0], ids[1]);
        });

        // Configure the graph to show a label next to every node
        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }

        graph.display();
    }

    /**
     * 
     * @param resultGraph 
     */
    public void plotGraph(ResultGraphMap resultGraph) {

        System.setProperty("org.graphstream.ui", "swing");
        Graph graph = new SingleGraph("Network Topology");
        graph.setAttribute("ui.stylesheet", "node {shape: box; size: 100px, 40px; fill-mode: plain; fill-color: lightgrey; stroke-mode: plain; stroke-color: #333;}");

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

        graph.display();
    }
    
    private void plotMultiStageGraph(DirectedGraph<String, KPath> graphMultiStage) {

        Set<String> nodes = new HashSet();
        Set<String> links = new HashSet();

        System.setProperty("org.graphstream.ui", "swing");
        Graph graph = new SingleGraph("MutiStage Graph");
        graph.setAttribute("ui.stylesheet", "node {shape: box; size: 45px, 20px; fill-mode: plain; fill-color: lightgrey; stroke-mode: plain; stroke-color: #333;}");

        // Adds every node to the graph
        graphMultiStage.vertexSet().forEach(n -> {
            //graph.addNode(n);
            nodes.add(n);
        });

        graphMultiStage.edgeSet().forEach(kpath -> {
            kpath.getKShortestPath().forEach(ksp -> {
                ksp.getNodes().forEach(n -> {
                    //graph.addNode(n);
                    nodes.add(n);
                });
                ksp.getLinks().forEach(link -> {
                    String linkId = link;
                    String nodesId = linkId.substring(0, linkId.indexOf("/"));
                    //String[] ids = nodesId.split("-");

                    links.add(nodesId);

                    //graph.addEdge(nodesId, ids[0], ids[1]);
                });
            });
        });

        // Addd every node to the graph
        nodes.forEach(nodeId -> {
            graph.addNode(nodeId);
        });

        // Adds every edge (link) between neighbour nodes to the graph
        links.forEach(link -> {
            String[] ids = link.split("-");
            graph.addEdge(link, ids[0], ids[1]);
        });

        // Configure the graph to show a label next to every node
        for (org.graphstream.graph.Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }

        graph.display();
    }
}
