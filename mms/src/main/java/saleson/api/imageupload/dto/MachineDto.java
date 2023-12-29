package saleson.api.imageupload.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Machine;
import saleson.model.Mold;
import saleson.model.MoldPart;

import java.util.List;

@Data
@NoArgsConstructor
public class MachineDto {
    private Long id;
    private String machineCode;
    private String machineType;

    private Long numImages;

    private Long companyId;
    private String companyCode;
    private String companyName;
    private Long locationId;
    private String locationCode;
    private String locationName;

    public MachineDto(Machine model, Long numImages) {
        ValueUtils.map(model, this);
        this.numImages = numImages;
    }
}
