package py.edu.fiuni.dmop.decision.ahp;

import lombok.Getter;

/**
 * Representa un criterio en el método AHP.
 *
 * @author Marcelo Ferreira
 */
@Getter
public class Criteria {
    private String name;

    public Criteria(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
