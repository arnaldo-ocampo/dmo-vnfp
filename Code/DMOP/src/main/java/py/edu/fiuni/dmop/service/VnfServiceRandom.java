package py.edu.fiuni.dmop.service;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.moeaframework.core.variable.Permutation;
import py.edu.fiuni.dmop.dto.*;
import py.edu.fiuni.dmop.dto.NFVdto.*;
import py.edu.fiuni.dmop.util.Configurations;

import java.util.*;
import java.util.stream.Collectors;


public class VnfServiceRandom {
    Logger logger = Logger.getLogger(VnfServiceRandom.class);

    private Map<String, List<ShortestPath>> shortestPathMap;
    private DirectedGraph<String, KPath> graphMultiStage;
    private Map<String, Node> nodesMap;
    private Map<String, Link> linksMap;
    private Map<String, VnfShared> vnfSharedMap;


    public boolean placement() {
        TrafficService trafficService = new TrafficService();
        ObjectiveFunctionService ofs = new ObjectiveFunctionService();
        ResultPath resultPath;
        List<Traffic> traffics;
        try {
            shortestPathMap = DataService.shortestPathMap;
            vnfSharedMap = DataService.vnfsShared;

            if (Configurations.trafficsReadFile)
                traffics = trafficService.readTraffics();
            else
                if (Configurations.trafficsRandom)
                    traffics = trafficService.generateRandomTraffic(Configurations.numberOfTraffics, DataService.nodesMap, DataService.vnfs);
                else
                    traffics = trafficService.generateAllToAllTraffic(DataService.nodesMap, DataService.vnfs);

            for (int i = 1; i <= Configurations.numberSolutions; i++) {
                nodesMap = loadNodesMapAux(DataService.nodesMap);
                linksMap = loadLinkMapAux(DataService.linksMap);
                int count = 1;
                logger.info("Tanda: " + i);
                for (Traffic traffic : traffics) {
                    traffic.setRejectLink(0);
                    traffic.setRejectNode(0);
                    graphMultiStage = createGraphtMultiStage(traffic);
                    if (graphMultiStage == null) {
                        traffic.setRejectNode(1);
                        traffic.setProcessed(false);
                        traffic.setResultPath(null);
                        logger.warn(count + "- No Grafo Multi-Estados: " +
                                "origen: " + traffic.getSourceNodeId() + ", destino: " + traffic.getDestinationNodeId());
                    } else {
                        resultPath = provisionTrafficRandom(traffic);
                        traffic.setResultPath(resultPath);
                        if (resultPath == null) {
                            traffic.setProcessed(false);
                            logger.warn(count + "- No Solucion: " +
                                    "origen: " + traffic.getSourceNodeId() + ", destino: " + traffic.getDestinationNodeId());
                        } else {
                            traffic.setProcessed(true);
                            logger.info(count + "- Solucion: " +
                                    "origen: " + traffic.getSourceNodeId() + ", destino: " + traffic.getDestinationNodeId());
                        }
                    }
                    count++;
                }
                ofs.solutionFOs(nodesMap, linksMap, traffics, DataService.vnfsShared);
            }
            logger.info(ofs.solutions);
            ofs.writeSolutions(ofs.solutions);
        } catch (Exception e) {
            logger.error("Error VNF placement: " + e.getMessage());
        }
        return true;
    }

