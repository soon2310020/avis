package saleson.model.clone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    private String authority;
    private String name;
    private String description;
    @Column(length = 1000)
    private String roleGraphs;
    @Column(length = 2000)
    private String roleMenus;
    private String roleMenuIds;
}
