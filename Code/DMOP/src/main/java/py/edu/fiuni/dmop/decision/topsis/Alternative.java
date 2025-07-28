package py.edu.fiuni.dmop.decision.topsis;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/*
 *
 * @author danigpam
 * https://github.com/danigpam
 *
 */
@Setter
@Getter
public class Alternative {

    private String name;
    private List<CriteriaValue> criteriaValues;
    private double calculatedPerformanceScore;

    public Alternative(String name, List<CriteriaValue> criteriaValues) {
        super();
        this.name = name;
        this.criteriaValues = criteriaValues;
    }

    public Alternative(String name) {
        super();
        this.name = name;
    }

    public Alternative() {
        super();
    }

    public void addCriteriaValue(CriteriaValue criteriaValue) {
        if (criteriaValues == null) {
            criteriaValues = new ArrayList<CriteriaValue>();
        }
        this.criteriaValues.add(criteriaValue);
    }

    public void addCriteriaValue(Criteria criteria, double value) {
        if (criteriaValues == null) {
            criteriaValues = new ArrayList<CriteriaValue>();
        }
        this.criteriaValues.add(new CriteriaValue(criteria, value));
    }

}