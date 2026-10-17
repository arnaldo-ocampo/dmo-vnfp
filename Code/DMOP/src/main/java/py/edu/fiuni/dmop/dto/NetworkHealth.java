package py.edu.fiuni.dmop.dto;

import lombok.Data;

/**
 * Metricas de la red despues de implantar una solucion (0 a 1, mayor es mejor).
 */
@Data
public class NetworkHealth {

    //Metricas
    private double saturation;
    private double balance;
    private double efficiency;
    private double links;
    private double qos;

    //Puntaje: promedio de las metricas
    private double score;

    //Datos usados para calcularlas
    private int totalServers;
    private int activeServers;
    private int saturatedServers;
    private double maxLinkUsage;
    private int totalTraffics;
    private int acceptedTraffics;
    private int slaCompliantTraffics;

    //Datos extra, no entran en el puntaje
    private double avgDelay;      // ms
    private double avgCpuUsage;
    private double maxCpuUsage;
    private double maxRamUsage;
}
