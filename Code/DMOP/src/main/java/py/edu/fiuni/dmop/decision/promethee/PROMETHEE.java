package py.edu.fiuni.dmop.decision.promethee;

import lombok.Getter;
import py.edu.fiuni.dmop.decision.DecisionMaker;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;

import java.util.List;

public class PROMETHEE extends DecisionMaker {
    private final List<Alternative> alternatives;
    private final List<Criteria> criteria;
    private final double[] weights;
    private double[][] preferenceMatrix;
    @Getter
    private double[] positiveFlow;
    @Getter
    private double[] negativeFlow;

    public PROMETHEE(List<Alternative> alternatives, List<Criteria> criteria, double[] weights) {
        this.alternatives = alternatives;
        this.criteria = criteria;
        this.weights = weights;
    }

    @Override
    public Alternative calculateOptimalSolution() throws DecisionMakerException {
        try {
            calculatePreferenceMatrix();
            calculateFlows();
            return determineBestAlternative();
        } catch (Exception e) {
            throw new DecisionMakerException("Error during PROMETHEE calculation: " + e.getMessage());
        }
    }

    private void calculatePreferenceMatrix() {
        int size = alternatives.size();
        preferenceMatrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    double preference = 0.0;
                    for (int k = 0; k < criteria.size(); k++) {
                        double diff = alternatives.get(i).getCriteriaValues().get(k).getValue() -
                                alternatives.get(j).getCriteriaValues().get(k).getValue();
                        preference += weights[k] * Math.max(0, diff); // Linear preference function
                    }
                    preferenceMatrix[i][j] = preference;
                }
            }
        }
    }

    private void calculateFlows() {
        int size = alternatives.size();
        positiveFlow = new double[size];
        negativeFlow = new double[size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    positiveFlow[i] += preferenceMatrix[i][j];
                    negativeFlow[i] += preferenceMatrix[j][i];
                }
            }
        }
    }

    private Alternative determineBestAlternative() {
        double[] netFlow = new double[alternatives.size()];
        int bestIndex = 0;
        for (int i = 0; i < alternatives.size(); i++) {
            netFlow[i] = positiveFlow[i] - negativeFlow[i];
            if (netFlow[i] > netFlow[bestIndex]) {
                bestIndex = i;
            }
        }
        return alternatives.get(bestIndex);
    }

}