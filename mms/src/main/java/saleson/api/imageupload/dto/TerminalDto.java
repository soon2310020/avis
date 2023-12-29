package saleson.api.imageupload.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;
import saleson.model.MoldPart;
import saleson.model.Terminal;

import java.util.List;

@Data
@NoArgsConstructor
public class TerminalDto {
    private Long id;
    private String equipmentCode;
    private Long numImages;

    private Long companyId;
    private String companyCode;
    private String companyName;
    private Long locationId;
    private String locationCode;
    private String locationName;

    public TerminalDto(Terminal model, Long numImages) {
        ValueUtils.map(model, this);
        this.numImages = numImages;
    }
}
