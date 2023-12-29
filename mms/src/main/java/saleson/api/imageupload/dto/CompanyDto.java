package saleson.api.imageupload.dto;

import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Company;
import saleson.model.Counter;

import java.util.List;

@Data
@NoArgsConstructor
public class CompanyDto {
    private Long id;
    private String companyCode;
    private String name;

    private Long numImages;
    private List<FltPlant> plants;


    public CompanyDto(Company model, Long numImages, List<FltPlant> plants) {
        ValueUtils.map(model, this);
        this.numImages = numImages;
        this.plants = plants;
    }
}
