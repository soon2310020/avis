package saleson.api.imageupload.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;
import saleson.model.MoldPart;
import saleson.model.Part;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class PartDto {
    private Long id;
    private String name;
    private String partCode;
    private Long numImages;
    private List<MoldPartDto> moldParts;

    private String productName;
    private String categoryName;

    public PartDto(Part model, Long numImages) {
        ValueUtils.map(model, this);
        this.productName = model.getProjectName();
        this.numImages = numImages;
        if(model.getMoldParts()!=null){
            this.moldParts = model.getMoldParts().stream().map(moldPart -> new MoldPartDto(moldPart)).collect(Collectors.toList());
        }
    }
}
