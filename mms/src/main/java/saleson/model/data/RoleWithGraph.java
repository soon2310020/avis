package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.GraphType;
import saleson.model.Role;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleWithGraph {
    private Role role;
    private List<GraphType> graphTypes;
}
