package py.edu.fiuni.dmop.decision.ahp;

/**
 * Excepción para datos incompletos en AHP.
 *
 * @author Marcelo Ferreira
 */
public class AHPIncompleteDataException extends Exception {
    @Override
    public String getMessage() {
        return "Datos incompletos en la matriz de comparación de criterios.";
    }
}
