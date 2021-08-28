package py.edu.fiuni.dmop.dto.NFVdto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Vnf implements Serializable {

    private static final long serialVersionUID = 1L; // -4346254958072354423L;

    // VNF Id
    private String id = null;

    // VNF Type
    private String type = null;

    // CPU Requirement of the VNF (Cores number)
    private int resourceCPU = 0;

    // RAM requirement of the VNF (In GB)
    private int resourceRAM = 0;

    /**
     *
     * @param id
     * @param type
     * @param resourceCPU
     * @param resourceRAM
     */
    public Vnf(String id, String type, int resourceCPU, int resourceRAM) {
        this.id = id;
        this.type = type;
        this.resourceCPU = resourceCPU;
        this.resourceRAM = resourceRAM;
    }

    /**
     * Copy constructor
     *
     * @param vnf The VNF to be copied
     */
    public Vnf(Vnf vnf) {
        this.id = vnf.getId();
        this.type = vnf.getType();
        this.resourceCPU = vnf.getResourceCPU();
        this.resourceRAM = vnf.getResourceRAM();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vnf{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type=").append(type);
        sb.append(", resourceCPU=").append(resourceCPU);
        sb.append(", resourceRAM=").append(resourceRAM);
        sb.append('}');
        return sb.toString();
    }
}