    private DirectedGraph<String, KPath> createGraphtMultiStage(Traffic traffic) throws Exception {
        DirectedGraph<String, KPath> gMStage = new DefaultDirectedGraph<>(KPath.class);
        int numberStages = traffic.getSfc().getVnfs().size();
        List<Node> states = new ArrayList<>();
        List<ShortestPath> kShortestPath;
        String nMSDestinyId, nMSOriginId;
        KPath path;
        Vnf vnf;
        try {
            //Se verifica si existe alguna ruta entre el origin y el destino del trafico
            if (shortestPathMap.get(traffic.getSourceNodeId() + "-" + traffic.getDestinationNodeId()) == null)
                return null;

            //Se guarda en el grafo multi estados el origen y el destino del trafico
            gMStage.addVertex(traffic.getSourceNodeId());
            gMStage.addVertex(traffic.getDestinationNodeId());

            //Se crea enlaces desde el origen a la primera etapa
            vnf = traffic.getSfc().getVnfs().get(0);
            for (Node node : nodesMap.values()) {
                if (node.getServer() != null) {
                    kShortestPath = shortestPathMap.get(traffic.getSourceNodeId() + "-" + node.getId());

                    //Se guardan los nodos con servidor
                    states.add(node);
                    if (isResourceAvailableServer(node.getServer(), vnf)) {
                        //Se cambia la referencia del nodo guardando en otro objeto
                        nMSDestinyId = changeId(node.getId(), 1);
                        if (kShortestPath != null && kShortestPath.size() > 0) {
                            path = new KPath(kShortestPath, traffic.getSourceNodeId() + "-" + nMSDestinyId);

                            //se guarda el nodo en el grafo multiestados con ID = numero de etapa y el id del nodo
                            gMStage.addVertex(nMSDestinyId);

                            //Se crea el enlace del grafo multi estados
                            // que seria el camino (conjunto de IDs de los nodos y enlaces del grafo principal)
                            // entre el par de nodos del grafo multi estados
                            gMStage.addEdge(traffic.getSourceNodeId(), nMSDestinyId, path);

                            // Si el nodo origen es igual al nodo destino
                        } else if (traffic.getSourceNodeId().equals(node.getId())) {
                            kShortestPath = new ArrayList<>();
                            ShortestPath shortestPath = new ShortestPath();
                            shortestPath.getNodes().add(node.getId());
                            kShortestPath.add(shortestPath);
                            path = new KPath(kShortestPath, traffic.getSourceNodeId() + "-" + nMSDestinyId);
                            gMStage.addVertex(nMSDestinyId);
                            gMStage.addEdge(traffic.getSourceNodeId(), nMSDestinyId, path);
                        }
                    }
                }
            }
            //Crear enlaces entre las etapas
            for (int i = 1; i < numberStages; i++) {
                vnf = traffic.getSfc().getVnfs().get(i);
                for (Node nodeOrigin : states) {
                    for (Node nodeDestiny : states) {
                        nMSOriginId = changeId(nodeOrigin.getId(), i);
                        nMSDestinyId = changeId(nodeDestiny.getId(), i + 1);
                        if (isResourceAvailableServer(nodeDestiny.getServer(), vnf) &&
                                gMStage.containsVertex(nMSOriginId)) {
                            kShortestPath = shortestPathMap.get(nodeOrigin.getId() + "-" + nodeDestiny.getId());

                            if (kShortestPath != null && kShortestPath.size() > 0) {
                                path = new KPath(kShortestPath, nMSOriginId + "-" + nMSDestinyId);
                                gMStage.addVertex(nMSDestinyId);
                                gMStage.addEdge(nMSOriginId, nMSDestinyId, path);
                            } else if (nodeOrigin.equals(nodeDestiny)) {
                                kShortestPath = new ArrayList<>();
                                ShortestPath shortestPath = new ShortestPath();
                                shortestPath.getNodes().add(nodeDestiny.getId());
                                kShortestPath.add(shortestPath);
                                path = new KPath(kShortestPath, nMSOriginId + "-" + nMSDestinyId);
                                gMStage.addVertex(nMSDestinyId);
                                gMStage.addEdge(nMSOriginId, nMSDestinyId, path);
                            }
                        }
                    }
                }
            }
            //Crear enlaces entre la ultima etapa y el destino
            for (Node node : states) {
                nMSOriginId = changeId(node.getId(), numberStages);
                if (gMStage.containsVertex(nMSOriginId)) {
                    kShortestPath = shortestPathMap.get(node.getId() + "-" + traffic.getDestinationNodeId());
                    if (kShortestPath != null && kShortestPath.size() > 0) {
                        path = new KPath(kShortestPath, nMSOriginId + "-" + traffic.getDestinationNodeId());
                        gMStage.addEdge(nMSOriginId, traffic.getDestinationNodeId(), path);
                    } else if (node.getId().equals(traffic.getDestinationNodeId())) {
                        kShortestPath = new ArrayList<>();
                        ShortestPath shortestPath = new ShortestPath();
                        shortestPath.getNodes().add(node.getId());
                        kShortestPath.add(shortestPath);
                        path = new KPath(kShortestPath, nMSOriginId + "-" + traffic.getDestinationNodeId());
                        gMStage.addEdge(changeId(node.getId(), numberStages), traffic.getDestinationNodeId(), path);
                    }
                }
            }

            DijkstraShortestPath<String, KPath> dijkstraShortestPath = new DijkstraShortestPath<>(gMStage);
            GraphPath<String, KPath> dijkstra = dijkstraShortestPath
                    .getPath(traffic.getSourceNodeId(), traffic.getDestinationNodeId());

            if (dijkstra == null)
                gMStage = null;

            return gMStage;
        } catch (Exception e) {
            logger.error("No se pudo crear el grafo multi-estados: " + e.getMessage());
            throw new Exception();
        }
    }

