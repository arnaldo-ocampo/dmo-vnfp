package py.edu.fiuni.dmop.problem;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.util.Vector;
import org.moeaframework.util.io.CommentedLineReader;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.moeaframework.core.variable.Permutation;
import py.edu.fiuni.dmop.StaticPlacement;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.SolutionTraffic;
import py.edu.fiuni.dmop.dto.TrafficSolutionMap;
import py.edu.fiuni.dmop.service.DataService;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.service.VnfService;
import py.edu.fiuni.dmop.util.Configurations;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;

public class DynamicVNFPlacementProblem extends AbstractDynamicProblem {

    private static final Logger logger = Logger.getLogger(DynamicVNFPlacementProblem.class);

    private final TrafficService trafficService;
    private List<List<Traffic>> allTraffics;
    private List<Traffic> currentTraffics;
    private NetworkConditionEnum networkCondition;

    private int lowerLimit = 0;
    private int upperLimit = 0;
    private int normalUpperLimit = 0;

    private boolean randomTrafficsGeneration = false;

    private int currentWindows = 0;

    public DynamicVNFPlacementProblem(Properties properties, TrafficService trafficService, List<List<Traffic>> allTraffics, int lower, int upper, int normalUpper, boolean randomTrafficsGen) {
        super((Double) properties.get("severityOfChange"), (Integer) properties.get("frequencyOfChange"));
        this.trafficService = trafficService;
        this.allTraffics = allTraffics;

        lowerLimit = lower;
        upperLimit = upper;
        normalUpperLimit = normalUpper;

        randomTrafficsGeneration = randomTrafficsGen;

        if (randomTrafficsGeneration) {
            this.currentTraffics = this.trafficService.readTraffics();
        } else {
            this.currentTraffics = allTraffics.get(currentWindows);
        }

        this.networkCondition = currentTraffics.size() > normalUpperLimit ? NetworkConditionEnum.Overloaded : NetworkConditionEnum.Normal;
    }


    //@Override
    /*protected*/ public void changeEnvironment() {
        /*for(int i=0;i<profit.length;i++){
            for(int j=0;j<profit[i].length;j++){
                // Unit Change :  +-%1
                // Total Change : severityOfChange * Unit Change
                double unitChange = ((Math.random()/50)-0.01);
                profit[i][j] += profit[i][j] * unitChange * severityOfChange;
            }
        }*/

        try {

            this.currentWindows++;
            
            if (randomTrafficsGeneration) {

                Random rn = new Random();

                int newTrafficNumber = rn.nextInt(upperLimit - lowerLimit + 1) + lowerLimit;
                List<Traffic> newTraffics = trafficService.generateRandomTraffic(newTrafficNumber, DataService.nodesMap, DataService.vnfs);

                this.currentTraffics = newTraffics;
            }else{
               this.currentTraffics = allTraffics.get(currentWindows);               
            }
            
            this.networkCondition = currentTraffics.size() > normalUpperLimit ? NetworkConditionEnum.Overloaded : NetworkConditionEnum.Normal;

            logger.info(String.format("Environment has changed. Windows #%d, Traffics #%d, NetCondition: %s", this.currentWindows, this.currentTraffics.size(), this.networkCondition.toString()));

        } catch (Exception ex) {
            System.out.println("Fatal Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void evaluate(Solution solution) {

        VnfService vnfService = new VnfService();

        double[] objectives = new double[getNumberOfObjectives()];
        Permutation permutation = (Permutation) solution.getVariable(0);

        TrafficSolutionMap solutionsMap = vnfService.placement(this.currentTraffics, permutation);

        List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);

        for (int foIndex = 0; foIndex < this.getNumberOfObjectives(); foIndex++) {
            objectives[foIndex] = solutionsMap.getObjectiveFunctionValueFor(sceneObjectives.get(foIndex));
        }

        solution.setObjectives(objectives);

        //TODO: Commented while used in manual mode
        //super.evaluate(solution);
    }

    @Override
    public String getName() {
        return "DynVNFPlacement";
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return SceneObjectiveFunctions.SceneMap.get(networkCondition).size();
    }

    @Override
    public int getNumberOfVariables() {
        return 1;
    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());

        Permutation permutation = new Permutation(this.currentTraffics.size());
        permutation.randomize();
        solution.setVariable(0, permutation);

        // Make sure a new solution has very high fitness value
        // for every objective function, considering we are minimizing.
        for (int i = 0; i < getNumberOfObjectives(); i++) {
            solution.setObjective(i, Integer.MAX_VALUE);
        }

        return solution;
    }

    @Override
    public void close() {
        // do nothing
    }

    
    // TODO:: REMOVER SI NO USADO
    
    public int getNumberOfTraffics() {
        return this.currentTraffics.size();
    }

    public NetworkConditionEnum getNetworkCondition() {
        return this.networkCondition;
    }
    public List<Traffic> getCurrentTraffics() {
        return this.currentTraffics;
    }
    
    public int getCurrentWindows() {
        return this.currentWindows;
    }
}
