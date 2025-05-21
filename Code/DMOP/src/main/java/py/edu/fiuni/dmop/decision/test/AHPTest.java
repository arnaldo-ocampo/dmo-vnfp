package py.edu.fiuni.dmop.decision.test;

import py.edu.fiuni.dmop.decision.ahp.AHP;
import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;

import java.util.ArrayList;
import java.util.List;

public class AHPTest {
    public static void main(String[] args) {
        // Definir criterios con pesos 0 (igual importancia)
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

        // Crear objeto AHP
        AHP ahp = new AHP(alternatives, criteriaList);

        // Configurar la matriz de comparación por pares
        double[][] comparisonMatrix = {
                {1.0, 2.0, 0.5},  // Comparación entre Cost y otros criterios
                {0.5, 1.0, 0.33}, // Comparación entre Quality y otros criterios
                {2.0, 3.0, 1.0}   // Comparación entre Delivery Time y otros criterios
        };
        ahp.setPairwiseComparisonMatrix(comparisonMatrix);

        try {
            // Calcular la solución óptima
            Alternative best = ahp.calculateOptimalSolution();
            
            // Verificar consistencia
            if (ahp.isConsistent()) {
                System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio());
                System.out.println("Best alternative: " + best.getName());
                System.out.println("Score: " + best.getCalculatedPerformanceScore());
                
                // Mostrar los pesos calculados para cada criterio
                System.out.println("\nCriterios y sus pesos calculados:");
                for (int i = 0; i < criteriaList.size(); i++) {
                    System.out.printf("%s: %.4f%n", 
                        criteriaList.get(i).getName(), 
                        ahp.getWeights()[i]);
                }
            } else {
                System.out.println("Warning: Inconsistent comparisons detected!");
                System.out.println("Consistency Ratio: " + ahp.getConsistencyRatio());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
