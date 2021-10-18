package py.edu.fiuni.dmop.decision;

import py.edu.fiuni.dmop.decision.topsis.Alternative;

/**
 * Super class for all of the MultiCriteria Decision Maker algorithms
 * 
 * @author Arnaldo Ocampo, Néstor Tapia
 */
public abstract class DecisionMaker {
        
    /**
     * Calculate the best possible solution given a Pareto Set
     * @return The best alternative resulting from applying a specific algorithm
     * @throws DecisionMakerException 
     */
    public abstract Alternative calculateOptimalSolution() throws DecisionMakerException;
    
}
