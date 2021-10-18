package py.edu.fiuni.dmop;

import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;

import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
// import chapter2.SchafferProblem;


public class ManualTest {

    public static void main(String[] args) {
        // define the problem
        Problem problem = new Testing.MyDTLZ2(); // SchafferProblem();

        // create an initial random population of 100 individuals
        Initialization initialization = new RandomInitialization(
                problem,
                100);

        // define the selection operator
        TournamentSelection selection = new TournamentSelection(2,
                new ChainedComparator(
                        new ParetoDominanceComparator(),
                        new CrowdingComparator()));

        // define the crossover / mutation operator
        Variation variation = new GAVariation(
                new SBX(1.0, 25.0),
                new PM(1.0 / problem.getNumberOfVariables(), 30.0));

        // construct the algorithm
        Algorithm algorithm = new NSGAII(
                problem,
                new NondominatedSortingPopulation(),
                null, // no archive
                selection,
                variation,
                initialization);

        // run the algorithm for 10,000 evaluations
        while (algorithm.getNumberOfEvaluations() < 10000) {
            algorithm.step();
        }

        // get the Pareto approximate results
        NondominatedPopulation result = algorithm.getResult();
    }

}
