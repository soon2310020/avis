package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineVersionDto {
    private Long id;
    private String machineId;
    private String line;
    private String companyName;
    private String locationName;
    private String machineMaker;
    private String machineType;
    private String machineModel;
    private Integer machineTonnage;
    private boolean enabled;
}
