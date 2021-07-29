package py.edu.fiuni.dmop.dto;

import lombok.Data;

@Data
public class Cost {

    private String id;
    private double costNormalized;
    private ShortestPath shortestPath;
    
    
    // FIRST ATTEMPT //
    private int delay;    
    private int numberInstances;
    private double maximunUseLink;
    
    // FOR LATER USE ONLY //
    private double energy;    
    private int distance;
    private double bandwidth;    
    private double resources;
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

    public Cost() {
    }
}
