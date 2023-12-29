package saleson.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.GraphType;

import javax.persistence.*;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleGraph {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long roleId;

    @Enumerated(EnumType.STRING)
    private GraphType graphType;

    public RoleGraph(Long roleId, GraphType graphType){
        this.roleId = roleId;
        this.graphType = graphType;
    }
}
