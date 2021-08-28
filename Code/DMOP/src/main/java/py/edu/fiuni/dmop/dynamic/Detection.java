/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.dynamic;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

public interface Detection {
    /**
     * Returns the proportion of solutions that is going to be controlled to detect change in the environment with {@code isEnvironmentChanged}
     * method.
     *
     * @return the proportion of solutions that is going to be controlled to detect change in the environment with {@code isEnvironmentChanged}
     * method.
     */
    public double getProportionOfExperimentalSolutions();

    /**
     * Evaluates the n (specified by {@code getProportionOfExperimentalSolutions} * size of solution set) subset of solution set again and checks
     * whether the environment is modified.
     *
     * @param problem the array of parent solutions
     * @param solutions the array of parent solutions
     * @return {@code true} if environment is changed, {@code false} otherwise
     */
    public boolean isEnvironmentChanged(Problem problem, Solution[] solutions);
}