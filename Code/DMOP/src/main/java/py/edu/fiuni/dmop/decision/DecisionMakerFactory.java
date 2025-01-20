package py.edu.fiuni.dmop.decision;

import py.edu.fiuni.dmop.decision.ahp.AHP;
import py.edu.fiuni.dmop.decision.promethee.PROMETHEE;
import py.edu.fiuni.dmop.decision.saw.SAW;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;
import py.edu.fiuni.dmop.decision.topsis.Topsis;

import java.util.List;

public class DecisionMakerFactory {

    public static DecisionMaker getDecisionMaker(String algorithm, List<Alternative> alternatives, List<Criteria> criteria) {
        switch (algorithm.toLowerCase()) {
            case "topsis":
                Topsis topsis = new Topsis();
                for (Alternative alt : alternatives) {
                    topsis.addAlternative(alt);
                }
                return topsis;

            case "saw":
                return new SAW(alternatives, criteria);

            case "promethee":
                double[] weights = criteria.stream().mapToDouble(Criteria::getWeight).toArray();
                return new PROMETHEE(alternatives, criteria, weights);

            case "ahp":
                AHP ahp = new AHP(alternatives, criteria);
                ahp.setPairwiseComparisonMatrix(createDefaultComparisonMatrix(criteria.size()));
                return ahp;

            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }

    private static double[][] createDefaultComparisonMatrix(int size) {
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (i == j) ? 1.0 : 0.5; // Default values
            }
        }
        return matrix;
    }
}
