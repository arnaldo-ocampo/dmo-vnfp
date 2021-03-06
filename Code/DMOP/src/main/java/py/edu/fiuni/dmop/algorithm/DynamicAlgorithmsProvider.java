package py.edu.fiuni.dmop.algorithm;

import org.moeaframework.core.*;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.CrowdingComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.spi.AlgorithmProvider;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.ProviderNotFoundException;
import org.moeaframework.util.TypedProperties;

import java.util.Properties;

public class DynamicAlgorithmsProvider extends AlgorithmProvider {

    @Override
    public Algorithm getAlgorithm(String name, Properties properties, Problem problem) {
        TypedProperties typedProperties = new TypedProperties(properties);

        try {
            if (name.equalsIgnoreCase("DNSGAIIA") || name.equalsIgnoreCase("DNSGAII-A")) {
                return newDNSGAIIA(typedProperties, problem);

            } else if (name.equalsIgnoreCase("DNSGAIIB") || name.equalsIgnoreCase("DNSGAII-B")) {
                return newDNSGAIIB(typedProperties, problem);

            } else {
                return null;
            }
        } catch (FrameworkException e) {
            throw new ProviderNotFoundException(name, e);
        }
    }

    private Algorithm newDNSGAIIA(TypedProperties properties, Problem problem) {
        int populationSize = (int) properties.getDouble("populationSize", 100);

        Initialization initialization = new RandomInitialization(problem, populationSize);

        NondominatedSortingPopulation population = new NondominatedSortingPopulation();

        TournamentSelection selection = null;

        if (properties.getBoolean("withReplacement", true)) {
            selection = new TournamentSelection(2,
                    new ChainedComparator(new ParetoDominanceComparator(), new CrowdingComparator()));
        }

        Variation variation = OperatorFactory.getInstance().getVariation(null, properties, problem);

        Detection detection = new StandardDetection();

        double zeta = properties.getDouble("zeta", 0.2d);

        return new DNSGAIIA(problem, population, null, selection, variation, initialization, detection, zeta);
    }

    private Algorithm newDNSGAIIB(TypedProperties properties, Problem problem) {
        int populationSize = (int) properties.getDouble("populationSize", 100);

        Initialization initialization = new RandomInitialization(problem, populationSize);

        NondominatedSortingPopulation population = new NondominatedSortingPopulation();

        TournamentSelection selection = null;

        if (properties.getBoolean("withReplacement", true)) {
            selection = new TournamentSelection(2,
                    new ChainedComparator(new ParetoDominanceComparator(), new CrowdingComparator()));
        }

        Variation variation = OperatorFactory.getInstance().getVariation(null, properties, problem);

        Detection detection = new StandardDetection();

        double zeta = properties.getDouble("zeta", 0.2d);

        return new DNSGAIIB(problem, population, null, selection, variation, initialization, detection, zeta);
    }
}
