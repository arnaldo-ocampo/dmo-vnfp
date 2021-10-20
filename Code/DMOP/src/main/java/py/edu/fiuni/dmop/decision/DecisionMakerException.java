package py.edu.fiuni.dmop.decision;

/**
 *
 * @author Néstor Tapia, Arnaldo Ocampo
 */
public class DecisionMakerException extends Exception{
    
    /**
     * Default constructor
     */
    public DecisionMakerException(){
        super("Error al intentar obtener una solucion no dominada");
    }
    
    /**
     * Constructor
     * @param msg A short message that summarize the exception
     */
    public DecisionMakerException(String msg){
        super(msg);
    }
}
