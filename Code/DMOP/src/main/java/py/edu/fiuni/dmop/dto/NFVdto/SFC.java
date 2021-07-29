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

@Data
public class SFC implements Serializable {
    
    private static final long serialVersionUID = 1L;
    

    //Secuencia de VNFs - Cadena de Servicio
    private List<Vnf> vnfs = new ArrayList<>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SFC{");
        sb.append("vnfs=").append(vnfs);
        sb.append('}');
        return sb.toString();
    }
}
