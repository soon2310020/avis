package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.DataUtils;
import saleson.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    @JsonProperty("code")
    private String authority;
    private String name;
    private String description;
    public static RoleDTO convertToDTO(Role role){
        RoleDTO roleDTO= DataUtils.mapper.map(role,RoleDTO.class);

        return roleDTO;
    }
}
