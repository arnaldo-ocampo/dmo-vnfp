package py.edu.fiuni.dmop.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import py.edu.fiuni.dmop.dto.NFVdto.Link;
import py.edu.fiuni.dmop.dto.NFVdto.Node;
import py.edu.fiuni.dmop.dto.NFVdto.Server;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.NFVdto.Vnf;
import py.edu.fiuni.dmop.dto.NetworkHealth;
import py.edu.fiuni.dmop.dto.Path;
import py.edu.fiuni.dmop.dto.ResultGraphMap;
import py.edu.fiuni.dmop.util.Constants;

/**
 * Calcula las metricas de la red despues de implantar una solucion.
 */
public class NetworkStateService {

    public NetworkHealth evaluate(ResultGraphMap network) {

        NetworkHealth health = new NetworkHealth();

        List<Server> servers = network.getNodesMap().values().stream()
                .map(Node::getServer).filter(s -> s != null).collect(Collectors.toList());
        List<Server> active = servers.stream()
                .filter(s -> s.getResourceCPUUsed() > 0).collect(Collectors.toList());

        //Servidores encendidos por encima del umbral
        long saturated = active.stream().filter(s -> usage(s) > Constants.SATURATION_THRESHOLD).count();
        health.setSaturation(active.isEmpty() ? 1 : 1 - (double) saturated / active.size());

        //Reparto de la carga entre todos los servidores
        health.setBalance(normalizedJain(servers.stream().mapToDouble(NetworkStateService::usage).toArray()));

        //Servidores encendidos
        health.setEfficiency(servers.isEmpty() ? 1 : 1 - (double) active.size() / servers.size());

        //Enlace mas cargado
        double maxLinkUsage = network.getLinksMap().values().stream()
                .mapToDouble(l -> l.getBandwidthUsed() / l.getBandwidth()).max().orElse(0);
        health.setLinks(1 - maxLinkUsage);

        //Traficos atendidos y que cumplen el SLA
        List<Traffic> traffics = network.getTraffics();
        List<Traffic> accepted = traffics.stream().filter(Traffic::isProcessed).collect(Collectors.toList());
        long slaCompliant = accepted.stream()
                .filter(t -> delayOf(t, network.getLinksMap()) <= t.getDelayMaxSLA()).count();
        double acceptance = traffics.isEmpty() ? 1 : (double) accepted.size() / traffics.size();
        double slaCompliance = accepted.isEmpty() ? 0 : (double) slaCompliant / accepted.size();
        health.setQos((acceptance + slaCompliance) / 2);

        health.setScore((health.getSaturation() + health.getBalance() + health.getEfficiency()
                + health.getLinks() + health.getQos()) / 5);

        health.setTotalServers(servers.size());
        health.setActiveServers(active.size());
        health.setSaturatedServers((int) saturated);
        health.setMaxLinkUsage(maxLinkUsage);
        health.setTotalTraffics(traffics.size());
        health.setAcceptedTraffics(accepted.size());
        health.setSlaCompliantTraffics((int) slaCompliant);

        //Datos extra, no entran en el puntaje
        health.setAvgDelay(accepted.stream().mapToDouble(t -> delayOf(t, network.getLinksMap())).average().orElse(0));
        health.setAvgCpuUsage(active.stream().mapToDouble(NetworkStateService::cpuUsage).average().orElse(0));
        health.setMaxCpuUsage(servers.stream().mapToDouble(NetworkStateService::cpuUsage).max().orElse(0));
        health.setMaxRamUsage(servers.stream().mapToDouble(NetworkStateService::ramUsage).max().orElse(0));

        return health;
    }

    //Uso del servidor: el mayor entre CPU y RAM
    private static double usage(Server server) {
        return Math.max(cpuUsage(server), ramUsage(server));
    }

    private static double cpuUsage(Server server) {
        return (double) server.getResourceCPUUsed() / server.getResourceCPU();
    }

    private static double ramUsage(Server server) {
        return (double) server.getResourceRAMUsed() / server.getResourceRAM();
    }

    //Indice de Jain llevado a 0..1 (1 = carga pareja)
    private static double normalizedJain(double[] loads) {
        int n = loads.length;
        double sum = 0, sumSquares = 0;
        for (double x : loads) {
            sum += x;
            sumSquares += x * x;
        }
        if (n <= 1 || sumSquares == 0) {
            return 1;
        }
        double jain = (sum * sum) / (n * sumSquares);
        return (jain - 1.0 / n) / (1 - 1.0 / n);
    }

    //Delay del trafico: enlaces del camino + procesamiento de cada VNF (igual que calculateSLOCost)
    private static double delayOf(Traffic traffic, Map<String, Link> linksMap) {
        double delay = 0;
        for (Path path : traffic.getResultPath().getPaths()) {
            for (String linkId : path.getShortestPath().getLinks()) {
                delay += linksMap.get(linkId).getDelay();
            }
        }
        for (Vnf vnf : traffic.getSfc().getVnfs()) {
            delay += DataService.vnfsShared.get(vnf.getId()).getDelay();
        }
        return delay;
    }
}
