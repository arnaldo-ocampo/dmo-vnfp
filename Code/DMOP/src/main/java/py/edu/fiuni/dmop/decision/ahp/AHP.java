package py.edu.fiuni.dmop.decision.ahp;

import java.util.List;

/**
 * Implementación del Proceso Analítico Jerárquico (AHP).
 * Referencia: María Siles Luna, "Implementación de Métodos de Toma de Decisiones Multicriterio usando Matlab", 2015.
 *
 * @author Marcelo Ferreira
 */
public class AHP {
    private final List<Alternative> alternatives;
    private final List<Criteria> criteria;
    private final PairwiseComparisonMatrix criteriaComparisonMatrix;

    public AHP(List<Alternative> alternatives, List<Criteria> criteria) {
        this.alternatives = alternatives;
        this.criteria = criteria;
        this.criteriaComparisonMatrix = new PairwiseComparisonMatrix(criteria.size());
    }

    public void setCriteriaComparison(int row, int col, double value) {
        criteriaComparisonMatrix.setComparison(row, col, value);
    }

    public void calculateGlobalWeights() throws AHPIncompleteDataException {
        double[] criteriaWeights = criteriaComparisonMatrix.calculateCriteriaWeights();

        if (criteriaWeights.length != criteria.size()) {
            throw new AHPIncompleteDataException();
        }

        for (Alternative alternative : alternatives) {
            double globalWeight = 0.0;
            for (int i = 0; i < criteria.size(); i++) {
                globalWeight += criteriaWeights[i] * alternative.getCriteriaWeights().get(i);
            }
            System.out.println("Alternative: " + alternative.getName() + " - Global Weight: " + globalWeight);
        }
    }
}
