package py.edu.fiuni.dmop.decision.saw;

import py.edu.fiuni.dmop.decision.DecisionMaker;
import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;

import java.util.ArrayList;
import java.util.List;

public class SAW extends DecisionMaker {
    private List<Alternative> alternatives;
    private List<Criteria> criteria;

    public SAW(List<Alternative> alternatives, List<Criteria> criteria) {
        this.alternatives = alternatives;
        this.criteria = criteria;
    }

    @Override
    public Alternative calculateOptimalSolution() throws DecisionMakerException {
        normalize();
        List<Double> scores = calculateScores();
        return getBestAlternative(scores);
    }

    private void normalize() {
        for (int j = 0; j < criteria.size(); j++) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            for (Alternative alt : alternatives) {
                double value = alt.getCriteriaValues().get(j).getValue();
                if (value < min) min = value;
                if (value > max) max = value;
            }

            for (Alternative alt : alternatives) {
                double value = alt.getCriteriaValues().get(j).getValue();
                if (criteria.get(j).isNegative()) {
                    alt.getCriteriaValues().get(j).setValue(min / value);
                } else {
                    alt.getCriteriaValues().get(j).setValue(value / max);
                }
            }
        }
    }

    private List<Double> calculateScores() {
        List<Double> scores = new ArrayList<>();
        for (Alternative alt : alternatives) {
            double score = 0;
            for (int j = 0; j < criteria.size(); j++) {
                score += alt.getCriteriaValues().get(j).getValue() * criteria.get(j).getWeight();
            }
            scores.add(score);
        }
        return scores;
    }

    private Alternative getBestAlternative(List<Double> scores) {
        double maxScore = Double.MIN_VALUE;
        int bestIndex = -1;
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i) > maxScore) {
                maxScore = scores.get(i);
                bestIndex = i;
            }
        }
        return alternatives.get(bestIndex);
    }
}