package saleson.api.imageupload.dto;

import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Location;
import saleson.model.Mold;
import saleson.model.MoldPart;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class MoldDto {
    private Long id;
    private String equipmentCode;
    private Long numImages;
    private List<MoldPartDto> moldParts;

    private Long companyId;
    private String companyCode;
    private String companyName;
    private Long locationId;
    private String locationCode;
    private String locationName;
    private String areaName;

    public MoldDto(Mold model, Long numImages) {
        ValueUtils.map(model, this);
        this.numImages = numImages;
        if (model.getMoldParts() != null) {
            this.moldParts = model.getMoldParts().stream().map(moldPart -> new MoldPartDto(moldPart)).collect(Collectors.toList());
        }
        this.areaName = model.getAreaName();
    }
}
