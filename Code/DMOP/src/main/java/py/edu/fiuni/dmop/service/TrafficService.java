package py.edu.fiuni.dmop.service;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import py.edu.fiuni.dmop.dto.NFVdto.*;
import py.edu.fiuni.dmop.util.Configurations;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import py.edu.fiuni.dmop.util.Utility;

public class TrafficService {

    private static Logger logger = Logger.getLogger(TrafficService.class);

    public static List<Traffic> traffics = new ArrayList<>();

    /**
     *
     * @param nodesMap
     * @param vnfs
     * @return
     * @throws Exception
     */
    public List<Traffic> generateRandomTraffic(Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {
        Random rn = new Random();
        int sfcSize;
        int nodesSize = nodesMap.size();
        String[] nodesIdArray = new String[nodesSize];
        try {
            logger.info("Generating Random Traffic:");

            int i = 0;
            for (Node node : nodesMap.values()) {
                nodesIdArray[i++] = node.getId();
            }

            for (int n = 1; n <= Configurations.numberTraffic; n++) {
                Traffic traffic = new Traffic();
                traffic.setBandwidth(rn.nextInt(Configurations.trafficBandwidthMax - Configurations.trafficBandwidthMin + 1) + Configurations.trafficBandwidthMin);
                traffic.setPenaltyCostSLO(rn.nextInt(Configurations.trafficPenaltySloMax - Configurations.trafficPenaltySloMin + 1) + Configurations.trafficPenaltySloMin);
                traffic.setProcessed(false);

                traffic.setNodeOriginId(nodesIdArray[rn.nextInt(nodesSize)]);

                // make sure we are assigning different nodes as origin and destination
                do {
                    traffic.setNodeDestinyId(nodesIdArray[rn.nextInt(nodesSize)]);
                } while (traffic.getNodeOriginId().equals(traffic.getNodeDestinyId()));

                // Ramdonly assign how many vnf needs this traffic
                sfcSize = rn.nextInt(Configurations.trafficSfcMax - Configurations.trafficSfcMin + 1)
                        + Configurations.trafficSfcMin;

                SFC sfc = new SFC();
                for (int k = 0; k < sfcSize; k++) {
                    Vnf vnf = new Vnf(vnfs.get(rn.nextInt(vnfs.size())));
                    sfc.getVnfs().add(vnf);
                }
                traffic.setSfc(sfc);
                traffic.setDelayMaxSLA(getDelayMax(sfc, traffic.getNodeOriginId(), traffic.getNodeDestinyId()));

                traffics.add(traffic);

                logger.info(n + " " + traffic.toString());
            }

            return traffics;
        } catch (Exception e) {
            logger.error("Error generating random traffic: ", e);
            throw e;
        }
    }

    /**
     *
     * @param nodesMap
     * @param vnfs
     * @return
     * @throws Exception
     */
    public List<Traffic> generateAllToAllTraffic(Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {
        Random rn = new Random();
        int sfcSize;
        try {
            for (Node nodeOrigin : nodesMap.values()) {
                for (Node nodeDestiny : nodesMap.values()) {
                    if (!nodeDestiny.getId().equalsIgnoreCase(nodeOrigin.getId())) {
                        Traffic traffic = new Traffic();
                        traffic.setNodeOriginId(nodeOrigin.getId());
                        traffic.setNodeDestinyId(nodeDestiny.getId());
                        traffic.setBandwidth(rn.nextInt(Configurations.trafficBandwidthMax - Configurations.trafficBandwidthMin + 1) + Configurations.trafficBandwidthMin);
                        traffic.setPenaltyCostSLO(rn.nextInt(Configurations.trafficPenaltySloMax - Configurations.trafficPenaltySloMin + 1) + Configurations.trafficPenaltySloMin);
                        traffic.setProcessed(false);

                        sfcSize = rn.nextInt(Configurations.trafficSfcMax - Configurations.trafficSfcMin + 1) + Configurations.trafficSfcMin;

                        SFC sfc = new SFC();
                        for (int k = 0; k < sfcSize; k++) {
                            Vnf vnf = new Vnf(vnfs.get(rn.nextInt(vnfs.size())));
                            sfc.getVnfs().add(vnf);
                        }
                        traffic.setSfc(sfc);
                        traffic.setDelayMaxSLA(getDelayMax(sfc, traffic.getNodeOriginId(), traffic.getNodeDestinyId()));

                        traffics.add(traffic);
                    }
                }
            }

            return traffics;
        } catch (Exception e) {
            logger.error("Error generating All-to-All Traffic", e);
            throw e;
        }
    }

    /**
     *
     * @param traffics
     * @throws Exception
     */
    public void writeTraffics(List<Traffic> traffics) throws Exception {
        Gson gson = new Gson();
        try (PrintWriter writer = new PrintWriter(new File(Utility.buildFilePath(Configurations.trafficsFileName)))) {
            for (Traffic traffic : traffics) {
                writer.println(gson.toJson(traffic));
            }
        } catch (Exception e) {
            logger.error("Error writting traffics to file", e);
            throw e;
        }
    }

    /**
     *
     * @return @throws Exception
     */
    public static List<Traffic> readTraffics() throws Exception {
        Gson gson = new Gson();
        try (Scanner reader = new Scanner(new File(Utility.buildFilePath(Configurations.trafficsFileName)))) {
            logger.info("Reading traffic from file: ");
            for (int i = 1; i <= Configurations.numberTraffic; i++) {
                String strTraffic = reader.nextLine();
                traffics.add(gson.fromJson(strTraffic, Traffic.class));
                logger.info(i + " " + strTraffic);
            }
            return traffics;
        } catch (Exception e) {
            logger.error("Error al leer del archivo de traficos", e);
            throw e;
        }
    }

    /**
     * 
     * @param sfc
     * @param originId
     * @param destinyId
     * @return 
     */
    private double getDelayMax(SFC sfc, String originId, String destinyId) {

        List<GraphPath<Node, Link>> paths;
        double delayMin = 0;

        KShortestPaths<Node, Link> pathInspector
                = new KShortestPaths<>(DataService.graph, 3, Integer.MAX_VALUE);

        // 
        paths = pathInspector.getPaths(DataService.nodesMap.get(originId), DataService.nodesMap.get(destinyId));

        for (Vnf vnf : sfc.getVnfs()) {
            delayMin = delayMin + DataService.vnfsShared.get(vnf.getId()).getDelay();
        }

        if (paths != null && paths.size() > 0) {
            for (Link link : paths.get(paths.size() - 1).getEdgeList()) {
                delayMin = delayMin + link.getDelay();
            }
        }

        return delayMin + (delayMin * (Configurations.trafficDelayMax / 100));
    }
}
