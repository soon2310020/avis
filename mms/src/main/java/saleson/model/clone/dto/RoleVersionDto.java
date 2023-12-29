package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleVersionDto {
    private Long id;
    private String authority;
    private String name;
    private String description;
    private String roleGraphs;
    private String roleMenus;
    private Long originId;
    private MMSMenuAccessDto mmsMenuAccessDto;
    private AdminMenuAccessDto adminMenuAccessDto;
}
