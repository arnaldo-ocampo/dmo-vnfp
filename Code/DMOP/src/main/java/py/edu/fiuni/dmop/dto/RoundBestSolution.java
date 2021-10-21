package py.edu.fiuni.dmop.dto;

import lombok.Data;
import org.moeaframework.core.Solution;
import org.moeaframework.core.NondominatedPopulation;

/**
 *
 * @author Arnaldo
 */

@Data
public class RoundBestSolution {

    private String algorithm = null;
    private int roundNumber = 0;
    private int windows = 0;
    private Solution solution = null;

    public RoundBestSolution(String alg, int round, int windows, Solution sol) {
        this.algorithm = alg;
        this.roundNumber = round;
        this.windows = windows;
        this.solution = sol;
    }
}
