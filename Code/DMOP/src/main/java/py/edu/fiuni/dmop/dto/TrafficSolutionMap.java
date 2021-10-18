package py.edu.fiuni.dmop.dto;

import java.util.EnumMap;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;


public class TrafficSolutionMap {
    
    //
    private static final EnumMap<ObjectiveFunctionEnum, Double> solutionValuesMap
            = new EnumMap<ObjectiveFunctionEnum, Double>(ObjectiveFunctionEnum.class);
    
    /**
     * 
     * @param ofEnum
     * @param value 
     */
    public void addObjectiveFunctionValue(ObjectiveFunctionEnum ofEnum, Double value){
        this.solutionValuesMap.put(ofEnum, value);
    }
    
    /**
     * 
     * @param ofEnum
     * @return 
     */
    public Double getObjectiveFunctionValueFor(ObjectiveFunctionEnum ofEnum){
        return this.solutionValuesMap.get(ofEnum);
    }
    
    @Override
    public String toString() {
        
        final StringBuilder sb = new StringBuilder("Solution{");
        
        for(ObjectiveFunctionEnum objFuncEnum : solutionValuesMap.keySet()){
            if(sb.length() > 10) sb.append(", ");
            sb.append(objFuncEnum.getPropertyName()).append("=").append(solutionValuesMap.get(objFuncEnum));
        }
        sb.append("}");
        return sb.toString();
    }
}
