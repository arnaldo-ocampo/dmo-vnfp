package py.edu.fiuni.dmop.decision.promethee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del método PROMETHEE.
  * Referencia: María Siles Luna, "Implementación de Métodos de Toma de Decisiones Multicriterio usando Matlab", 2015.
 *
 * @author Marcelo Ferreira
 */
public class PROMETHEE {
    private final List<Alternative> alternatives;
    private final List<Criteria> criteria;

    public PROMETHEE(List<Alternative> alternatives, List<Criteria> criteria) {
        this.alternatives = alternatives;
        this.criteria = criteria;
    }

    public Map<Alternative, Double> calculateFlows() {
        Map<Alternative, Double> netFlows = new HashMap<>();

        for (Alternative a : alternatives) {
            double positiveFlow = 0;
            double negativeFlow = 0;

            for (Alternative b : alternatives) {
                if (!a.equals(b)) {
                    double preference = calculateAggregatePreference(a, b);
                    positiveFlow += preference;
                    negativeFlow += calculateAggregatePreference(b, a);
                }
            }

            double netFlow = positiveFlow - negativeFlow;
            netFlows.put(a, netFlow);
        }

        return netFlows;
    }

    private double calculateAggregatePreference(Alternative a, Alternative b) {
        double aggregatePreference = 0;

        for (Criteria c : criteria) {
            double diff = a.getEvaluations().get(c) - b.getEvaluations().get(c);
            PreferenceFunction pf = new PreferenceFunction(0.1, 0.2); // Umbrales de ejemplo
            aggregatePreference += pf.calculatePreference(diff) * c.getWeight();
        }

        return aggregatePreference;
    }

    public void printResults(Map<Alternative, Double> netFlows) {
        System.out.println("Resultados PROMETHEE:");
        netFlows.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> System.out.println("Alternative: " + entry.getKey().getName() + " - Net Flow: " + entry.getValue()));
    }
}
