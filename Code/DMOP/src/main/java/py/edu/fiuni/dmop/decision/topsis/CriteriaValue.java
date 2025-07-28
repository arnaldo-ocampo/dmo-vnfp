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
public class CriteriaValue {

    private Criteria criteria;
    private double value;

    public CriteriaValue(Criteria criteria, double value) {
        super();
        this.criteria = criteria;
        this.value = value;
    }

    public CriteriaValue() {
        super();
    }

}