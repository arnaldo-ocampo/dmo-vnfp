package py.edu.fiuni.dmop.util;

/**
 *
 * @author Arnaldo
 */
public enum ObjectiveFunctionEnum {
    
    // CURRENTLY IN USE BY THE DIFFERENT USE CASES
    DELAY("Delay", "delayCost", true),
    LINK_MAX_USE("Link Max Use", "maxUseLink", true),
    SERVICE_AVAILABILITY("Service Availability", "sloCost", true),
    VNF_INSTANCES("VNF Instances", "numberInstances", true),
    RESOURCES_COST("Resource Cost", "resourcesCost", true),
    RECOVERY_TIME("Recovery Time", "recoveryTime", true),
    ENERGY_COST("Energy Cost", "energyCost", true),
    
    // CURRENTLY NOT USING
    DISTANCE("Distance", "distance", true),
    BANDWIDTH("Bandwidth", "bandwidth", true),
    TRAFFIC_LOAD("Traffic Load", "loadTraffic", true),
    LICENSE_COST("License Cost", "licencesCost", true),
    THROUGHPUT("Throughput", "throughput", true),
    FRAGMENTATION("Fragmentation", "fragmentation", true);
    
    ObjectiveFunctionEnum(String name, String propertyName, boolean minimize){
        this.name = name;
        this.propertyName = propertyName;
        this.minimize = minimize;
    }
    
    public String getName(){
        return this.name;
    }
    
    public String getPropertyName(){
        return this.propertyName;
    }
    
    public boolean isMinimizable(){
        return this.minimize;
    }
    
    private String name = null;
    private String propertyName = null;
    private boolean minimize = true;
}
