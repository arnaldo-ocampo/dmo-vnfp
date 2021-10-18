package py.edu.fiuni.dmop.algorithm;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.moeaframework.core.variable.Permutation;
import py.edu.fiuni.dmop.problem.DynamicVNFPlacementProblem;

public class NetStatusDetection implements Detection {

    private final double proportion;

    public NetStatusDetection(double proportion){
        this.proportion = proportion;
    }

    public NetStatusDetection(){
        this(0.1d);
    }

    @Override
    public double getProportionOfExperimentalSolutions() {
        return proportion;
    }

    @Override
    public boolean isEnvironmentChanged(Problem problem, Solution[] solutions) {
        System.out.println("Detecting if env has changed!?");
        // SI CAMBIÓ LA CANTIDAD DE TRAFICO, POR MINIMA QUE SEA, YA SE PUEDE DECIR
        // QUE EL ENTORNO HA CAMBIADO.. NO IMPORTA SI NO HA CAMBIADO EL ESCENARIO (NORMAL, SOBRECARGA).
        //DynamicVNFPlacementProblem myProblem = (DynamicVNFPlacementProblem)problem.;
        
        Solution tmpSolution = problem.newSolution();
        
        int objectives = ((Permutation)(tmpSolution.getVariable(0))).size();
        int permutationSize = ((Permutation)(solutions[0].getVariable(0))).size();
                
        return objectives != permutationSize;
        
        
        
        /*int[] indexes = ThreadLocalRandom.current().ints(0, solutions.length).distinct().limit((int)(solutions.length*this.proportion)).toArray();
        for(int index : indexes) {
            double[] objectives = solutions[index].getObjectives().clone();
            problem.evaluate(solutions[index]);
            if (!Arrays.equals(objectives, solutions[index].getObjectives())) {
                return true;
            }
        }
        return false;*/
    }
}