package py.edu.fiuni.dmop.service;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.moeaframework.core.variable.Permutation;
import py.edu.fiuni.dmop.dto.*;
import py.edu.fiuni.dmop.dto.NFVdto.*;

import java.util.*;
import java.util.stream.Collectors;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import py.edu.fiuni.dmop.util.Utility;

public class VnfService {

    private static final Logger logger = Logger.getLogger(VnfService.class);

    private Map<String, List<ShortestPath>> shortestPathMap;
    private DirectedGraph<String, KPath> graphMultiStage;

    private Map<String, Node> nodesMap;
    private Map<String, Link> linksMap;

    private Map<String, VnfShared> vnfSharedMap;

    /**
     *
     * @param traffics
     * @param permutation
     * @return
     */
    public TrafficSolutionMap placement(List<Traffic> traffics, Permutation permutation) {
        try {
            ObjectiveFunctionService ofs = new ObjectiveFunctionService();

            // makes a copy of the nodes in the current network topoloy
            nodesMap = copyNodesMap(DataService.nodesMap);
            // makes a copy of the links in the current network topoloy
            linksMap = copyLinksMap(DataService.linksMap);
            // makes a copy of the traffics received by argument
            traffics = copyTraffics(traffics);

            //
            solution(traffics, permutation);
        
            TrafficSolutionMap solution = ofs.solutionTrafficFOs(nodesMap, linksMap, traffics, DataService.vnfsShared);

            return solution;

        } catch (Exception e) {
            logger.error("Error on VNF placement: ", e);
            return null;
        }
    }

    public ResultGraphMap placementGraph(List<Traffic> traffics, Permutation permutation) {

        try {
            ResultGraphMap resultGraphMap = new ResultGraphMap();

            nodesMap = copyNodesMap(DataService.nodesMap);
            linksMap = copyLinksMap(DataService.linksMap);
            traffics = copyTraffics(traffics);

            solution(traffics, permutation);

            resultGraphMap.setNodesMap(nodesMap);
            resultGraphMap.setLinksMap(linksMap);

            return resultGraphMap;
        } catch (Exception e) {
            logger.error("Error on placementGraph: ", e);
            return null;
        }
    }

    /**
     *
     * @param traffics
     * @param permutation
     */
    public void solution(List<Traffic> traffics, Permutation permutation) {

        try {
            shortestPathMap = DataService.shortestPathMap;
            vnfSharedMap = DataService.vnfsShared;

            int count = 1;
            for (int i = 0; i < permutation.size(); i++) {
                Traffic traffic = traffics.get(permutation.get(i));
                traffic.setRejectLink(0);
                traffic.setRejectNode(0);

                // 
                graphMultiStage = createGraphtMultiStage(traffic);

                if (graphMultiStage == null) {
                    traffic.setRejectNode(1);
                    traffic.setProcessed(false);
                    traffic.setResultPath(null);
                    //    logger.warn(count + "- No Grafo Multi-Estados: " + "origen: " + traffic.getNodeOriginId() + ", destino: " + traffic.getNodeDestinyId());
                } else {
                    //
                    //plotGraphMultiStage(graphMultiStage);

                    ResultPath resultPath = provisionTraffic(traffic);
                    traffic.setProcessed(resultPath != null);
                    traffic.setResultPath(resultPath);

                    /*if (resultPath == null) {
                        traffic.setProcessed(false);
                        //logger.warn(count + "- No Solucion: " + "origen: " + traffic.getNodeOriginId() + ", destino: " + traffic.getNodeDestinyId());
                    } else {
                        traffic.setProcessed(true);
                        //logger.info(count + "- Solucion: " + "origen: " + traffic.getNodeOriginId() + ", destino: " + traffic.getNodeDestinyId());
                    }*/
                }
                count++;
            }
        } catch (Exception e) {
            logger.error("Error VNF placement: " + e.getMessage());
        }
    }

