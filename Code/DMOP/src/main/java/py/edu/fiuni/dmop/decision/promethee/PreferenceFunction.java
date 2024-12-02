package py.edu.fiuni.dmop.decision.promethee;

/**
 * Representa una función de preferencia para un criterio en el método PROMETHEE.
 *
 * @author Marcelo Ferreira
 */
public class PreferenceFunction {
    private final double thresholdPreference;
    private final double thresholdIndifference;

    public PreferenceFunction(double thresholdPreference, double thresholdIndifference) {
        this.thresholdPreference = thresholdPreference;
        this.thresholdIndifference = thresholdIndifference;
    }

    public double calculatePreference(double d) {
        if (d <= thresholdIndifference) return 0;
        else if (d <= thresholdPreference) {
            return (d - thresholdIndifference) / (thresholdPreference - thresholdIndifference);
        } else {
            return 1;
        }
    }
}
