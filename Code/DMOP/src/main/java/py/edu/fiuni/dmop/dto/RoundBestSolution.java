package py.edu.fiuni.dmop.dto;

import lombok.Data;
import org.moeaframework.core.Solution;
import org.moeaframework.core.NondominatedPopulation;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;

/**
 *
 * @author Arnaldo
 */

@Data
public class RoundBestSolution {

    private String algorithm = null;
    private int roundNumber = 0;
    private int windows = 0;
    private int trafficsCount = 0;
    private NetworkConditionEnum networkCondition = null;
    private Solution solution = null;

    public RoundBestSolution(String alg, int round, int windows, Solution sol, int trafficsCount, NetworkConditionEnum netCondition) {
        this.algorithm = alg;
        this.roundNumber = round;
        this.windows = windows;
        this.trafficsCount = trafficsCount;
        this.networkCondition = netCondition;
        this.solution = sol;
    }
}
