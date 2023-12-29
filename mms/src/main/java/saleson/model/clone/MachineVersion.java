package saleson.model.clone;

import lombok.*;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MachineVersion extends VersionAudit {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name = "MACHINE_ID")
    private String machineId;
   
    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean enabled;
    
    @Column(name = "COMPANY_ID")
    private Long companyId;
    
    @Column(name = "LOCATION_ID")
    private Long locationId;
    
    private String line;
    
    @Column(name = "MACHINE_MAKER")
    private String machineMaker;
    
    @Column(name = "MACHINE_TYPE")
    private String machineType;
    
    @Column(name = "MACHINE_MODEL")
    private String machineModel;
    
    @Column(name = "MACHINE_TONNAGE")
    private Integer machineTonnage;
}
