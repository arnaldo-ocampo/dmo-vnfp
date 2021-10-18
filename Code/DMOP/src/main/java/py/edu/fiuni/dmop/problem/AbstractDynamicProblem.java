package py.edu.fiuni.dmop.problem;


import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

// TODO: Confirm we can implement AbstractProblem instead of Problem
public abstract class AbstractDynamicProblem implements Problem {
    protected final double severityOfChange;
    private final int frequencyOfChange;
    private int counter = 0;

    public AbstractDynamicProblem(double severityOfChange, int frequencyOfChange) {
        this.severityOfChange = severityOfChange;
        this.frequencyOfChange = frequencyOfChange;
    }

    @Override
    public void evaluate(Solution solution){
        System.out.println("NO DEBERIA ESTAR LLAMANDOSE POR AHORA!!!!!!");
        if(++counter>frequencyOfChange) {
            counter = 0;
            changeEnvironment();
        }
    }

    protected abstract void changeEnvironment();
}
