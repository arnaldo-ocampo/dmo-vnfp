package py.edu.fiuni.dmop.problem;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.problem.AbstractProblem;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.SolutionTraffic;
import py.edu.fiuni.dmop.util.Configurations;
import java.util.List;
import py.edu.fiuni.dmop.dto.TrafficSolutionMap;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.service.VnfService;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;
import py.edu.fiuni.dmop.util.ObjectiveFunctionEnum;

/**
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class StaticVNFPlacementProblem extends AbstractProblem {

    private final List<Traffic> traffics;
    private final NetworkConditionEnum networkCondition;
    
    /**
     * Constructs a new instance of the Virtual Network Function Placement
     * problem, it uses 1 decision variable and the number objectives used by the current scene.
     * @param traffics The list of traffics to be processed
     * @param networkCondition The current scenario the network is running under
     */
    public StaticVNFPlacementProblem(List<Traffic> traffics, NetworkConditionEnum networkCondition) {
        
        // the number of objective fuctions depends on the Scene we are running on.
        super(1, SceneObjectiveFunctions.SceneMap.get(networkCondition).size());
        
        this.traffics = traffics;
        this.networkCondition = networkCondition;
    }

    /**
     * Constructs a new solution and defines the bounds of the decision
     * variables.
     */
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());

        Permutation permutation = new Permutation(this.traffics.size());
        permutation.randomize();
        solution.setVariable(0, permutation);

        return solution;
    }

    /**
     * Extracts the decision variables from the solution, 
     * evaluates the Virtual Network Function placement, 
     * and saves the resulting objective values back to the solution.
     */
    @Override
    public void evaluate(Solution solution) {
        VnfService vnfService = new VnfService();

        double[] objectives = new double[numberOfObjectives];
        Permutation permutation = (Permutation) solution.getVariable(0);

        //
        TrafficSolutionMap solutionsMap = vnfService.placement(traffics, permutation);
        
        //
        List<ObjectiveFunctionEnum> sceneObjectives = SceneObjectiveFunctions.SceneMap.get(networkCondition);
        
        //
        for(int foIndex = 0; foIndex < this.getNumberOfObjectives(); foIndex++){
            objectives[foIndex] = solutionsMap.getObjectiveFunctionValueFor(sceneObjectives.get(foIndex));
        }       
        
        solution.setObjectives(objectives);
    }
}
