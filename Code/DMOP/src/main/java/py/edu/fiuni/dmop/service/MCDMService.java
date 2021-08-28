/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.service;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import py.edu.fiuni.dmop.TrafficGenerator;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.util.Configurations;

/**
 *
 * @author Arnaldo
 */
public class MCDMService {

    private static Logger logger = Logger.getLogger(MCDMService.class);

    public MCDMService() {

    }

    public Solution calculateOptimalSolution(NondominatedPopulation result)/* throws DecisionMakerException*/ {

        try {
            List<String> alternatives = new ArrayList<>();
            //double[][] alternatives = new double [][];
            for (Solution solution : result) {
                double[] objectives = solution.getObjectives();
                alternatives.add( Arrays.toString(objectives)); 
                
                //solution.
            }
            
            System.out.println("Alternatives:: " + Arrays.toString( alternatives.toArray()));

            /*
        
        String params = "{ \"\"criteria\"\" : [1, 1, 0], \"\"alternatives\"\" : [[385,6.5,12850],[290,7.5,13695],[210,7.6,12850],[245,6.5,11385],[325,7.55,11235],[235,6.85,12525]], \"\"method\"\": [\"\"topsis\"\", \"\"smart\"\", \"\"promethee\"\", \"\"vikor\"\"]}";

      
            ProcessBuilder pb = new ProcessBuilder(
                    Configurations.pythonInterpreter,
                    Configurations.pythonScript,
                    params, //Params       "D:\\MCO_Library\\MCO_Java_Library-master\\MCO_Java_Library\\src\\decision_logs\\decisions.log" // logging path
                    Configurations.pythonLogPath
            );

            Process p = pb.start();
            p.waitFor();

            Scanner scanner = new Scanner(p.getInputStream());

            System.out.println(".........start   process.........");
            String line = "";
            while ((line = scanner.nextLine()) != null) {
                System.out.println(line);
            }

            System.out.println("........end   process.......");
             */
            // TODO: RETURNING TEMP DEFAULT VALUE
            return result.get(0);

        } catch (Exception e) {
            logger.error("Error on calculating optimal solution", e);
            return null;
        }
    }

}
