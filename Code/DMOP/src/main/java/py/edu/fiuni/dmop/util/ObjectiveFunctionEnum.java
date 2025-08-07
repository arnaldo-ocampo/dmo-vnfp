package py.edu.fiuni.dmop.util;

/**
 *
 * @author Arnaldo
 */
public enum ObjectiveFunctionEnum {
    
    // CURRENTLY IN USE BY THE DIFFERENT USE CASES
    DELAY("Latencia", "delayCost", true),
    LINK_MAX_USE("Máxima_Carga_de_Enlace", "maxUseLink", true),
    DISTANCE("Distancia", "distance", true),
    VNF_INSTANCES("Núm._VNFs", "numberInstances", true),
    ENERGY_COST("Costo_de_Energia", "energyCost", true),
    RESOURCES_COST("Costo_de_Recursos", "resourcesCost", true),
    
    // CURRENTLY NOT USING
    SERVICE_AVAILABILITY("Disponibilidad_de_Servicio", "sloCost", true),
    RECOVERY_TIME("Recovery Time", "recoveryTime", true),
    BANDWIDTH("Bandwidth", "bandwidth", true),
    TRAFFIC_LOAD("Traffic Load", "loadTraffic", true),
    LICENSE_COST("License Cost", "licencesCost", true),
    THROUGHPUT("Throughput", "throughput", true),
    FRAGMENTATION("Fragmentation", "fragmentation", true);
    
    
    /**
     * 
     * @param name The name of the enumerable, will use in Spanish as will be use to generate charts for every objective function
     * @param propertyName
     * @param minimize 
     */
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
