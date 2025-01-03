package py.edu.fiuni.dmop.decision.test;

import py.edu.fiuni.dmop.decision.DecisionMakerException;
import py.edu.fiuni.dmop.decision.saw.SAW;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;

import java.util.ArrayList;
import java.util.List;

public class SAWTest {
    public static void main(String[] args) {
        // Definir criterios
        Criteria criteria1 = new Criteria("Cost", 0.3, true); // Costo
        Criteria criteria2 = new Criteria("Quality", 0.5, false); // Calidad
        Criteria criteria3 = new Criteria("Delivery Time", 0.2, true); // Tiempo de entrega
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria1);
        criteriaList.add(criteria2);
        criteriaList.add(criteria3);

        // Definir alternativas
        Alternative alt1 = new Alternative("Supplier A");
        alt1.addCriteriaValue(criteria1, 500);  // Costo
        alt1.addCriteriaValue(criteria2, 8);    // Calidad
        alt1.addCriteriaValue(criteria3, 3);    // Tiempo de entrega

        Alternative alt2 = new Alternative("Supplier B");
        alt2.addCriteriaValue(criteria1, 450);  // Costo
        alt2.addCriteriaValue(criteria2, 7);    // Calidad
        alt2.addCriteriaValue(criteria3, 5);    // Tiempo de entrega

        Alternative alt3 = new Alternative("Supplier C");
        alt3.addCriteriaValue(criteria1, 550);  // Costo
        alt3.addCriteriaValue(criteria2, 9);    // Calidad
        alt3.addCriteriaValue(criteria3, 4);    // Tiempo de entrega

        List<Alternative> alternatives = new ArrayList<>();
        alternatives.add(alt1);
        alternatives.add(alt2);
        alternatives.add(alt3);

        // Crear objeto SAW
        SAW saw = new SAW(alternatives, criteriaList);

        try {
            // Calcular la mejor alternativa
            Alternative bestAlternative = saw.calculateOptimalSolution();

            // Encabezado de la tabla
            System.out.printf("%-15s", "Alternativa");
            for (Criteria crit : criteriaList) {
                System.out.printf("%-20s", crit.getName());
            }
            System.out.printf("%-15s\n", "Puntuación Total");

            System.out.println("=".repeat(15 + 20 * criteriaList.size() + 15));

            // Imprimir información de cada alternativa
            for (Alternative alt : alternatives) {
                System.out.printf("%-15s", alt.getName());
                for (int i = 0; i < criteriaList.size(); i++) {
                    double value = alt.getCriteriaValues().get(i).getValue();
                    System.out.printf("%-20.4f", value);
                }
                System.out.printf("%-15.4f\n", alt.getCalculatedPerformanceScore());
            }

            // Imprimir la mejor alternativa
            System.out.println("\nLa mejor alternativa es: " + bestAlternative.getName());
        } catch (DecisionMakerException e) {
            System.err.println("Error al calcular la mejor alternativa: " + e.getMessage());
        }
    }
}
