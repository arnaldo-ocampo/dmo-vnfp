/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.decision;

import py.edu.fiuni.dmop.decision.topsis.Alternative;

/**
 *
 * @author Arnaldo
 */
public abstract class DecisionMaker {
        
    public abstract Alternative calculateOptimalSolution() throws DecisionMakerException;
    
}
