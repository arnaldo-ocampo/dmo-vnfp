package py.edu.fiuni.dmop.service;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.SimpleGraph;
import py.edu.fiuni.dmop.dto.ShortestPath;
import py.edu.fiuni.dmop.dto.NFVdto.*;
import py.edu.fiuni.dmop.util.Configurations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import py.edu.fiuni.dmop.dto.NFVdto.Node;
import py.edu.fiuni.dmop.util.Utility;

public class DataService {

    private static Logger logger = Logger.getLogger(DataService.class);

    private static List<String> linksString;
    public static final Graph<Node, Link> graph = new SimpleGraph<>(Link.class);
    public static Map<String, List<ShortestPath>> shortestPathMap = new HashMap<>();
    public static Map<String, Node> nodes = new HashMap<>();
    public static Map<String, Node> nodesMap = new HashMap<>();
    public static Map<String, Link> linksMap = new HashMap<>();
    public static Map<String, VnfShared> vnfsShared = new HashMap<>();
    public static List<Vnf> vnfs = new ArrayList<>();
    public static Map<String, Server> servers = new HashMap<>();

    public static void loadData() throws Exception {
        try {
            logger.info("Loading initial data: ");
            loadVnfsShared();
            loadVnfs();
            loadServers();
            loadNodes();
            loadLinks();
            loadGraph();
            kShortestPath();
        } catch (Exception e) {
            logger.error("Error loading initial data: ", e);
            throw e;
        }
    }

    private static void loadVnfsShared() throws Exception {

        String strVNF = null;
        String[] vnfSplit;
        logger.info("VNFs: ");
        try (BufferedReader reader = new BufferedReader(new FileReader(Utility.buildFilePath(Configurations.vnfsShareFileName)))) {

            int i = 1;
            reader.readLine();
            while ((strVNF = reader.readLine()) != null) {
                vnfSplit = strVNF.split(" ");

                VnfShared vnfShared = new VnfShared();
                vnfShared.setId(vnfSplit[0]);
                vnfShared.setDelay(Integer.parseInt(vnfSplit[1]));
                vnfShared.setDeploy(Integer.parseInt(vnfSplit[2]));
                vnfShared.setLicenceCost(Integer.parseInt(vnfSplit[3]));
                vnfShared.setBandwidthFactor(Double.parseDouble(vnfSplit[4]));
                vnfShared.setResourceCPU(Integer.parseInt(vnfSplit[5]));
                vnfShared.setResourceRAM(Integer.parseInt(vnfSplit[6]));
                vnfShared.setResourceStorage(Integer.parseInt(vnfSplit[7]));

                vnfsShared.put(vnfShared.getId(), vnfShared);
                logger.info(i++ + " " + vnfShared.toString());
            }

        } catch (NumberFormatException e) {
            logger.error("Error parsing VNFs data: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error on loading Shated VNFs data:", e);
            throw e;
        }
    }

