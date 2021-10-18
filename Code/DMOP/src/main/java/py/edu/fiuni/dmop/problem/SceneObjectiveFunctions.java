package py.edu.fiuni.dmop.problem;


import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;

/*
    * Latencia                            HAY - Delay Cost
    * Min. Max Carga de Enlace            HAY - Max Use Link(MB)
    * Min. Num de VNF                     HAY - Number Instances
    * Min. Distancia                      HYA - Distance
    * Min. Costo Energia                  HAY - EnergyCost
    * Min. Costo Recursos                 HAY - Resources Cost (cpu, ram, storage)   -- si es cost se minimiza

Normal:
------------
    Latencia
    Min. Max. Carga de Enlace
    Min. Num de VNF
    Min. Distancia 
    Min. Costo Energia
    Min. Costo Recursos

Sobrecarga:
------------
    Latencia
    Min. Max Carga de Enlace
    Min. Num de VNF


Error:
------------
    Latencia    
    Min. Max Carga de Enlace
    Min. Num de VNF
    Max. Hardware Servidor
    Min Num de Servidores Encendidos    
    Min. Tiempo de Recuperacion
*/


public class SceneObjectiveFunctions {

    public static final EnumMap<NetworkConditionEnum, List<ObjectiveFunctionEnum>> SceneMap
            = new EnumMap<NetworkConditionEnum, List<ObjectiveFunctionEnum>>(NetworkConditionEnum.class);
    
    static {
        
        // Objective Functions to be used when the network is working 
        // under Normal conditions
        List<ObjectiveFunctionEnum> normalList = Arrays.asList(
                ObjectiveFunctionEnum.DELAY,
                ObjectiveFunctionEnum.LINK_MAX_USE,              
                //ObjectiveFunctionEnum.VNF_INSTANCES,
                ObjectiveFunctionEnum.DISTANCE,
                ObjectiveFunctionEnum.ENERGY_COST,
                ObjectiveFunctionEnum.RESOURCES_COST
        );
        
        // Objective Functions to be used when the network is working 
        // under Overloaded conditions
        List<ObjectiveFunctionEnum> overloadedList = Arrays.asList(
                ObjectiveFunctionEnum.DELAY,
                ObjectiveFunctionEnum.LINK_MAX_USE/*,                
                ObjectiveFunctionEnum.VNF_INSTANCES*/
        );
        
        // Objective Functions to be used when the network is working 
        // under Error conditions
        List<ObjectiveFunctionEnum> errorList = Arrays.asList(
                ObjectiveFunctionEnum.DELAY,
                ObjectiveFunctionEnum.LINK_MAX_USE
        );
        
        // Objective Functions to be used when the network is working 
        // under Error with Overloaded conditions
        List<ObjectiveFunctionEnum> errorOverloadedList = Arrays.asList(
                ObjectiveFunctionEnum.DELAY,
                ObjectiveFunctionEnum.LINK_MAX_USE
        );
        
        
        SceneMap.put(NetworkConditionEnum.Normal, normalList);
        SceneMap.put(NetworkConditionEnum.Overloaded, overloadedList);
        SceneMap.put(NetworkConditionEnum.Error, errorList);
        SceneMap.put(NetworkConditionEnum.ErrorOverloaded, errorOverloadedList);
    }
}
