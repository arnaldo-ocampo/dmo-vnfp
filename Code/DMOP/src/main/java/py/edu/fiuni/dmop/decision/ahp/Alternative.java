package py.edu.fiuni.dmop.decision.ahp;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa una alternativa en el método AHP.
 *
 * @author Marcelo Ferreira
 */
@Getter
public class Alternative {
    private String name;
    private final List<Double> criteriaWeights;

    public Alternative(String name) {
        this.name = name;
        this.criteriaWeights = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCriteriaWeight(double weight) {
        this.criteriaWeights.add(weight);
    }
}
