package py.edu.fiuni.dmop.decision.promethee;

import lombok.Getter;

/**
 * Representa un criterio en el método PROMETHEE.
 *
 * @author Marcelo Ferreira
 */
@Getter
public class Criteria {
    private final String name;
    private final double weight;

    public Criteria(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

}
