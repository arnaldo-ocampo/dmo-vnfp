package py.edu.fiuni.dmop.decision;

import py.edu.fiuni.dmop.decision.topsis.Alternative;
import py.edu.fiuni.dmop.decision.topsis.Criteria;
import java.util.List;

/**
 * Clase base abstracta para todos los algoritmos de decisión multicriterio.
 * Define la interfaz común y métodos de utilidad para todos los algoritmos.
 * 
 * @author Arnaldo Ocampo, Néstor Tapia
 */
public abstract class DecisionMaker {
        
    /**
     * Calcula la mejor solución posible dado un conjunto de alternativas y criterios.
     * @return La mejor alternativa resultante de aplicar el algoritmo específico
     * @throws DecisionMakerException si ocurre un error durante el cálculo
     */
    public abstract Alternative calculateOptimalSolution() throws DecisionMakerException;
    
    /**
     * Valida que la suma de los pesos de los criterios sea igual a 1.0.
     * Si todos los pesos son 0, asigna pesos iguales automáticamente.
     * 
     * @param criteria Lista de criterios a validar
     * @throws DecisionMakerException si los pesos no suman 1.0
     */
    protected void validateWeights(List<Criteria> criteria) throws DecisionMakerException {
        // Verificar si todos los pesos son 0 (igual importancia)
        boolean allWeightsZero = criteria.stream()
                .allMatch(c -> c.getWeight() == 0);
                
        if (allWeightsZero) {
            // Si todos los pesos son 0, asignar pesos iguales
            double equalWeight = 1.0 / criteria.size();
            for (Criteria c : criteria) {
                c.setWeight(equalWeight);
            }
            return;
        }
        
        // Si no todos son 0, verificar que sumen 1.0
        double sum = criteria.stream()
                .mapToDouble(Criteria::getWeight)
                .sum();
        
        if (Math.abs(sum - 1.0) > 0.0001) {
            throw new DecisionMakerException("La suma de los pesos de los criterios debe ser 1.0, suma actual: " + sum);
        }
    }
    
    /**
     * Valida que todas las alternativas tengan valores para todos los criterios.
     * 
     * @param alternatives Lista de alternativas a validar
     * @param criteria Lista de criterios contra los cuales validar
     * @throws DecisionMakerException si la validación falla
     */
    protected void validateAlternatives(List<Alternative> alternatives, List<Criteria> criteria) 
            throws DecisionMakerException {
        if (alternatives == null || alternatives.isEmpty()) {
            throw new DecisionMakerException("La lista de alternativas no puede ser nula o vacía");
        }
        
        if (criteria == null || criteria.isEmpty()) {
            throw new DecisionMakerException("La lista de criterios no puede ser nula o vacía");
        }
        
        for (Alternative alt : alternatives) {
            if (alt.getCriteriaValues() == null || alt.getCriteriaValues().size() != criteria.size()) {
                throw new DecisionMakerException(
                    String.format("La alternativa %s debe tener valores para todos los %d criterios", 
                    alt.getName(), criteria.size()));
            }
        }
    }
    
    /**
     * Normaliza un valor entre un mínimo y un máximo.
     * Si el criterio es negativo (menor es mejor), invierte la normalización.
     * 
     * @param value Valor a normalizar
     * @param min Valor mínimo
     * @param max Valor máximo
     * @param isNegative Indica si el criterio es negativo (menor es mejor)
     * @return Valor normalizado
     */
    protected double normalizeValue(double value, double min, double max, boolean isNegative) {
        if (max == min) return 0.0;
        
        if (isNegative) {
            return (max - value) / (max - min);
        } else {
            return (value - min) / (max - min);
        }
    }
}
