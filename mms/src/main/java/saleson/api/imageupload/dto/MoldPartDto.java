package saleson.api.imageupload.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.MoldPart;
import saleson.model.Part;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoldPartDto {
    private Long moldId;
    private String moldCode;
    private String categoryName;
    private String projectName;		// categoryName
    private Long partId;
    private String partCode;
    private String partName;
    private Integer cavity;
    public MoldPartDto(MoldPart model) {
        ValueUtils.map(model, this);
        if(model.getMold()!=null){
            this.moldId = model.getMold().getId();
            this.moldCode = model.getMold().getEquipmentCode();
        }
    }
}