    /**
     *
     * @param traffic
     * @return
     * @throws Exception
     */
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
            if (shortestPathMap.get(String.format("%s-%s", traffic.getSourceNodeId(), traffic.getDestinationNodeId())) == null) {
                return null;
            }

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

                        // TODO: REFACTOR CODIGO PARA SIMPLIFICAR IF ELSE
                        if (kShortestPath != null && !kShortestPath.isEmpty()) {
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
                        if (isResourceAvailableServer(nodeDestiny.getServer(), vnf)
                                && gMStage.containsVertex(nMSOriginId)) {
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

            if (dijkstra == null) {
                gMStage = null;
            }

            return gMStage;
        } catch (Exception e) {
            logger.error("No se pudo crear el grafo multi-estados: " + e.getMessage());
            throw new Exception();
        }
    }

    private ResultPath provisionTraffic(Traffic traffic) throws Exception {

        try {
            ResultPath resultPath = new ResultPath();
            List<Path> pathNodeIds = new ArrayList<>();
            List<String> serverVnf = new ArrayList<>();

            Map<String, Node> nodesMapAux = copyNodesMap(nodesMap);
            Map<String, Link> linksMapAux = copyLinksMap(linksMap);
            
            String originNodeId = traffic.getSourceNodeId();
            double currentBandwidth = traffic.getBandwidth();

            boolean validPlacement = recursion(originNodeId, traffic, currentBandwidth, 0, pathNodeIds, serverVnf, nodesMapAux, linksMapAux);

            if (validPlacement) {
                resultPath.setPaths(pathNodeIds);
                resultPath.setServerVnf(serverVnf);
                return resultPath;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error provisioning Traffic:", e);
            throw e;
        }
    }

    private boolean recursion(String originNodeId, Traffic traffic, double bandwidtCurrent, int indexVnf,
            List<Path> pathNodeIds, List<String> serverVnf,
            Map<String, Node> nodesMap, Map<String, Link> linksMap) throws Exception {
        
        Map<String, Node> nodesMapAux = copyNodesMap(nodesMap);
        Map<String, Link> linksMapAux = copyLinksMap(linksMap);
        
        ShortestPath shortestPath;
        List<Cost> costs;
        String destinyNodeId;
        try {
            //Cuando indexVnf es igual a la cantidad de vnfs, se intenta llegar al nodo destino del trafico
            if (traffic.getSfc().getVnfs().size() == indexVnf) {
                Set<KPath> links = graphMultiStage.outgoingEdgesOf(originNodeId);
                destinyNodeId = graphMultiStage.getEdgeTarget(links.iterator().next());

                if (traffic.getDestinationNodeId().equals(destinyNodeId)) {
                    for (ShortestPath shortestPathLast : links.iterator().next().getKShortestPath()) {
                        if (isResourceAvailableLink(originNodeId, destinyNodeId,
                                bandwidtCurrent, linksMapAux, nodesMapAux, shortestPathLast, traffic)) {
                            pathNodeIds.add(new Path(originNodeId, destinyNodeId, shortestPathLast));
                            updateGraphMap(nodesMapAux, linksMapAux);
                            return true;
                        } else {
                            linksMapAux = copyLinksMap(linksMap);
                        }
                    }
                    return false;
                }
            }
            Vnf vnf = traffic.getSfc().getVnfs().get(indexVnf);
            //Normaliza los costos(Energy, Bandwidth, Delay, Distance, Instances, Resources, Licences, Fragmentation, MaximunUseLink) y guarda en un atributo
            costs = normalizeCosts(vnf, originNodeId, bandwidtCurrent, nodesMapAux, linksMapAux);

            //Se ordena de forma ascendente de acuerdo al valor normalizado
            costs = costs.stream().sorted(Comparator.comparing(Cost::getCostNormalized)).collect(Collectors.toList());

            //Se recorre los nodos destinos de forma ascendente de acuerdo a los costos normalizados
            for (Cost destinyCosts : costs) {
                destinyNodeId = destinyCosts.getId();
                shortestPath = destinyCosts.getShortestPath();

                if (isResourceAvailableGraph(vnf, nodesMapAux, linksMapAux, shortestPath, bandwidtCurrent)) {
                    bandwidtCurrent = vnfSharedMap.get(vnf.getId()).getBandwidthFactor() * bandwidtCurrent;
                    pathNodeIds.add(new Path(originNodeId, destinyNodeId, shortestPath));
                    serverVnf.add(destinyNodeId);
                    originNodeId = destinyNodeId;

                    if (recursion(originNodeId, traffic, bandwidtCurrent, indexVnf + 1, pathNodeIds, serverVnf, nodesMapAux, linksMapAux)) {
                        return true;
                    } else {
                        pathNodeIds.remove(indexVnf);
                        serverVnf.remove(indexVnf);
                    }
                }
                //Si no se puede colocar, se limpian los nodos y links de acuerdo a como estaban al entrar en la recursion
                nodesMapAux = copyNodesMap(nodesMap);
                linksMapAux = copyLinksMap(linksMap);
            }
            return false;
        } catch (Exception e) {
            logger.error("Error on recursion:", e);
            throw e;
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

            if (cpuToUse <= server.getResourceCPU() && ramToUse <= server.getResourceRAM()
                    && storageToUse <= server.getResourceStorage()) {
                server.setResourceCPUUsed(cpuToUse);
                server.setResourceRAMUsed(ramToUse);
                server.setResourceStorageUsed(storageToUse);
            } else {
                return null;
            }

            cpuToUse = vnfToInstall.getResourceCPUUsed() + vnf.getResourceCPU();
            ramToUse = vnfToInstall.getResourceRAMUsed() + vnf.getResourceRAM();
            if (cpuToUse <= vnfToInstall.getResourceCPU() && ramToUse <= vnfToInstall.getResourceRAM()) {
                vnfToInstall.setResourceRAMUsed(ramToUse);
                vnfToInstall.setResourceCPUUsed(cpuToUse);
                vnfToInstall.getVnfs().add(vnf);
                return vnfToInstall;
            } else {
                return null;
            }
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
                if (!shortestPath.getLinks().isEmpty()) {
                    for (String id : shortestPath.getNodes()) {
                        node = nodesMapAux.get(id);
                        node.setTrafficAmount(node.getTrafficAmount() + 1);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error on IsResourceAvailableLink: ", e);
            throw e;
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

                    if (cpuToUse <= vnfShared.getResourceCPU() && ramToUse <= vnfShared.getResourceRAM()) {
                        return true;
                    }
                }
            }
            vnfToInstall = vnfSharedMap.get(vnf.getId());
            cpuToUse = server.getResourceCPUUsed() + vnfToInstall.getResourceCPU();
            ramToUse = server.getResourceRAMUsed() + vnfToInstall.getResourceRAM();
            storageToUse = server.getResourceStorageUsed() + vnfToInstall.getResourceStorage();

            return cpuToUse <= server.getResourceCPU() && ramToUse <= server.getResourceRAM()
                    && storageToUse <= server.getResourceStorage();

        } catch (Exception e) {
            logger.error("Error en IsResourceAvailableServer: " + e.getMessage());
            throw new Exception();
        }
    }

    private String changeId(String originalNodeId, int stage) {
        return "s" + stage + originalNodeId;
    }

    /**
     * Makes a deep copy of the Nodes map received
     *
     * @param nodesMap A Map of Network Graph Nodes
     * @return A new map with a copy of every entry in the given map
     */
    private Map<String, Node> copyNodesMap(Map<String, Node> nodesMap) {
        return nodesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new Node(e.getValue())));
    }

    /**
     * Makes a deep copy of the Links map received
     *
     * @param linksMap A Map of Network Graph Links
     * @return A new map with a copy of every entry in the given map
     */
    private Map<String, Link> copyLinksMap(Map<String, Link> linksMap) {
        return linksMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new Link(e.getValue())));
    }

    /**
     * Makes a deep copy of the Traffics List received
     *
     * @param traffics A List of Network Traffics
     * @return A new List with a copy of every Traffic instance present in the
     * given list
     */
    private List<Traffic> copyTraffics(List<Traffic> traffics) {
        return traffics.stream().map(m -> new Traffic(m)).collect(Collectors.toList());
    }

    private void updateGraphMap(Map<String, Node> nodes, Map<String, Link> links) {
        nodesMap = new HashMap<>(nodes);
        linksMap = new HashMap<>(links);
    }

    // Calcula el costo de todas las funciones objetivos al instalar un VNF
    private Cost calculateCosts(Vnf vnf, ShortestPath shortestPath, double bandwidtCurrent, Map<String, Node> nodesMap, Map<String, Link> linksMap) throws Exception {
        ObjectiveFunctionService ofs = new ObjectiveFunctionService();

        Map<String, Node> nodesMapAux = copyNodesMap(nodesMap);
        Map<String, Link> linksMapAux = copyLinksMap(linksMap);

        boolean result = isResourceAvailableGraph(vnf, nodesMapAux, linksMapAux, shortestPath, bandwidtCurrent);

        if (result) {
            return ofs.costTotalFOs(nodesMapAux, linksMapAux);
        } else {
            return null;
        }
    }

    private boolean isResourceAvailableGraph(Vnf vnf, Map<String, Node> nodesMapAux, Map<String, Link> linksMapAux,
            ShortestPath shortestPath, double bandwidtCurrent) throws Exception {
        VnfShared vnfToInstall;
        int cpuToUse, ramToUse;
        List<VnfShared> vnfsShared;
        double bandwidtUsed;
        Link link;
        try {
            String nodeOriginId = shortestPath.getNodes().get(0);
            String nodeDestinyId = shortestPath.getNodes().get(shortestPath.getNodes().size() - 1);
            Node node = nodesMapAux.get(nodeDestinyId);
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
                    } else {
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
                            return true;
                        }
                    }

                    //Instalar un nuevo VNF porque no existe espacio
                    vnfToInstall = installVNF(server, vnf);
                    if (vnfToInstall != null) {
                        vnfsShared.add(vnfToInstall);
                        server.getVnfs().put(vnf.getId(), vnfsShared);
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
            //Verificar recursos en el enlace que llega al destino del trafico
            if (!nodeDestinyId.equals(nodeOriginId)) {
                for (String linkId : shortestPath.getLinks()) {
                    link = linksMapAux.get(linkId);
                    bandwidtUsed = link.getBandwidthUsed() + bandwidtCurrent;
                    if (link.getBandwidth() < bandwidtUsed) {
                        return false;
                    } else {
                        link.setBandwidthUsed(bandwidtUsed);
                        link.setTrafficAmount(link.getTrafficAmount() + 1);
                    }
                }
                if (!shortestPath.getLinks().isEmpty()) {
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

    
    private List<Cost> normalizeCosts(Vnf vnf,
            String originNodeId, double bandwidtCurrent, Map<String, Node> nodesMap, Map<String, Link> linksMap) throws Exception {
        
        Set<KPath> links = graphMultiStage.outgoingEdgesOf(originNodeId);
        String destinyNodeId;
        ShortestPath shortestPath;
        List<ShortestPath> kShortestPath;
        List<Cost> costs = new ArrayList<>();
        
        for (KPath kPath : links) {
            destinyNodeId = graphMultiStage.getEdgeTarget(kPath);
            kShortestPath = kPath.getKShortestPath();
            for (int k = 0; k < kShortestPath.size(); k++) {
                shortestPath = kShortestPath.get(k);
                Cost resultCost = calculateCosts(vnf, shortestPath, bandwidtCurrent, nodesMap, linksMap);
                if (resultCost != null) {
                    resultCost.setId(destinyNodeId);
                    resultCost.setShortestPath(shortestPath);
                    costs.add(resultCost);
                }
            }
        }

        if (!costs.isEmpty()) {
            double maxEnergy = costs.stream().mapToDouble(Cost::getEnergy)
                    .max().orElseThrow(NoSuchElementException::new);
            double minEnergy = costs.stream().mapToDouble(Cost::getEnergy)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxBandwidth = costs.stream().mapToDouble(Cost::getBandwidth)
                    .max().orElseThrow(NoSuchElementException::new);
            double minBandwidth = costs.stream().mapToDouble(Cost::getBandwidth)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxDelay = costs.stream().mapToDouble(Cost::getDelay)
                    .max().orElseThrow(NoSuchElementException::new);
            double minDelay = costs.stream().mapToDouble(Cost::getDelay)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxDistance = costs.stream().mapToDouble(Cost::getDistance)
                    .max().orElseThrow(NoSuchElementException::new);
            double minDistance = costs.stream().mapToDouble(Cost::getDistance)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxInstances = costs.stream().mapToDouble(Cost::getNumberInstances)
                    .max().orElseThrow(NoSuchElementException::new);
            double minInstances = costs.stream().mapToDouble(Cost::getNumberInstances)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxResources = costs.stream().mapToDouble(Cost::getResources)
                    .max().orElseThrow(NoSuchElementException::new);
            double minResources = costs.stream().mapToDouble(Cost::getResources)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxLicences = costs.stream().mapToDouble(Cost::getLicences)
                    .max().orElseThrow(NoSuchElementException::new);
            double minLicences = costs.stream().mapToDouble(Cost::getLicences)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxFragmentation = costs.stream().mapToDouble(Cost::getFragmentation)
                    .max().orElseThrow(NoSuchElementException::new);
            double minFragmentation = costs.stream().mapToDouble(Cost::getFragmentation)
                    .min().orElseThrow(NoSuchElementException::new);

            double maxMaximunUseLink = costs.stream().mapToDouble(Cost::getMaximunUseLink)
                    .max().orElseThrow(NoSuchElementException::new);
            double minMaximunUseLink = costs.stream().mapToDouble(Cost::getMaximunUseLink)
                    .min().orElseThrow(NoSuchElementException::new);

            costs.forEach(cost -> {
                double normalizedEnergy = Utility.normalizeValue(cost.getEnergy(), maxEnergy, minEnergy);
                double normalizedBandwidth = Utility.normalizeValue(cost.getBandwidth(), maxBandwidth, minBandwidth);
                double normalizedDelay = Utility.normalizeValue(cost.getDelay(), maxDelay, minDelay);
                double normalizedDistance = Utility.normalizeValue(cost.getDistance(), maxDistance, minDistance);
                double normalizedInstances = Utility.normalizeValue(cost.getNumberInstances(), maxInstances, minInstances);
                double normalizedResources = Utility.normalizeValue(cost.getResources(), maxResources, minResources);
                double normalizedLicences = Utility.normalizeValue(cost.getLicences(), maxLicences, minLicences);
                double normalizedFragmentation = Utility.normalizeValue(cost.getFragmentation(), maxFragmentation, minFragmentation);
                double normalizedMaximunUseLink = Utility.normalizeValue(cost.getMaximunUseLink(), maxMaximunUseLink, minMaximunUseLink);

                double costNormalized = normalizedEnergy + normalizedBandwidth + normalizedDelay
                        + normalizedDistance + normalizedInstances + normalizedResources + normalizedLicences
                        + normalizedFragmentation + normalizedMaximunUseLink;

                cost.setCostNormalized(costNormalized / 9);
            });
        }

        return costs;
    }
}
