package py.edu.fiuni.dmop.decision.topsis;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una alternativa en el proceso de toma de decisiones multicriterio.
 * Una alternativa es una opción que se evalúa según múltiples criterios.
 * Cada alternativa tiene un conjunto de valores que representan su desempeño en cada criterio.
 *
 * @author danigpam
 * https://github.com/danigpam
 */
public class Alternative {
    private String name;                           // Nombre de la alternativa
    private List<CriteriaValue> criteriaValues;    // Valores de los criterios para esta alternativa
    private double calculatedPerformanceScore;     // Puntuación calculada por el algoritmo de decisión

    /**
     * Constructor de la clase Alternative.
     * @param name Nombre de la alternativa
     */
    public Alternative(String name) {
        this.name = name;
        this.criteriaValues = new ArrayList<>();
        this.calculatedPerformanceScore = 0.0;
    }

    /**
     * Agrega un valor para un criterio específico.
     * @param criteriaValue Valor del criterio a agregar
     */
    public void addCriteriaValue(CriteriaValue criteriaValue) {
        criteriaValues.add(criteriaValue);
    }

    /**
     * Obtiene el valor de un criterio específico.
     * @param criteriaName Nombre del criterio
     * @return El valor del criterio, o null si no existe
     */
    public CriteriaValue getCriteriaValue(String criteriaName) {
        for (CriteriaValue value : criteriaValues) {
            if (value.getCriteria().getName().equals(criteriaName)) {
                return value;
            }
        }
        return null;
    }

    /**
     * @return El nombre de la alternativa
     */
    public String getName() {
        return name;
    }

    /**
     * @return La lista de valores de los criterios
     */
    public List<CriteriaValue> getCriteriaValues() {
        return criteriaValues;
    }

    /**
     * @return La puntuación calculada por el algoritmo de decisión
     */
    public double getCalculatedPerformanceScore() {
        return calculatedPerformanceScore;
    }

    /**
     * Establece la puntuación calculada por el algoritmo de decisión.
     * @param score Nueva puntuación
     */
    public void setCalculatedPerformanceScore(double score) {
        this.calculatedPerformanceScore = score;
    }
}
