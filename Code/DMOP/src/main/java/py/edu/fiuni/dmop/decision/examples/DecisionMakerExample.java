package py.edu.fiuni.dmop.decision.examples;

import py.edu.fiuni.dmop.decision.ahp.AHP;
import py.edu.fiuni.dmop.decision.promethee.PROMETHEE;
import py.edu.fiuni.dmop.decision.topsis.*;
import java.util.*;

public class DecisionMakerExample {
    public static void main(String[] args) {
        // Define criteria
        List<Criteria> criteria = Arrays.asList(
                new Criteria("Cost", 0.5, true),
                new Criteria("Quality", 0.3, false),
                new Criteria("Delivery", 0.2, false)
        );

        // Define alternatives
        List<Alternative> alternatives = Arrays.asList(
                new Alternative("Supplier A", Arrays.asList(
                        new CriteriaValue(criteria.get(0), 100),
                        new CriteriaValue(criteria.get(1), 80),
                        new CriteriaValue(criteria.get(2), 90)
                )),
                new Alternative("Supplier B", Arrays.asList(
                        new CriteriaValue(criteria.get(0), 120),
                        new CriteriaValue(criteria.get(1), 85),
                        new CriteriaValue(criteria.get(2), 70)
                )),
                new Alternative("Supplier C", Arrays.asList(
                        new CriteriaValue(criteria.get(0), 110),
                        new CriteriaValue(criteria.get(1), 75),
                        new CriteriaValue(criteria.get(2), 80)
                ))
        );

        // TOPSIS Example
        Topsis topsis = new Topsis(alternatives);
        try {
            Alternative bestTopsis = topsis.calculateOptimalSolution();
            System.out.println("TOPSIS Best Alternative: " + bestTopsis.getName());
        } catch (Exception e) {
            System.err.println("Error in TOPSIS: " + e.getMessage());
        }

        // AHP Example
        AHP ahp = new AHP(alternatives, criteria);
        try {
            Alternative bestAHP = ahp.calculateOptimalSolution();
            System.out.println("AHP Best Alternative: " + bestAHP.getName());
        } catch (Exception e) {
            System.err.println("Error in AHP: " + e.getMessage());
        }

        // PROMETHEE Example
        double[] weights = {0.5, 0.3, 0.2};
        PROMETHEE promethee = new PROMETHEE(alternatives, criteria, weights);
        try {
            Alternative bestPromethee = promethee.calculateOptimalSolution();
            System.out.println("PROMETHEE Best Alternative: " + bestPromethee.getName());
        } catch (Exception e) {
            System.err.println("Error in PROMETHEE: " + e.getMessage());
        }
    }
}
