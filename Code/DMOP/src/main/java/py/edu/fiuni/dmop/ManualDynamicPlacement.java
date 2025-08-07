package py.edu.fiuni.dmop;

import org.apache.log4j.Logger;
import org.moeaframework.core.spi.AlgorithmFactory;

import py.edu.fiuni.dmop.algorithm.DynamicAlgorithmsProvider;
import py.edu.fiuni.dmop.service.DMOPService;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.MCDMService;
import py.edu.fiuni.dmop.service.SolutionService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.util.Configurations;

/**
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class ManualDynamicPlacement {

    private static final Logger logger = Logger.getLogger(ManualDynamicPlacement.class);

    
    public static void main(String args[]) {

        try {
            AlgorithmFactory.getInstance().addProvider(new DynamicAlgorithmsProvider());

            Configurations.loadProperties();
            DataService.loadData();
            
            TrafficService trafficeService = new TrafficService();
            SolutionService solutionService = new SolutionService();
            MCDMService decisionService = new MCDMService();
            
            DMOPService dmopService = new DMOPService(solutionService, trafficeService);
            
            String[] algorithms = {"DNSGAII-A", "DNSGAII-B", "NSGAIII","MOEAD", "RVEA"};
           
            // 
            //dmopService.runSolver();
            
            // Create files to be used by Charts generator
            solutionService.generateCSVFiles(algorithms, decisionService);

        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }
}
