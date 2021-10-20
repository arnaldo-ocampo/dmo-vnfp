package py.edu.fiuni.dmop.dto.NFVdto;

import lombok.Data;
import java.io.Serializable;
import py.edu.fiuni.dmop.dto.ResultPath;

/**
 * // TODO: UPDATE INFORMATION
 * Original file from:   github project url
 * @author Arnaldo
 */

@Data
public class Traffic implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // Source node
    private String sourceNodeId;

    // Destination node
    private String destinationNodeId;

    // Initial bandwitdh
    private int bandwidth;

    
    
    
    
    // TODO: Look into SLA, TBD to include or not.
    //Maximo Delay permitido por el trafico de acuerdo al SLA
    private double delayMaxSLA;

    //Costo en dolares por falta del SLA
    private double penaltyCostSLO;

    
    
    
    
    //Cadena de Servicio (Secuencias de VNF)
    private SFC sfc;

    
    
    //Indica si el trafico fue procesado o no
    private boolean processed;

    //Solucion del trafico
    private ResultPath resultPath;

    //Cantidad de rechazos por sobrecarga de enlaces
    private int rejectLink;

    //Cantidad de rechazos por sobrecarga en los nodos
    private int rejectNode;

    /**
     * Copy Constructor
     * @param traffic The traffic to be copied
     */
    public Traffic(Traffic traffic) {
        this.sourceNodeId = traffic.getSourceNodeId();
        this.destinationNodeId = traffic.getDestinationNodeId();
        this.bandwidth = traffic.getBandwidth();
        this.delayMaxSLA = traffic.getDelayMaxSLA();
        this.penaltyCostSLO = traffic.getPenaltyCostSLO();
        this.sfc = traffic.getSfc();
        this.processed = traffic.isProcessed();
        this.resultPath = traffic.getResultPath();
        this.rejectLink = traffic.getRejectLink();
        this.rejectNode = traffic.getRejectNode();
    }

    /**
     *  Default constructor
     */
    public Traffic() { }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Traffic: ");
        sb.append("sourceNodeId=").append(sourceNodeId);
        sb.append(", destinationNodeId=").append(destinationNodeId);
        sb.append(", bandwidth=").append(bandwidth);
        sb.append(", delayMaxSLA=").append(delayMaxSLA);
        sb.append(", penaltyCostSLO=").append(penaltyCostSLO);
        sb.append(", sfc=").append(sfc);
        return sb.toString();
    }
}

