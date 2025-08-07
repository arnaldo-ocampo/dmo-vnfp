package py.edu.fiuni.dmop.decision.topsis;

import lombok.Getter;
import lombok.Setter;

/*
 *
 * @author danigpam
 * https://github.com/danigpam
 *
 */
@Setter
@Getter
public class Criteria {

    private String name;
    private double weight;
    private boolean negative;

    public Criteria() {
        super();
    }

    public Criteria(String name, double weight) {
        super();
        this.name = name;
        this.weight = weight;
    }

    public Criteria(String name, double weight, boolean negative) {
        super();
        this.name = name;
        this.weight = weight;
        this.negative = negative;
    }

}