/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.edu.fiuni.dmop.decision.topsis;

/*
 * 
 * @author danigpam
 * https://github.com/danigpam
 * 
 */
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

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
