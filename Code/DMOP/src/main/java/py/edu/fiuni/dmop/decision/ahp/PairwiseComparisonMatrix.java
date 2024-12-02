package py.edu.fiuni.dmop.decision.ahp;

import lombok.Getter;

/**
 * Maneja las matrices de comparación por pares para el método AHP.
 *
 * @author Marcelo Ferreira
 */
@Getter
public class PairwiseComparisonMatrix {
    private final double[][] matrix;

    public PairwiseComparisonMatrix(int size) {
        matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = 1.0;
        }
    }

    public void setComparison(int row, int col, double value) {
        matrix[row][col] = value;
        matrix[col][row] = 1 / value;
    }

    public double[] calculateCriteriaWeights() {
        int n = matrix.length;
        double[] rowSums = new double[n];
        double totalSum = 0;

        // Sumar filas
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rowSums[i] += matrix[i][j];
            }
            totalSum += rowSums[i];
        }

        // Normalizar filas
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            weights[i] = rowSums[i] / totalSum;
        }

        return weights;
    }
}
