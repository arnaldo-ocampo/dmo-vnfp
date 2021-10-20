package py.edu.fiuni.dmop.algorithm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import org.moeaframework.core.*;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.variable.Permutation;

/**
 * Implementation of the first version of Dynamic NSGA-II algorithm proposed by
 * Kalyanmoy Deb et. al. by extending the original {@link NSGAII} algorithm and
 * implementing the {@link DynamicAlgorithm}
 * <p>
 * References:
 * <ol>
 * <li>Deb K., Rao N. U.B., Karthik S. (2007) Dynamic Multi-objective
 * Optimization and Decision-Making Using Modified NSGA-II: A Case Study on
 * Hydro-thermal Power Scheduling. In: Obayashi S., Deb K., Poloni C., Hiroyasu
 * T., Murata T. (eds) Evolutionary Multi-Criterion Optimization. EMO 2007.
 * Lecture Notes in Computer Science, vol 4403. Springer, Berlin, Heidelberg
 * </ol>
 */
public class DNSGAIIA extends NSGAII implements DynamicAlgorithm {

    /**
     * The detection operator
     */
    private final Detection detection;

    /**
     * operator
     */
    private final double zeta;

    /**
     * Constructs the NSGA-II algorithm with the specified components.
     *
     * @param problem the problem being solved
     * @param population the population used to store solutions
     * @param archive the archive used to store the result; can be {@code null}
     * @param selection the selection operator
     * @param variation the variation operator
     * @param initialization
     * @param detection the detection operator
     * @param zeta the zeta variable
     */
    public DNSGAIIA(Problem problem, NondominatedSortingPopulation population, EpsilonBoxDominanceArchive archive, Selection selection, Variation variation, Initialization initialization, Detection detection, double zeta) {
        super(problem, population, archive, selection, variation, initialization);
        this.detection = detection;
        this.zeta = zeta;
    }

    public DNSGAIIA(Problem problem, NondominatedSortingPopulation population, EpsilonBoxDominanceArchive archive, Selection selection, Variation variation, Initialization initialization, Detection detection) {
        this(problem, population, archive, selection, variation, initialization, detection, 0.2d);
    }

    @Override
    public void iterate() {

        System.out.println("DNSGAiiA - Iterate executed: Evaluations:" + getNumberOfEvaluations());

        // check for changes is the environment
        // create a temp solution for the problem, if the environment changed, 
        // then the solution will have a Variable(Permutation) of different length 
        // compared to those solutions in the current population.
        Solution tmpSolution = problem.newSolution();

        int newVarPermLength = ((Permutation) (tmpSolution.getVariable(0))).size();
        int varPermLength = ((Permutation) (population.get(0).getVariable(0))).size();

        if (varPermLength != newVarPermLength) {
            stepOnChange();
        }

        /*if(detection.isEnvironmentChanged(problem, StreamSupport.stream(population.spliterator(),true).toArray(Solution[]::new))){
            stepOnChange();
        }*/
        super.iterate();
    }

    @Override
    public void stepOnChange() {
        System.out.println("Step on changed executed...");

        Solution firstSolutionPop = this.population.get(0);
        Solution tmpSolution = problem.newSolution();

        //int currentPermutationSize = ((Permutation) firstSolutionPop.getVariable(0)).size();
        int newPermutationSize = ((Permutation) tmpSolution.getVariable(0)).size();

        //int newObjectivesNumber = tmpSolution.getObjectives().length;
        // Se obtienen los indices de las soluciones a ser borradas de la poblacion (aleatoriamente)
        // Luego se insertan nuevas soluciones en las posiciones de aquellas soluciones que fueron borradas.
        int[] indexesToRemove = ThreadLocalRandom.current().ints(0, population.size()).distinct().limit((int) (population.size() * this.zeta)).toArray();
        population.removeAll(Arrays.stream(indexesToRemove).boxed().map(population::get).collect(Collectors.toList()));

        List<Solution> newSolutions = new ArrayList<>();
        Stream.generate(problem::newSolution).limit(population.size()).forEach(newSolutions::add);

        // Obtener los indices de las soluciones que no estan en el arreglo de los borrados.        
        //Set<Integer> removedIndexesSet = IntStream.of(indexesToRemove).boxed().collect(Collectors.toSet());
        //int[] nonRemovedIndexes = IntStream.range(0, population.size()).filter(i -> !removedIndexesSet.contains(i)).toArray();
        for (int solIndex = 0; solIndex < population.size(); solIndex++) {
            Solution oldSolution = population.get(solIndex);
            Solution newSolution = newSolutions.get(solIndex);

            // Make sure permutation length is the correct one. 
            // Will need to be reduced or expanded depending on the case
            Permutation permutation = (Permutation) oldSolution.getVariable(0);
            int[] currentPermValues = permutation.toArray();

            int[] newPermValues = null;

            // Se debe reducir el tamaño de la permutation en la solucion
            if (permutation.size() > newPermutationSize) {
                newPermValues = IntStream.of(currentPermValues).filter(n -> n < newPermutationSize).toArray();

                //Permutation newPermutation = new Permutation(newPermValues);
                //theSolution.setVariable(0, newPermutation);
            } else if (permutation.size() < newPermutationSize) {
                // Aumentar el tamaño de la permutation, agregar los numeros que faltan al final
                // Por ahora agregados en orden
                // TODO: Agregar al final pero en orden aleatorio
                newPermValues = IntStream.range(0, newPermutationSize).toArray(); // new int[newPermutationSize];
                System.arraycopy(currentPermValues, 0, newPermValues, 0, currentPermValues.length);

                //Permutation newPermutation = new Permutation(newPermValues);
                //theSolution.setVariable(0, newPermutation);
            }

            // Make sure objective functions number is the correct one. 
            // Will need to be reduced or expanded depending on the case
            double[] oldObjectives = oldSolution.getObjectives();
            double[] newObjectives = newSolution.getObjectives();

            // reduce the number of objective functions
            if (oldObjectives.length > newObjectives.length) {
                //double[] newObjectives = new double[newObjectivesNumber];
                System.arraycopy(oldObjectives, 0, newObjectives, 0, newObjectives.length);

                // The objective funcitons number remains the same or 
                // Increase the number of objective functions
            } else if (oldObjectives.length <= newObjectives.length) {
                // Aumentar el numero de funciones objetivo, los nuevos agregados seran inicializados
                // a un valor muy alto, considerando que solo estamos usando minimizacion.                
                //double[] newObjectives = DoubleStream.generate(() -> Double.MAX_VALUE).limit(newObjectivesNumber).toArray();
                System.arraycopy(oldObjectives, 0, newObjectives, 0, oldObjectives.length);
            }

            newSolution.setVariable(0, new Permutation(newPermValues));
            newSolution.setObjectives(newObjectives);
        }

        // Clear the current population, so we can add the modified ones
        population.clear();
        
        population.addAll(newSolutions);
        
        // Add new solutions at the end, instead of those deleted at the first step.
        Stream.generate(problem::newSolution).limit(indexesToRemove.length).forEach(population::add);

        
        /*
        population.forEach(sol -> {
            Permutation permu = (Permutation) sol.getVariable(0);
            double[] objectives = sol.getObjectives();
            System.out.println("Fitness: " + Arrays.toString(objectives) + "  Permu: " + Arrays.toString(permu.toArray()));
        });
        */
    }
}
