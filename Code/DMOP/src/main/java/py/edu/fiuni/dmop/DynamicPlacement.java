package py.edu.fiuni.dmop;

import org.apache.log4j.Logger;
import py.edu.fiuni.dmop.service.DMOPService;

/**
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class DynamicPlacement {

    private static final Logger logger = Logger.getLogger(DynamicPlacement.class);

    public static void main(String args[]) {

        logger.debug("Starting VNF Placement process");
        
        try {
            DMOPService dmopService = new DMOPService();
            
            // Run the solver using One algorithm, 
            // once it gets the ParetoSet, picks the best one using a MultiCriteria Decision Maker
            // Then implement the Winner solution.
            dmopService.runDynamicSolver();
            
        } catch (Exception ex) {
            logger.fatal("ERROR", ex);            
        }
    }
}
