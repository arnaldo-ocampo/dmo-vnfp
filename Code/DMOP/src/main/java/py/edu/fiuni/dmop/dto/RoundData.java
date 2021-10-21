package py.edu.fiuni.dmop.dto;

import lombok.Data;
import org.moeaframework.core.NondominatedPopulation;

/**
 *
 * @author Arnaldo
 */

@Data
public class RoundData {

    private String algorithm = null;
    private int roundNumber = 0;
    private NondominatedPopulation solutions = null;

    public RoundData(String alg, int round, NondominatedPopulation result) {
        this.algorithm = alg;
        this.roundNumber = round;
        this.solutions = result;
    }
}
