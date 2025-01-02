package py.edu.fiuni.dmop.decision.test;

import py.edu.fiuni.dmop.decision.promethee.PROMETHEE;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;

import java.util.ArrayList;
import java.util.List;

public class PROMETHEETest {
    public static void main(String[] args) {
        // Definir criterios
        Criteria criteria1 = new Criteria("Cost", 0);
        Criteria criteria2 = new Criteria("Quality", 0);
        Criteria criteria3 = new Criteria("Delivery Time", 0);
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria1);
        criteriaList.add(criteria2);
        criteriaList.add(criteria3);

        // Definir alternativas
        Alternative alt1 = new Alternative("Supplier A");
        alt1.addCriteriaValue(criteria1, 500);  // Cost
        alt1.addCriteriaValue(criteria2, 8);    // Quality
        alt1.addCriteriaValue(criteria3, 3);    // Delivery Time

        Alternative alt2 = new Alternative("Supplier B");
        alt2.addCriteriaValue(criteria1, 450);
        alt2.addCriteriaValue(criteria2, 7);
        alt2.addCriteriaValue(criteria3, 5);

        Alternative alt3 = new Alternative("Supplier C");
        alt3.addCriteriaValue(criteria1, 550);
        alt3.addCriteriaValue(criteria2, 9);
        alt3.addCriteriaValue(criteria3, 4);

        List<Alternative> alternatives = new ArrayList<>();
        alternatives.add(alt1);
        alternatives.add(alt2);
        alternatives.add(alt3);

        // Pesos de los criterios
        double[] weights = {0.5, 0.3, 0.2}; // Ejemplo de pesos: Cost (50%), Quality (30%), Delivery Time (20%)

        // Crear objeto PROMETHEE
        PROMETHEE promethee = new PROMETHEE(alternatives, criteriaList, weights);

        try {
            // Calcular la mejor alternativa
            Alternative bestAlternative = promethee.calculateOptimalSolution();
            System.out.println("La mejor alternativa es: " + bestAlternative.getName());
        } catch (Exception e) {
            System.err.println("Error al calcular la mejor alternativa: " + e.getMessage());
        }
    }
}
