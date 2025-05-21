package py.edu.fiuni.dmop.decision;

import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;

import java.util.*;

/**
 * Clase para realizar análisis de sensibilidad de los resultados de los algoritmos de decisión multicriterio.
 * Permite evaluar cómo cambios en los pesos de los criterios afectan a la solución óptima.
 */
public class SensitivityAnalysis {
    private final DecisionMaker decisionMaker;
    private final List<Alternative> alternatives;
    private final List<Criteria> criteria;
    private final Map<String, List<Double>> weightVariations;
    private final Map<String, List<Alternative>> results;
    
    public SensitivityAnalysis(DecisionMaker decisionMaker, List<Alternative> alternatives, List<Criteria> criteria) {
        this.decisionMaker = decisionMaker;
        this.alternatives = alternatives;
        this.criteria = criteria;
        this.weightVariations = new HashMap<>();
        this.results = new HashMap<>();
    }
    
    /**
     * Realiza un análisis de sensibilidad variando los pesos de los criterios
     * @param variationStep Paso de variación para los pesos (ej: 0.1 para variar en 10%)
     * @return Mapa con los resultados del análisis
     */
    public Map<String, List<Alternative>> analyze(double variationStep) {
        // Guardar pesos originales
        Map<String, Double> originalWeights = new HashMap<>();
        for (Criteria c : criteria) {
            originalWeights.put(c.getName(), c.getWeight());
        }
        
        // Variar cada criterio
        for (Criteria c : criteria) {
            List<Double> variations = new ArrayList<>();
            List<Alternative> criterionResults = new ArrayList<>();
            
            double originalWeight = c.getWeight();
            double minWeight = Math.max(0, originalWeight - 0.5);
            double maxWeight = Math.min(1, originalWeight + 0.5);
            
            for (double w = minWeight; w <= maxWeight; w += variationStep) {
                // Ajustar peso del criterio actual
                c.setWeight(w);
                
                // Ajustar pesos de otros criterios proporcionalmente
                double remainingWeight = 1 - w;
                double otherCriteriaWeight = remainingWeight / (criteria.size() - 1);
                
                for (Criteria other : criteria) {
                    if (!other.getName().equals(c.getName())) {
                        other.setWeight(otherCriteriaWeight);
                    }
                }
                
                // Ejecutar algoritmo
                try {
                    Alternative result = decisionMaker.calculateOptimalSolution();
                    variations.add(w);
                    criterionResults.add(result);
                } catch (DecisionMakerException e) {
                    // Ignorar errores y continuar con siguiente variación
                }
            }
            
            weightVariations.put(c.getName(), variations);
            results.put(c.getName(), criterionResults);
            
            // Restaurar pesos originales
            for (Criteria crit : criteria) {
                crit.setWeight(originalWeights.get(crit.getName()));
            }
        }
        
        return results;
    }
    
    /**
     * Obtiene las variaciones de peso utilizadas en el análisis
     * @return Mapa con las variaciones de peso por criterio
     */
    public Map<String, List<Double>> getWeightVariations() {
        return weightVariations;
    }
    
    /**
     * Obtiene los resultados del análisis de sensibilidad
     * @return Mapa con los resultados por criterio
     */
    public Map<String, List<Alternative>> getResults() {
        return results;
    }
    
    /**
     * Imprime un resumen del análisis de sensibilidad
     */
    public void printAnalysis() {
        System.out.println("Sensitivity Analysis Results:");
        System.out.println("============================");
        
        for (String criterionName : results.keySet()) {
            System.out.println("\nCriterion: " + criterionName);
            System.out.println("Weight variations: " + weightVariations.get(criterionName));
            System.out.println("Optimal alternatives:");
            
            List<Alternative> criterionResults = results.get(criterionName);
            for (int i = 0; i < criterionResults.size(); i++) {
                Alternative alt = criterionResults.get(i);
                double weight = weightVariations.get(criterionName).get(i);
                System.out.printf("  Weight %.2f: %s (Score: %.4f)%n", 
                    weight, alt.getName(), alt.getCalculatedPerformanceScore());
            }
        }
    }
} 