package py.edu.fiuni.dmop.decision.topsis;

/**
 * Clase que representa el valor de un criterio para una alternativa específica.
 * Este valor puede ser numérico o lingüístico, dependiendo del tipo de criterio.
 *
 * @author danigpam
 * https://github.com/danigpam
 */
public class CriteriaValue {
    private Criteria criteria;  // El criterio al que pertenece este valor
    private double value;       // El valor numérico del criterio
    private String linguisticValue; // El valor lingüístico (si el criterio es cualitativo)

    /**
     * Constructor para valores numéricos.
     * @param criteria El criterio al que pertenece este valor
     * @param value El valor numérico
     */
    public CriteriaValue(Criteria criteria, double value) {
        this.criteria = criteria;
        this.value = value;
        this.linguisticValue = null;
    }

    /**
     * Constructor para valores lingüísticos.
     * @param criteria El criterio al que pertenece este valor
     * @param linguisticValue El valor lingüístico
     */
    public CriteriaValue(Criteria criteria, String linguisticValue) {
        if (!criteria.isQualitative()) {
            throw new IllegalArgumentException("El criterio debe ser cualitativo para usar valores lingüísticos");
        }
        this.criteria = criteria;
        this.linguisticValue = linguisticValue;
        this.value = criteria.getNumericValue(linguisticValue);
    }

    /**
     * @return El criterio al que pertenece este valor
     */
    public Criteria getCriteria() {
        return criteria;
    }

    /**
     * @return El valor numérico del criterio
     */
    public double getValue() {
        return value;
    }

    /**
     * Establece el valor numérico del criterio.
     * @param value Nuevo valor numérico
     */
    public void setValue(double value) {
        this.value = value;
        this.linguisticValue = null;
    }

    /**
     * @return El valor lingüístico del criterio, o null si es numérico
     */
    public String getLinguisticValue() {
        return linguisticValue;
    }

    /**
     * Establece el valor lingüístico del criterio.
     * @param linguisticValue Nuevo valor lingüístico
     */
    public void setLinguisticValue(String linguisticValue) {
        if (!criteria.isQualitative()) {
            throw new IllegalStateException("El criterio debe ser cualitativo para usar valores lingüísticos");
        }
        this.linguisticValue = linguisticValue;
        this.value = criteria.getNumericValue(linguisticValue);
    }
}