    private ResultPath provisionTrafficRandom(Traffic traffic) throws Exception {
        ResultPath resultPath = new ResultPath();
        String randomNodeId, originNodeId;
        Random rn = new Random();
        Map<String, Node> nodesMapAux = null;
        Map<String, Link> linksMapAux = null;
        List<Path> pathNodeIds = null;
        List<String> serverVnf = null;
        List<ShortestPath> kShortestPath;
        double bandwidtCurrent;
        boolean validPlacement = false;
        int retries = 0, indexVnf;
        ShortestPath shortestPath;
        Vnf vnf;
        try {
            while (!validPlacement && retries <= Configurations.retriesSolution) {
                originNodeId = traffic.getSourceNodeId();
                bandwidtCurrent = traffic.getBandwidth();
                nodesMapAux = loadNodesMapAux(this.nodesMap);
                linksMapAux = loadLinkMapAux(this.linksMap);
                pathNodeIds = new ArrayList<>();
                serverVnf = new ArrayList<>();

                retries = retries + 1;
                indexVnf = 0;
                while (!validPlacement) {  //Hasta completar una ruta random
                    //De forma randomica se obtiene un nodo del grafo multi estados
                    Set<KPath> links = graphMultiStage.outgoingEdgesOf(originNodeId);
                    KPath kPath = (KPath) links.toArray()
                            [rn.nextInt(graphMultiStage.outDegreeOf(originNodeId))];
                    kShortestPath = kPath.getKShortestPath();
                    shortestPath = kShortestPath.get(rn.nextInt(kShortestPath.size()));
                    randomNodeId = graphMultiStage.getEdgeTarget(kPath);

                    //la ruta es valida si se llega hasta el nodo destino
                    if (traffic.getDestinationNodeId().equals(randomNodeId)) {
                        if (!isResourceAvailableLink(originNodeId, randomNodeId,
                                bandwidtCurrent, linksMapAux, nodesMapAux, shortestPath, traffic)) {
                            break;
                        } else {
                            validPlacement = true;
                            pathNodeIds.add(new Path(kPath.getId(), shortestPath));
                        }
                    } else {
                        vnf = traffic.getSfc().getVnfs().get(indexVnf);
                        if (!isResourceAvailableGraph(originNodeId, randomNodeId, bandwidtCurrent,
                                vnf, nodesMapAux, linksMapAux, shortestPath, serverVnf, traffic))
                            break;
                        else {
                            bandwidtCurrent = vnfSharedMap.get(vnf.getId()).getBandwidthFactor() * bandwidtCurrent;
                            pathNodeIds.add(new Path(kPath.getId(), shortestPath));
                            originNodeId = randomNodeId;
                            indexVnf = indexVnf + 1;
                        }
                    }
                }
            }
            if (validPlacement) {
                updateGraphMap(nodesMapAux, linksMapAux);
                resultPath.setPaths(pathNodeIds);
                resultPath.setServerVnf(serverVnf);
                return resultPath;
            } else
                return null;
        } catch (Exception e) {
            logger.error("Error en el provisionTraffic:" + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailableGraph(String nodeOriginId, String nodeDestinyId,
                                             double bandwidtCurrent, Vnf vnf, Map<String, Node> nodesMapAux,
                                             Map<String, Link> linksMapAux, ShortestPath shortestPath,
                                             List<String> serverVnf, Traffic traffic) throws Exception {
        int cpuToUse, ramToUse;
        VnfShared vnfToInstall;
        List<VnfShared> vnfsShared;
        double bandwidtUsed;
        Link link;
        try {
            String nodeId = shortestPath.getNodes().get(shortestPath.getNodes().size() - 1);
            Node node = nodesMapAux.get(nodeId);
            Server server = node.getServer();
            if (server != null) {
                //se verifica si el vnf ya esta instalado para poder reutilizar
                vnfsShared = server.getVnfs().get(vnf.getId());
                if (vnfsShared == null) {
                    //Vnf a instalar en el servidor por primera vez
                    vnfToInstall = installVNF(server, vnf);
                    if (vnfToInstall != null) {
                        vnfsShared = new ArrayList<>();
                        vnfsShared.add(vnfToInstall);
                        server.getVnfs().put(vnf.getId(), vnfsShared);
                        serverVnf.add(node.getId());
                    } else {
                        traffic.setRejectNode(traffic.getRejectNode() + 1);
                        return false;
                    }
                } else {
                    //Buscar VNF compartido para reutilizar
                    for (VnfShared vnfShared : vnfsShared) {
                        cpuToUse = vnfShared.getResourceCPUUsed() + vnf.getResourceCPU();
                        ramToUse = vnfShared.getResourceRAMUsed() + vnf.getResourceRAM();
                        if (cpuToUse <= vnfShared.getResourceCPU() && ramToUse <= vnfShared.getResourceRAM()) {
                            vnfShared.setResourceRAMUsed(ramToUse);
                            vnfShared.setResourceCPUUsed(cpuToUse);
                            vnfShared.getVnfs().add(vnf);
                            serverVnf.add(node.getId());
                            return true;
                        }
                    }

                    //Instalar un nuevo VNF porque no existe espacio
                    vnfToInstall = installVNF(server, vnf);
                    if (vnfToInstall != null) {
                        vnfsShared.add(vnfToInstall);
                        server.getVnfs().put(vnf.getId(), vnfsShared);
                        serverVnf.add(node.getId());
                    } else {
                        traffic.setRejectNode(traffic.getRejectNode() + 1);
                        return false;
                    }
                }
            } else {
                traffic.setRejectNode(traffic.getRejectNode() + 1);
                return false;
            }
            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : shortestPath.getLinks()) {
                    link = linksMapAux.get(linkId);
                    bandwidtUsed = link.getBandwidthUsed() + bandwidtCurrent;
                    if (link.getBandwidth() < bandwidtUsed) {
                        traffic.setRejectLink(traffic.getRejectLink() + 1);
                        return false;
                    } else {
                        link.setBandwidthUsed(bandwidtUsed);
                        link.setTrafficAmount(link.getTrafficAmount() + 1);
                    }
                }
                if (shortestPath.getLinks().size() != 0) {
                    for (int i = 0; i < shortestPath.getNodes().size() - 1; i++) {
                        node = nodesMapAux.get(shortestPath.getNodes().get(i));
                        node.setTrafficAmount(node.getTrafficAmount() + 1);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableGraph: " + e.getMessage());
            throw new Exception();
        }
    }

    private VnfShared installVNF(Server server, Vnf vnf) throws Exception {
        int cpuToUse, ramToUse, storageToUse;
        VnfShared vnfToInstall;
        try {
            //Vnf a instalar en el servidor
            vnfToInstall = new VnfShared(vnfSharedMap.get(vnf.getId()));
            cpuToUse = server.getResourceCPUUsed() + vnfToInstall.getResourceCPU();
            ramToUse = server.getResourceRAMUsed() + vnfToInstall.getResourceRAM();
            storageToUse = server.getResourceStorageUsed() + vnfToInstall.getResourceStorage();

            if (cpuToUse <= server.getResourceCPU() && ramToUse <= server.getResourceRAM() &&
                    storageToUse <= server.getResourceStorage()) {
                server.setResourceCPUUsed(cpuToUse);
                server.setResourceRAMUsed(ramToUse);
                server.setResourceStorageUsed(storageToUse);
            } else
                return null;

            cpuToUse = vnfToInstall.getResourceCPUUsed() + vnf.getResourceCPU();
            ramToUse = vnfToInstall.getResourceRAMUsed() + vnf.getResourceRAM();
            if (cpuToUse <= vnfToInstall.getResourceCPU() && ramToUse <= vnfToInstall.getResourceRAM()) {
                vnfToInstall.setResourceRAMUsed(ramToUse);
                vnfToInstall.setResourceCPUUsed(cpuToUse);
                vnfToInstall.getVnfs().add(vnf);
                return vnfToInstall;
            } else
                return null;
        } catch (Exception e) {
            logger.error("Error al instalar VNF: " + e.getMessage());
            throw new Exception();
        }
    }

    private boolean isResourceAvailableLink(String nodeOriginId, String nodeDestinyId, double bandwidtCurrent,
                                            Map<String, Link> linksMapAux, Map<String, Node> nodesMapAux,
                                            ShortestPath shortestPath, Traffic traffic) throws Exception {
        Link link;
        Node node;
        double bandwidtUsed;
        try {
            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : shortestPath.getLinks()) {
                    link = linksMapAux.get(linkId);
                    bandwidtUsed = link.getBandwidthUsed() + bandwidtCurrent;
                    if (link.getBandwidth() < bandwidtUsed) {
                        traffic.setRejectLink(traffic.getRejectLink() + 1);
                        return false;
                    } else {
                        link.setBandwidthUsed(bandwidtUsed);
                        link.setTrafficAmount(link.getTrafficAmount() + 1);
                    }
                }
                if (shortestPath.getLinks().size() != 0) {
                    for (String id : shortestPath.getNodes()) {
                        node = nodesMapAux.get(id);
                        node.setTrafficAmount(node.getTrafficAmount() + 1);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableLink: " + e.getMessage());
            throw new Exception();
        }
    }


    private boolean isResourceAvailableServer(Server server, Vnf vnf) throws Exception {
        int cpuToUse, ramToUse, storageToUse;
        VnfShared vnfToInstall;
        List<VnfShared> vnfsShared;
        try {
            vnfsShared = server.getVnfs().get(vnf.getId());
            if (vnfsShared != null) {
                for (VnfShared vnfShared : vnfsShared) {
                    cpuToUse = vnfShared.getResourceCPUUsed() + vnf.getResourceCPU();
                    ramToUse = vnfShared.getResourceRAMUsed() + vnf.getResourceRAM();

                    if (cpuToUse <= vnfShared.getResourceCPU() && ramToUse <= vnfShared.getResourceRAM())
                        return true;
                }
            }

            vnfToInstall = vnfSharedMap.get(vnf.getId());
            cpuToUse = server.getResourceCPUUsed() + vnfToInstall.getResourceCPU();
            ramToUse = server.getResourceRAMUsed() + vnfToInstall.getResourceRAM();
            storageToUse = server.getResourceStorageUsed() + vnfToInstall.getResourceStorage();

            return cpuToUse <= server.getResourceCPU() && ramToUse <= server.getResourceRAM() &&
                    storageToUse <= server.getResourceStorage();

        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableServer: " + e.getMessage());
            throw new Exception();
        }
    }

    private String changeId(String originalNodeId, int stage) {
        return "s" + stage + originalNodeId;
    }

    private Map<String, Node> loadNodesMapAux(Map<String, Node> nodesMap) throws Exception {
        try {
            return nodesMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new Node(e.getValue())));

        } catch (Exception e) {
            logger.error("Error en loadNodesMapAux: " + e.getMessage());
            throw new Exception();
        }
    }

    private Map<String, Link> loadLinkMapAux(Map<String, Link> linksMap) throws Exception {
        try {
            return linksMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new Link(e.getValue())));

        } catch (Exception e) {
            logger.error("Error en loadLinkMapAux: " + e.getMessage());
            throw new Exception();
        }
    }

    private void updateGraphMap(Map<String, Node> nodesMapAux, Map<String, Link> linksMapAux)
            throws Exception {
        try {
            nodesMap = new HashMap<>(nodesMapAux);
            linksMap = new HashMap<>(linksMapAux);
        } catch (Exception e) {
            logger.error("Error en updateGraphMap: " + e.getMessage());
            throw new Exception();
        }
    }
}

