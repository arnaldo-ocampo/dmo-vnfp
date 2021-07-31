/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.decision;

/**
 *
 * @author Néstor
 */
public class DecisionMakerException extends Exception{
    
    public DecisionMakerException(){
        super("Error al intentar objetener una solucion no dominada");
    }
    
    public DecisionMakerException(String msg){
        super(msg);
    }
    
    
}
