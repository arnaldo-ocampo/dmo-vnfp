package py.edu.fiuni.dmop.service;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.log4j.Logger;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import py.edu.fiuni.dmop.TrafficGenerator;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;
import py.edu.fiuni.dmop.decision.topsis.Topsis;
import py.edu.fiuni.dmop.problem.SceneObjectiveFunctions;
import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;

/**
 *
 * @author Arnaldo
 */
public class MCDMService {

    private static Logger logger = Logger.getLogger(MCDMService.class);

    public MCDMService() {

    }

    /**
     * 
     * @param result
     * @param networkCondition
     * @return
     * @throws DecisionMakerException 
     */
    public Solution calculateOptimalSolution(NondominatedPopulation result, NetworkConditionEnum networkCondition) throws DecisionMakerException {

        List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

        Topsis topsis = new Topsis();

        // Use the same weight for all of the criterias 
        // (every objective function has the same priority)
        double weight = 1.0 / sceneObjectives.size();

        List<Criteria> criteriaList = new ArrayList<>();
        for (ObjectiveFunctionEnum objFuncEnum : sceneObjectives) {
            criteriaList.add(new Criteria(objFuncEnum.getName(), weight, objFuncEnum.isMinimizable()));
        }

        for (int solIndex = 0; solIndex < result.size(); solIndex++) {
            Solution solution = result.get(solIndex);

            Alternative alt = new Alternative(Integer.toString(solIndex));

            for (int crIndex = 0; crIndex < criteriaList.size(); crIndex++) {
                Criteria criteria = criteriaList.get(crIndex);
                alt.addCriteriaValue(criteria, solution.getObjective(crIndex));
            }

            topsis.addAlternative(alt);
        }

        Alternative topsisOptimal = topsis.calculateOptimalSolution();
        Solution best = result.get(Integer.parseInt(topsisOptimal.getName()));

        topsis.printDetailedResults();
        System.out.println("The optimal solution is: " + topsisOptimal.getName());
        System.out.println("The optimal solution score is: " + topsisOptimal.getCalculatedPerformanceScore());
   
        return best;

    }

    /*
    public Solution calculateOptimalSolution(NondominatedPopulation result) throws DecisionMakerException {

        try {    
            //Criteria bandwidth = new Criteria("Bandwidth", 0.4);
            //Criteria resources = new Criteria("Resources", 0.3);
            //Criteria energy = new Criteria("Energy", 0.3, true);    
            List<String> alternatives = new ArrayList<>();
            //double[][] alternatives = new double [][];
            for (Solution solution : result) {
                double[] objectives = solution.getObjectives();
                alternatives.add( Arrays.toString(objectives));                 
                //solution
            }            
            System.out.println("Alternatives:: " + Arrays.toString( alternatives.toArray()));
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
            while ((line = scanner.nextLine()) != null) { System.out.println(line); }
            System.out.println("........end   process.......");             
        } catch (Exception e) {
            logger.error("Error on calculating optimal solution", e);
            return null;
        }
    }*/
}
