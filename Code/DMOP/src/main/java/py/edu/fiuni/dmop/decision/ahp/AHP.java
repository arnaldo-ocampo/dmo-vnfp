package py.edu.fiuni.dmop.decision.ahp;

import java.util.*;
import py.edu.fiuni.dmop.decision.DecisionMaker;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;
import py.edu.fiuni.dmop.decision.topsis.CriteriaValue;

/**
 * Implementación del método AHP.
 * Referencia: María Siles Luna, "Implementación de Métodos de Toma de Decisiones Multicriterio usando Matlab", 2015.
 *
 * @author Marcelo Ferreira
 */
public class AHP extends DecisionMaker {
    private final List<Alternative> alternatives;
    private final List<Criteria> criteria;
    private double[][] pairwiseComparisonMatrix;
    private double[] weights;

    public AHP(List<Alternative> alternatives, List<Criteria> criteria) {
        this.alternatives = alternatives;
        this.criteria = criteria;
    }

    @Override
    public Alternative calculateOptimalSolution() throws DecisionMakerException {
        try {
            calculateWeights();
            double[] globalScores = calculateGlobalScores();
            return determineBestAlternative(globalScores);
        } catch (Exception e) {
            throw new DecisionMakerException("Error during AHP calculation: " + e.getMessage());
        }
    }

    public void setPairwiseComparisonMatrix(double[][] matrix) {
        if (matrix.length != criteria.size() || matrix[0].length != criteria.size()) {
            throw new IllegalArgumentException("Matrix size must match the number of criteria.");
        }
        this.pairwiseComparisonMatrix = matrix;
    }

    private void calculateWeights() {
        int size = criteria.size();
        weights = new double[size];
        double[] columnSums = new double[size];

        // Normalize columns
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                columnSums[j] += pairwiseComparisonMatrix[i][j];
            }
        }
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                pairwiseComparisonMatrix[i][j] /= columnSums[j];
            }
        }

        // Calculate weights as the average of normalized rows
        for (int i = 0; i < size; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < size; j++) {
                rowSum += pairwiseComparisonMatrix[i][j];
            }
            weights[i] = rowSum / size;
        }
    }

    private double[] calculateGlobalScores() {
        double[] globalScores = new double[alternatives.size()];
        for (int i = 0; i < alternatives.size(); i++) {
            Alternative alternative = alternatives.get(i);
            List<CriteriaValue> criteriaValues = alternative.getCriteriaValues();
            for (int j = 0; j < criteriaValues.size(); j++) {
                globalScores[i] += criteriaValues.get(j).getValue() * weights[j];
            }
        }
        return globalScores;
    }

    private Alternative determineBestAlternative(double[] globalScores) {
        int bestIndex = 0;
        for (int i = 1; i < globalScores.length; i++) {
            if (globalScores[i] > globalScores[bestIndex]) {
                bestIndex = i;
            }
        }
        return alternatives.get(bestIndex);
    }
}
