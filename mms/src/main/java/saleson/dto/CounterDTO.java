package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PresetStatus;
import saleson.common.util.DataUtils;
import saleson.model.Counter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounterDTO {
    private Long id;
    @JsonProperty("code")
    private String equipmentCode;
    @JsonProperty("tooling_id")
    private Long toolingId;
    @JsonProperty("tooling_code")
    private String toolingCode;

//installation_info
    @JsonProperty("preset")
    private Integer presetCount;			// Preset 설정 카운터
    @JsonProperty("installation_date")
    private String installedAt;
    @JsonProperty("installer")
    private String installedBy;
    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    private PresetStatus presetStatus;
    @JsonProperty("op_status")
    @Enumerated(EnumType.STRING)
    private OperatingStatus operatingStatus;

    @JsonProperty("shot_count")
    private Integer shotCount;

    public static CounterDTO convertToDTO(Counter counter){
        CounterDTO counterDTO = DataUtils.mapper.map(counter,CounterDTO.class);
        if(counter.getMold()!=null){
            counterDTO.setToolingId(counter.getMold().getId());
            counterDTO.setToolingCode(counter.getMold().getEquipmentCode());
        }
        return counterDTO;
    }

}
