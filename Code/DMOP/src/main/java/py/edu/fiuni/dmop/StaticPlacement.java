package py.edu.fiuni.dmop;

import org.apache.log4j.Logger;
import py.edu.fiuni.dmop.service.SMOPService;

public class StaticPlacement {
    
    private static final Logger logger = Logger.getLogger(StaticPlacement.class);
    
    public static void main(String[] args) throws Exception {
        
        try {
            SMOPService smopService = new SMOPService();
            
            // Run the solver for n rounds with different algorithms
            // to find the Virtual Network Functions locations
            // and evaluate de quality of the Pareto Set
            // using the MOEAFramework Analyzer
            smopService.runSolutionsAnalyzer();
            
            // Run the solver using One algorithm, 
            // to find the Virtual Network Functions locations
            // once it gets the ParetoSet, picks the best one using a MultiCriteria Decision Maker
            // Then implement the Winner solution.
            //smopService.runSolutionDeployer();
            
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }    
}
