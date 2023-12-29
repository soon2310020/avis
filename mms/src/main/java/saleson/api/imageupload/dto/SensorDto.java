package saleson.api.imageupload.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Counter;
import saleson.model.Machine;

@Data
@NoArgsConstructor
public class SensorDto {
    private Long id;
    private String equipmentCode;

    private Long numImages;

//    private Long toolingId;
    private String toolingCode;

//    private Long terminalId;
    private String terminalCode;

    private Long locationId;
    private String locationCode;
    private String locationName;

    public SensorDto(Counter model, Long numImages, String terminalCode) {
        ValueUtils.map(model, this);
        this.numImages = numImages;
        this.terminalCode = terminalCode;
        if (model.getMold() != null)
            this.toolingCode = model.getMold().getEquipmentCode();
    }
}
