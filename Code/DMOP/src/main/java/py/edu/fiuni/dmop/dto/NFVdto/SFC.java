package py.edu.fiuni.dmop.dto.NFVdto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * // TODO: UPDATE INFORMATION
 * Original file from:   github project url
 * @author Arnaldo
 */

/**
 * Represents a Service Function Chain with its list of required VNFs.
 * @author Arnaldo Ocampo, Nésto Tapia.
 */
@Data
public class SFC implements Serializable {
    
    private static final long serialVersionUID = 1L;
    

    // List of VNFs needed for this service chain
    private List<Vnf> vnfs = new ArrayList<>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SFC{vnfs=").append(vnfs).append('}');
        return sb.toString();
    }
}
