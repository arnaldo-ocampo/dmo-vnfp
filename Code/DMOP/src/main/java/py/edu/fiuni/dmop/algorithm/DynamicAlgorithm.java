package py.edu.fiuni.dmop.algorithm;

import org.moeaframework.core.Algorithm;

public interface DynamicAlgorithm extends Algorithm{
    /**
     * steps required to take on the change of environment.
     */
    public void stepOnChange();
}
