package py.edu.fiuni.dmop.decision.promethee;

/**
 * Excepción para datos incompletos en PROMETHEE.
 *
 * @author Marcelo Ferreira
 */
public class PROMETHEEIncompleteDataException extends Exception {
    @Override
    public String getMessage() {
        return "Datos incompletos en las evaluaciones de PROMETHEE.";
    }
}
