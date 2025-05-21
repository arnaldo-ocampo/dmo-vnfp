package py.edu.fiuni.dmop.decision.topsis;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa un criterio de decisión en el proceso de toma de decisiones multicriterio.
 * Un criterio puede ser positivo (donde valores más altos son mejores) o negativo (donde valores más bajos son mejores).
 * También puede ser cualitativo, en cuyo caso utiliza valores lingüísticos en lugar de numéricos.
 *
 * @author danigpam
 * https://github.com/danigpam
 */
public class Criteria {

    private String name;           // Nombre del criterio
    private double weight;         // Peso del criterio en el proceso de decisión
    private boolean isNegative;    // Indica si el criterio es negativo (valores más bajos son mejores)
    private boolean isQualitative; // Indica si el criterio es cualitativo
    private Map<String, Double> linguisticValues; // Mapeo de valores lingüísticos a valores numéricos
    
    // Valores lingüísticos predefinidos
    public static final Map<String, Double> DEFAULT_LINGUISTIC_VALUES = new HashMap<>();
    static {
        DEFAULT_LINGUISTIC_VALUES.put("Very Poor", 1.0);
        DEFAULT_LINGUISTIC_VALUES.put("Poor", 2.0);
        DEFAULT_LINGUISTIC_VALUES.put("Medium", 3.0);
        DEFAULT_LINGUISTIC_VALUES.put("Good", 4.0);
        DEFAULT_LINGUISTIC_VALUES.put("Very Good", 5.0);
    }

    /**
     * Constructor de la clase Criteria.
     * @param name Nombre del criterio
     * @param weight Peso del criterio (debe sumar 1.0 con los demás criterios)
     * @param isNegative true si el criterio es negativo, false si es positivo
     */
    public Criteria(String name, double weight, boolean isNegative) {
        this.name = name;
        this.weight = weight;
        this.isNegative = isNegative;
        this.isQualitative = false;
        this.linguisticValues = new HashMap<>();
    }

    /**
     * Constructor para criterios cualitativos.
     * @param name Nombre del criterio
     * @param weight Peso del criterio
     * @param isNegative true si el criterio es negativo, false si es positivo
     * @param isQualitative true si el criterio es cualitativo
     */
    public Criteria(String name, double weight, boolean isNegative, boolean isQualitative) {
        this(name, weight, isNegative);
        this.isQualitative = isQualitative;
        if (isQualitative) {
            initializeDefaultLinguisticValues();
        }
    }

    /**
     * Inicializa los valores lingüísticos por defecto para criterios cualitativos.
     * Los valores van desde "Muy Malo" hasta "Excelente" con valores numéricos correspondientes.
     */
    private void initializeDefaultLinguisticValues() {
        linguisticValues.put("Muy Malo", 1.0);
        linguisticValues.put("Malo", 2.0);
        linguisticValues.put("Regular", 3.0);
        linguisticValues.put("Bueno", 4.0);
        linguisticValues.put("Muy Bueno", 5.0);
        linguisticValues.put("Excelente", 6.0);
    }

    /**
     * Agrega un valor lingüístico personalizado.
     * @param linguisticValue Valor lingüístico (ej: "Excelente")
     * @param numericValue Valor numérico correspondiente
     */
    public void addLinguisticValue(String linguisticValue, double numericValue) {
        if (!isQualitative) {
            throw new IllegalStateException("Solo los criterios cualitativos pueden tener valores lingüísticos");
        }
        linguisticValues.put(linguisticValue, numericValue);
    }

    /**
     * Obtiene el valor numérico correspondiente a un valor lingüístico.
     * @param linguisticValue Valor lingüístico a convertir
     * @return Valor numérico correspondiente
     * @throws IllegalArgumentException si el valor lingüístico no existe
     */
    public double getNumericValue(String linguisticValue) {
        if (!isQualitative) {
            throw new IllegalStateException("Este criterio no es cualitativo");
        }
        Double value = linguisticValues.get(linguisticValue);
        if (value == null) {
            throw new IllegalArgumentException("Valor lingüístico no válido: " + linguisticValue);
        }
        return value;
    }

    /**
     * @return true si el criterio es cualitativo
     */
    public boolean isQualitative() {
        return isQualitative;
    }

    /**
     * @return El nombre del criterio
     */
    public String getName() {
        return name;
    }

    /**
     * @return El peso del criterio
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Establece el peso del criterio.
     * @param weight Nuevo peso del criterio
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * @return true si el criterio es negativo (valores más bajos son mejores)
     */
    public boolean isNegative() {
        return isNegative;
    }

    /**
     * @return El mapa de valores lingüísticos a valores numéricos
     */
    public Map<String, Double> getLinguisticValues() {
        return linguisticValues;
    }
}
