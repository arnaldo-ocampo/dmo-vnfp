package py.edu.fiuni.dmop.problem;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Permutation;
import org.moeaframework.problem.AbstractProblem;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.dto.SolutionTraffic;
import py.edu.fiuni.dmop.util.Configurations;
import java.util.List;
import py.edu.fiuni.dmop.service.TrafficService;
import py.edu.fiuni.dmop.service.VnfService;

/**
 *
 * @author Arnaldo Ocampo, Nestor Tapia
 */
public class VNFPlacementProblem extends AbstractProblem {

    private final List<Traffic> traffics;

    /**
     * Constructs a new instance of the Virtual Network Function Placement
     * problem, defining it to include 1 decision variable and 2 objectives.
     */
    public VNFPlacementProblem() {
        super(1, 8);
        traffics = TrafficService.traffics;
    }

    /**
     * Constructs a new solution and defines the bounds of the decision
     * variables.
     */
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());

        Permutation permutation = new Permutation(Configurations.numberTraffic);
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

        double[] f = new double[numberOfObjectives];
        Permutation permutation = (Permutation) solution.getVariable(0);

        SolutionTraffic solutions = vnfService.placement(traffics, permutation);

        /*objectives[0] = solutions.getDelayCost();
        objectives[1] = solutions.getNumberInstances();*/

        
        f[0] = solutions.getBandwidth();
        f[1] = solutions.getEnergyCost();
        f[2] = solutions.getDelayCost();
        f[3] = solutions.getDistance();
        f[4] = solutions.getFragmentation();
        f[5] = solutions.getLicencesCost();
        f[6] = solutions.getLoadTraffic();
        f[7] = solutions.getMaxUseLink();
        //f[8] = solutions.getNumberInstances();         
        //f[9] = solutions.getResourcesCost();
        //f[10] = solutions.getSloCost();
        //f[11] = solutions.getThroughput();
         
        solution.setObjectives(f);
    }

}
