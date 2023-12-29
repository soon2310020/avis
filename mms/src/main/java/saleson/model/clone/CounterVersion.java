package saleson.model.clone;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class CounterVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    private String counterId;
    private Long toolingId;
    private Integer presetCount;
    private String installedAt;
    private String installedBy;
//    private String photo;
    @Lob
    private String memo;
    @Enumerated
    private EquipmentStatus equipmentStatus;

}
