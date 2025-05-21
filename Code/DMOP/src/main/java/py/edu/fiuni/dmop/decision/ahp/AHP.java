package py.edu.fiuni.dmop.decision.ahp;

import java.util.*;
import py.edu.fiuni.dmop.decision.DecisionMaker;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;
import py.edu.fiuni.dmop.decision.topsis.CriteriaValue;

/**
 * Implementación del método AHP (Analytic Hierarchy Process).
 * Este método utiliza comparaciones por pares para determinar la importancia relativa
 * de los criterios y las alternativas.
 * 
 * Referencia: María Siles Luna, "Implementación de Métodos de Toma de Decisiones Multicriterio usando Matlab", 2015.
 *
 * @author Marcelo Ferreira
 * Basado en la implementación original de Arnaldo Ocampo, Néstor Tapia
 */
public class AHP extends DecisionMaker {
    private final List<Alternative> alternatives;
    private final List<Criteria> criteria;
    private double[][] pairwiseComparisonMatrix; // Matriz de comparación por pares
    private double[] weights;                    // Pesos calculados para cada criterio
    private double consistencyRatio;             // Ratio de consistencia
    private double consistencyIndex;             // Índice de consistencia
    
    // Valores del índice aleatorio para diferentes tamaños de matriz
    private static final Map<Integer, Double> RANDOM_INDEX = new HashMap<>();
    static {
        RANDOM_INDEX.put(1, 0.0);
        RANDOM_INDEX.put(2, 0.0);
        RANDOM_INDEX.put(3, 0.58);
        RANDOM_INDEX.put(4, 0.90);
        RANDOM_INDEX.put(5, 1.12);
        RANDOM_INDEX.put(6, 1.24);
        RANDOM_INDEX.put(7, 1.32);
        RANDOM_INDEX.put(8, 1.41);
        RANDOM_INDEX.put(9, 1.45);
        RANDOM_INDEX.put(10, 1.49);
    }

    /**
     * Constructor de la clase AHP.
     * @param alternatives Lista de alternativas a evaluar
     * @param criteria Lista de criterios a considerar
     */
    public AHP(List<Alternative> alternatives, List<Criteria> criteria) {
        this.alternatives = alternatives;
        this.criteria = criteria;
    }

    @Override
    public Alternative calculateOptimalSolution() throws DecisionMakerException {
        try {
            validateAlternatives(alternatives, criteria);
            normalizeCriteriaValues();
            calculateWeights();
            calculateConsistencyRatio();
            calculateGlobalScores();
            return determineBestAlternative();
        } catch (Exception e) {
            throw new DecisionMakerException("Error durante el cálculo AHP: " + e.getMessage());
        }
    }

    /**
     * Establece la matriz de comparación por pares.
     * @param matrix Matriz de comparación por pares
     * @throws IllegalArgumentException si el tamaño de la matriz no coincide con el número de criterios
     */
    public void setPairwiseComparisonMatrix(double[][] matrix) {
        if (matrix.length != criteria.size() || matrix[0].length != criteria.size()) {
            throw new IllegalArgumentException("El tamaño de la matriz debe coincidir con el número de criterios.");
        }
        this.pairwiseComparisonMatrix = matrix;
    }

    /**
     * Calcula los pesos de los criterios a partir de la matriz de comparación por pares.
     * Utiliza el método de normalización de columnas y promedio de filas.
     */
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
        
        // Actualiza los pesos en los criterios
        for (int i = 0; i < size; i++) {
            criteria.get(i).setWeight(weights[i]);
        }
    }

    /**
     * Calcula el ratio de consistencia de la matriz de comparación por pares.
     * Este valor indica qué tan consistente es la matriz de comparaciones.
     */
    private void calculateConsistencyRatio() {
        int size = criteria.size();
        double[] weightedSums = new double[size];
        
        // Calcula las sumas ponderadas
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                weightedSums[i] += pairwiseComparisonMatrix[i][j] * weights[j];
            }
        }
        
        // Calcula lambda máximo
        double lambdaMax = 0;
        for (int i = 0; i < size; i++) {
            lambdaMax += weightedSums[i] / weights[i];
        }
        lambdaMax /= size;
        
        // Calcula el índice de consistencia
        consistencyIndex = (lambdaMax - size) / (size - 1);
        
        // Calcula el ratio de consistencia
        double randomIndex = RANDOM_INDEX.getOrDefault(size, 1.49);
        consistencyRatio = consistencyIndex / randomIndex;
    }

    /**
     * Calcula las puntuaciones globales para cada alternativa.
     * Multiplica los valores normalizados por los pesos de los criterios.
     */
    private void calculateGlobalScores() {
        for (int i = 0; i < alternatives.size(); i++) {
            Alternative alternative = alternatives.get(i);
            List<CriteriaValue> criteriaValues = alternative.getCriteriaValues();
            double globalScore = 0.0;
            for (int j = 0; j < criteriaValues.size(); j++) {
                globalScore += criteriaValues.get(j).getValue() * weights[j];
            }
            alternative.setCalculatedPerformanceScore(globalScore);
        }
    }

    /**
     * Determina la mejor alternativa basada en las puntuaciones globales.
     * @return La alternativa con la puntuación más alta
     */
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

    /**
     * Normaliza los valores de los criterios para cada alternativa.
     * Utiliza el método de normalización min-max.
     */
    private void normalizeCriteriaValues() {
        for (int j = 0; j < criteria.size(); j++) {
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;

            for (int i = 0; i < alternatives.size(); i++) {
                double value = alternatives.get(i).getCriteriaValues().get(j).getValue();
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }

            if (max != min) {
                for (int i = 0; i < alternatives.size(); i++) {
                    double value = alternatives.get(i).getCriteriaValues().get(j).getValue();
                    double normalizedValue = normalizeValue(value, min, max, criteria.get(j).isNegative());
                    alternatives.get(i).getCriteriaValues().get(j).setValue(normalizedValue);
                }
            } else {
                for (int i = 0; i < alternatives.size(); i++) {
                    alternatives.get(i).getCriteriaValues().get(j).setValue(0);
                }
            }
        }
    }
    
    /**
     * @return El ratio de consistencia de la matriz de comparación por pares
     */
    public double getConsistencyRatio() {
        return consistencyRatio;
    }
    
    /**
     * @return El índice de consistencia de la matriz de comparación por pares
     */
    public double getConsistencyIndex() {
        return consistencyIndex;
    }
    
    /**
     * @return true si la matriz de comparación por pares es suficientemente consistente (CR < 0.1)
     */
    public boolean isConsistent() {
        return consistencyRatio < 0.1;
    }
    
    /**
     * @return Los pesos calculados para cada criterio
     */
    public double[] getWeights() {
        return weights;
    }
}

