package py.edu.fiuni.dmop.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.moeaframework.core.NondominatedPopulation;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;
import py.edu.fiuni.dmop.problem.DynamicVNFPlacementProblem;
import py.edu.fiuni.dmop.util.NetworkConditionEnum;

/**
 *
 * @author Arnaldo
 */
@Data
public class WindowsData {

    private DynamicVNFPlacementProblem problem = null;
    private int windowsNumber = 0;
    private List<Traffic> traffics = null;
    private NetworkConditionEnum networkCondition = null;

    /**
     * List of Solutions resulted from a given run for the specified windows
     */
    private List<RoundData> results = null;

    /**
     * 
     * @param problem
     * @param windows
     * @param traffics
     * @param condition 
     */
    public WindowsData(DynamicVNFPlacementProblem problem, int windows, List<Traffic> traffics, NetworkConditionEnum condition) {
        this.windowsNumber = windows;
        this.problem = problem;
        this.traffics = traffics;
        this.networkCondition = condition;
        this.results = new ArrayList<>();
    }

    /**
     * 
     * @param roundNumber
     * @param result 
     */
    public void addRoundResult(String alg, int roundNumber, NondominatedPopulation result) {
        this.results.add(new RoundData(alg, roundNumber, result));
    }
}
