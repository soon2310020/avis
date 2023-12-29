package saleson.api.imageupload.dto;

import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Counter;
import saleson.model.Location;

import java.util.List;

@Data
@NoArgsConstructor
public class PlantDto {
    private Long id;
    private String name;
    private String locationCode;
    private Long numImages;

    private Long companyId;
    private String companyCode;
    private String companyName;

    public PlantDto(Location model, Long numImages) {
        ValueUtils.map(model, this);
        this.numImages = numImages;
    }
}
