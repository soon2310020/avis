package saleson.model.data;

import lombok.Data;
import saleson.common.enumeration.GraphType;

@Data
public class GraphIndexData {
    private GraphType type;
    private Integer linePosition;
    private Integer row;
    @Deprecated
    private Integer position;
    private Boolean enabled;
}