    private static void loadVnfs() throws Exception {

        String[] vnfSplit;
        logger.info("VNFs: ");
        try (BufferedReader reader = new BufferedReader(new FileReader(Utility.buildFilePath(Configurations.vnfsSfcFileName)))) {

            int i = 1;
            // read first line (header)
            String vnfLine = reader.readLine();
            while ((vnfLine = reader.readLine()) != null) {
                vnfSplit = vnfLine.split(" ");

                Vnf vnf = new Vnf(vnfSplit[0], vnfSplit[1], Integer.parseInt(vnfSplit[2]), Integer.parseInt(vnfSplit[3]));
                vnfs.add(vnf);
                logger.info(i++ + " " + vnf.toString());
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los VNFs: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los VNFs:", e);
            throw e;
        }
    }

    private static void loadServers() throws Exception {

        String[] serverSplit;
        logger.info("Servidores: ");
        try (BufferedReader reader = new BufferedReader(new FileReader(Utility.buildFilePath(Configurations.networkPackage + Configurations.serversFileName)))) {

            int i = 1;
            // Read first line from the file (header)
            String serverLine = reader.readLine();
            while ((serverLine = reader.readLine()) != null) {
                serverSplit = serverLine.split(" ");

                Server server = new Server();
                server.setId(serverSplit[0].trim());
                server.setLicenceCost(Integer.parseInt(serverSplit[1]));
                server.setDeploy(Integer.parseInt(serverSplit[2]));
                server.setResourceCPU(Integer.parseInt(serverSplit[3]));
                server.setResourceRAM(Integer.parseInt(serverSplit[4]));
                server.setResourceStorage(Integer.parseInt(serverSplit[5]));
                server.setResourceCPUCost(Double.parseDouble(serverSplit[6]));
                server.setResourceRAMCost(Double.parseDouble(serverSplit[7]));
                server.setResourceStorageCost(Double.parseDouble(serverSplit[8]));
                server.setEnergyIdleWatts(Integer.parseInt(serverSplit[9]));
                server.setEnergyPeakWatts(Integer.parseInt(serverSplit[10]));

                servers.put(server.getId(), server);
                logger.info(i++ + " " + server.toString());
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los Servidores: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los Servidores: ", e);
            throw e;
        }
    }

    private static void loadNodes() throws Exception {

        String[] splitNode;
        logger.info("Nodos: ");
        try (BufferedReader reader = new BufferedReader(new FileReader(Utility.buildFilePath(Configurations.networkPackage + Configurations.nodesFileName)))) {

            int i = 1;
            // Read first line from the file (header)
            String nodeString = reader.readLine();
            while ((nodeString = reader.readLine()) != null) {
                splitNode = nodeString.split(" ");

                Node node = new Node();
                node.setId(splitNode[0].trim());
                node.setEnergyCost(Double.parseDouble(splitNode[1].trim()));
                node.setServer(servers.get(splitNode[2].trim()));

                nodesMap.put(node.getId(), node);
                nodes.put(node.getId(), node);
                logger.info(i++ + " " + node.toString());
            }

        } catch (NumberFormatException e) {
            logger.error("Error al parsear los datos de los Nodos: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error al cargar los datos de los Nodos: ", e);
            throw e;
        }
    }

    private static void loadLinks() throws Exception {

        try (BufferedReader reader = new BufferedReader(new FileReader(Utility.buildFilePath(Configurations.networkPackage + Configurations.linksFileName)))) {

            String linkLine = reader.readLine();
            linksString = new ArrayList<>();
            while ((linkLine = reader.readLine()) != null) {
                linksString.add(linkLine.trim());
            }
        } catch (Exception e) {
            logger.error("Error on loading loading: ", e);
            throw e;
        }
    }

    private static void loadGraph() throws Exception {
        try {
            for (Node node : nodes.values()) {
                graph.addVertex(node);
            }

            logger.info("Enlaces: ");
            int i = 1;
            for (String linkString : linksString) {
                String[] linkSplit = linkString.split(" ");

                Link link = new Link();
                link.setId(linkSplit[0] + "-" + linkSplit[1] + "/" + linkSplit[1] + "-" + linkSplit[0]);
                link.setDelay(Integer.parseInt(linkSplit[2]));
                link.setDistance(Integer.parseInt(linkSplit[3]));
                link.setBandwidth(Double.parseDouble(linkSplit[4]));
                link.setBandwidthCost(Double.parseDouble(linkSplit[5]));

                linksMap.put(link.getId(), link);
                logger.info(i++ + " " + link.toString());
                graph.addEdge(nodes.get(linkSplit[0]), nodes.get(linkSplit[1]), link);
            }
            logger.info("Graph: ");
            logger.info(graph.toString());
        } catch (Exception e) {
            logger.error("Error on loading graph: ", e);
            throw e;
        }
    }

    /**
     * Obtiene los k caminos mas cortos(por cantidad de saltos), de cada par de
     * nodos
     *
     * @throws Exception
     */
    private static void kShortestPath() throws Exception {
        try {
            KShortestPaths<Node, Link> pathInspector
                    = new KShortestPaths<>(graph, Configurations.k, Integer.MAX_VALUE);
            List<GraphPath<Node, Link>> paths;
            List<ShortestPath> kShortestPath;
            String nodeString, linkString;
            for (Node origin : nodes.values()) {
                for (Node destiny : nodes.values()) {
                    if (!origin.equals(destiny)) {
                        kShortestPath = new ArrayList<>();
                        //Obtiene los k caminos mas cortos entre dos pares de nodos
                        paths = pathInspector.getPaths(origin, destiny);
                        for (GraphPath<Node, Link> path : paths) {
                            ShortestPath shortestPath = new ShortestPath();
                            // Guarda el ID de la lista de nodos de los k caminos mas cortos
                            for (Node node : path.getVertexList()) {
                                nodeString = node.getId();
                                shortestPath.getNodes().add(nodeString);
                            }
                            // Guarda el ID de la lista de enlaces de los k caminos mas cortos
                            for (Link link : path.getEdgeList()) {
                                linkString = link.getId();
                                shortestPath.getLinks().add(linkString);
                            }
                            kShortestPath.add(shortestPath);
                        }
                        //Se guarda en un Map los k caminos mas cortos de cada para de nodos del grafo
                        // donde el key es el origenId - destinoId
                        shortestPathMap.put(origin.getId() + "-" + destiny.getId(), kShortestPath);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error on loading kShortestPath: ", e);
            throw e;
        }
    }
}
