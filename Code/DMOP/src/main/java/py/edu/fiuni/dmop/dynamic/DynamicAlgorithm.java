/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.dynamic;

import org.moeaframework.core.Algorithm;

public interface DynamicAlgorithm extends Algorithm{
    /**
     * steps required to take on the change of environment.
     */
    public void stepOnChange();
}
