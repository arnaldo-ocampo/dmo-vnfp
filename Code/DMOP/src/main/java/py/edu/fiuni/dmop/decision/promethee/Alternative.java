package py.edu.fiuni.dmop.decision.promethee;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa una alternativa en el método PROMETHEE.
 *
 * @author Marcelo Ferreira
 */
@Getter
public class Alternative {
    private String name;
    private final Map<Criteria, Double> evaluations;

    public Alternative(String name) {
        this.name = name;
        this.evaluations = new HashMap<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addEvaluation(Criteria criteria, double value) {
        this.evaluations.put(criteria, value);
    }

}
