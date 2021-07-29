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
public class TopsisIncompleteAlternativeDataException extends Exception {

    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        return "Incomplete data used to calculate topsis. Ensure that all alternatives have a score for each of the same criteria.";
    }
}
