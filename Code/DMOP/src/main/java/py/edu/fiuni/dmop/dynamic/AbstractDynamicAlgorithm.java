/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.dynamic;


import org.moeaframework.algorithm.AbstractAlgorithm;
import org.moeaframework.core.Problem;

/**
 *
 * @author Arnaldo
 */
public abstract class AbstractDynamicAlgorithm extends AbstractAlgorithm{

    /**
     * Constructs an abstract algorithm for solving the specified problem.
     *
     * @param problem the problem being solved
     */
    public AbstractDynamicAlgorithm(Problem problem) {
        super(problem);
    }
    /**
     * Detects change in the problem.
     */
    public abstract boolean detectChange();

    /**
     * steps required to take on the change of environment.
     */
    public abstract void stepOnChange();
}