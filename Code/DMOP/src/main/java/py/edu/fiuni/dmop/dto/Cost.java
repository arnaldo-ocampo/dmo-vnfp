package py.edu.fiuni.dmop.dto;

import lombok.Data;

@Data
public class Cost {

    private String id;
    private double costNormalized;
    private ShortestPath shortestPath;
    
    
    ///////////////////////////////////////////////////////////
    private double delay;               // Prev defined as int 
    private double maximunUseLink;   
    private double numberInstances;     // Prev defined as int 
    private double distance;            // Prev defined as int 
    private double energy;              // Prev defined as int 
    private double resources;
    ///////////////////////////////////////////////////////////
    
    
    private double bandwidth;    
    private double licences;
    private double fragmentation;
        
    public Cost(Cost cost) {
        this.delay = cost.getDelay();
        this.numberInstances = cost.getNumberInstances();
        this.maximunUseLink = cost.getMaximunUseLink();
        this.energy = cost.getEnergy();        
        this.distance = cost.getDistance();
        this.bandwidth = cost.getBandwidth();        
        this.resources = cost.getResources();
        this.licences = cost.getLicences();
        this.fragmentation = cost.getFragmentation();
    }
    
    public Cost() {}
}
