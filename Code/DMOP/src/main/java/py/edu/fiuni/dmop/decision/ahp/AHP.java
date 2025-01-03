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
            normalizeCriteriaValues(); // Normaliza los valores de los criterios antes de calcular los pesos
            calculateWeights();
            calculateGlobalScores(); // Establece los puntajes en las alternativas
            return determineBestAlternative();
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

        // Normaliza las columnas
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

        // Calcula los pesos como el promedio de las filas normalizadas
        for (int i = 0; i < size; i++) {
            double rowSum = 0.0;
            for (int j = 0; j < size; j++) {
                rowSum += pairwiseComparisonMatrix[i][j];
            }
            weights[i] = rowSum / size;
        }
    }

    private void calculateGlobalScores() {
        for (int i = 0; i < alternatives.size(); i++) {
            Alternative alternative = alternatives.get(i);
            List<CriteriaValue> criteriaValues = alternative.getCriteriaValues();
            double globalScore = 0.0;
            for (int j = 0; j < criteriaValues.size(); j++) {
                globalScore += criteriaValues.get(j).getValue() * weights[j];
            }
            // Establecer el puntaje calculado en la alternativa
            alternative.setCalculatedPerformanceScore(globalScore);
        }
    }

    private Alternative determineBestAlternative() {
        int bestIndex = 0;
        for (int i = 1; i < alternatives.size(); i++) {
            if (alternatives.get(i).getCalculatedPerformanceScore() >
                    alternatives.get(bestIndex).getCalculatedPerformanceScore()) {
                bestIndex = i;
            }
        }
        return alternatives.get(bestIndex);
    }

    private void normalizeCriteriaValues() {
        // Normalizar cada criterio de cada alternativa
        for (int j = 0; j < criteria.size(); j++) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;

            // Encuentra los valores mínimo y máximo de este criterio
            for (int i = 0; i < alternatives.size(); i++) {
                double value = alternatives.get(i).getCriteriaValues().get(j).getValue();
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }

            // Verificar si max y min son iguales para evitar la división por cero
            if (max != min) {
                // Normaliza los valores para este criterio
                for (int i = 0; i < alternatives.size(); i++) {
                    double value = alternatives.get(i).getCriteriaValues().get(j).getValue();
                    double normalizedValue = (value - min) / (max - min);
                    alternatives.get(i).getCriteriaValues().get(j).setValue(normalizedValue);
                }
            } else {
                // Si max y min son iguales, no se puede normalizar, se asigna un valor por defecto (por ejemplo, 0)
                for (int i = 0; i < alternatives.size(); i++) {
                    alternatives.get(i).getCriteriaValues().get(j).setValue(0); // O cualquier valor que sea adecuado
                }
            }
        }
    }
}
