package py.edu.fiuni.dmop.dto;

import py.edu.fiuni.dmop.dto.NFVdto.Link;
import py.edu.fiuni.dmop.dto.NFVdto.Node;
import py.edu.fiuni.dmop.dto.NFVdto.Traffic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ResultGraphMap implements Serializable {

    private static final long serialVersionUID = -8626797840944687981L;

    private Map<String, Node> nodesMap;
    private Map<String, Link> linksMap;
    private List<Traffic> traffics;

    public Map<String, Node> getNodesMap() {
        return nodesMap;
    }

    public void setNodesMap(Map<String, Node> nodesMap) {
        this.nodesMap = nodesMap;
    }

    public Map<String, Link> getLinksMap() {
        return linksMap;
    }

    public void setLinksMap(Map<String, Link> linksMap) {
        this.linksMap = linksMap;
    }

    public List<Traffic> getTraffics() {
        return traffics;
    }

    public void setTraffics(List<Traffic> traffics) {
        this.traffics = traffics;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResultGraphMap{");
        sb.append("nodesMap=").append(nodesMap);
        sb.append(", linksMap=").append(linksMap);
        sb.append('}');
        return sb.toString();
    }
}
