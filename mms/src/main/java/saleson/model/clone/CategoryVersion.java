package saleson.model.clone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.support.VersionAudit;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CategoryVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private boolean enabled;
    private Long parentId;
}
