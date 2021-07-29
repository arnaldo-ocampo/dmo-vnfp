/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop;

import java.util.Arrays;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.Algorithm;
import py.edu.fiuni.dmop.service.DMOPService;

/**
 *
 * @author Arnaldo
 */
public class VNFPlacement {

    static Logger logger = Logger.getLogger(VNFPlacement.class);

    public static void main(String args[]) {

        logger.debug("Probando si funciona el Logger de eventos");
        System.out.println(Arrays.toString(args));
        
        String app = System.getProperty("app.home");
        System.out.println(app);
                
        //VNFPlacement.class.getResource(app)
        //Algorithm alg = new NSGAII();
        
        try {
            DMOPService dmopService = new DMOPService();
            dmopService.maoeaSolutions();
            
        } catch (Exception ex) {
            logger.fatal("ERROR", ex);            
        }
    }
}
