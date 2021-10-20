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

    /**
     *
     * @param numOfTraffics
     * @param nodesMap
     * @param vnfs
     * @return
     * @throws Exception
     */
    public List<Traffic> generateRandomTraffic(int numOfTraffics, Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {

        List<Traffic> traffics = new ArrayList<>();

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

            for (int n = 1; n <= numOfTraffics; n++) {
                Traffic traffic = new Traffic();
                traffic.setBandwidth(rn.nextInt(Configurations.trafficBandwidthMax - Configurations.trafficBandwidthMin + 1) + Configurations.trafficBandwidthMin);
                traffic.setPenaltyCostSLO(rn.nextInt(Configurations.trafficPenaltySloMax - Configurations.trafficPenaltySloMin + 1) + Configurations.trafficPenaltySloMin);
                traffic.setProcessed(false);

                traffic.setSourceNodeId(nodesIdArray[rn.nextInt(nodesSize)]);

                // make sure we are assigning different nodes as origin and destination
                do {
                    traffic.setDestinationNodeId(nodesIdArray[rn.nextInt(nodesSize)]);
                } while (traffic.getSourceNodeId().equals(traffic.getDestinationNodeId()));

                // Ramdonly assign how many vnf needs this traffic
                sfcSize = rn.nextInt(Configurations.trafficSfcMax - Configurations.trafficSfcMin + 1)
                        + Configurations.trafficSfcMin;

                SFC sfc = new SFC();
                for (int k = 0; k < sfcSize; k++) {
                    Vnf vnf = new Vnf(vnfs.get(rn.nextInt(vnfs.size())));
                    sfc.getVnfs().add(vnf);
                }
                traffic.setSfc(sfc);
                traffic.setDelayMaxSLA(getDelayMax(sfc, traffic.getSourceNodeId(), traffic.getDestinationNodeId()));

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

        List<Traffic> traffics = new ArrayList<>();

        Random rn = new Random();
        int sfcSize;
        try {
            for (Node sourceNode : nodesMap.values()) {
                for (Node destinationNode : nodesMap.values()) {
                    if (!destinationNode.getId().equalsIgnoreCase(sourceNode.getId())) {
                        Traffic traffic = new Traffic();
                        traffic.setSourceNodeId(sourceNode.getId());
                        traffic.setDestinationNodeId(destinationNode.getId());
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
                        traffic.setDelayMaxSLA(getDelayMax(sfc, traffic.getSourceNodeId(), traffic.getDestinationNodeId()));

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

    public List<List<Traffic>> generateWindowsTraffics(int[] windowsTrafficsNumber, Map<String, Node> nodesMap, List<Vnf> vnfs) throws Exception {

        List<List<Traffic>> traffics = new ArrayList<List<Traffic>>();

        for (int i = 0; i < windowsTrafficsNumber.length; i++) {
            int number = windowsTrafficsNumber[i];
            traffics.add(generateRandomTraffic(number, nodesMap, vnfs));
        }

        return traffics;
    }

    /**
     *
     * @param traffics
     * @throws Exception
     */
    public void writeTraffics(List<Traffic> traffics) throws Exception {
        writeTraffics(traffics, Configurations.trafficsFileName);
    }

    public void writeTraffics(List<Traffic> traffics, String trafficFileName) throws Exception {
        Gson gson = new Gson();
        try (PrintWriter writer = new PrintWriter(new File(Utility.buildFilePath(Configurations.trafficsFolder + "/" + trafficFileName)))) {
            for (Traffic traffic : traffics) {
                writer.println(gson.toJson(traffic));
            }
        } catch (Exception e) {
            logger.error("Error writting traffics to file " + trafficFileName, e);
            throw e;
        }
    }

    public void writeAllTraffics(List<List<Traffic>> allTraffics) throws Exception {
        String filenamePreffix = "Traffic_";
        int windows = 0;
        for (List<Traffic> theTraffics : allTraffics) {
            writeTraffics(theTraffics, String.format("%s%d.txt", filenamePreffix, windows++));
        }
    }

    /**
     *
     * @return @throws Exception
     */
    public List<Traffic> readTraffics() {
        return readTraffics(Configurations.trafficsFileName);
    }

    public List<Traffic> readTraffics(String trafficFileName) {

        List<Traffic> traffics = new ArrayList<>();

        Gson gson = new Gson();
        try (Scanner reader = new Scanner(new File(Utility.buildFilePath(Configurations.trafficsFolder + "/" + trafficFileName)))) {
            logger.info("Reading traffic from file: " + trafficFileName);
            int i = 0;
            //String strTraffic = null;
            while (reader.hasNext()) {
                String strTraffic = reader.nextLine();
                if (!strTraffic.isBlank()) {
                    traffics.add(gson.fromJson(strTraffic, Traffic.class));
                    logger.info(i++ + " " + strTraffic);
                }
            }
            return traffics;
        } catch (Exception e) {
            logger.error("Error al leer del archivo de traficos", e);
            return null;
        }
    }

    public List<List<Traffic>> readAllTraffics(int numberOfWindows) {

        List<List<Traffic>> traffics = new ArrayList<>();

        String filenamePreffix = "Traffic_";
        int windows = 0;
        while (windows < numberOfWindows) {
            traffics.add(readTraffics(String.format("%s%d.txt", filenamePreffix, windows)));
            windows++;
        }

        return traffics;
    }

    /**
     *
     * @param sfc
     * @param sourceId
     * @param destinationId
     * @return
     */
    private double getDelayMax(SFC sfc, String sourceId, String destinationId) {

        List<GraphPath<Node, Link>> paths;
        double delayMin = 0;

        KShortestPaths<Node, Link> pathInspector
                = new KShortestPaths<>(DataService.graph, 3, Integer.MAX_VALUE);

        // 
        paths = pathInspector.getPaths(DataService.nodesMap.get(sourceId), DataService.nodesMap.get(destinationId));

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
