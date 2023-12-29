package saleson.dto.productionPatternAnalysis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class InputCdataDTO {
    @JsonProperty("CI")
    private String ci;
    @JsonProperty("CT")
    private Double ct;
    @JsonProperty("RT")
    private String rt;
    @JsonProperty("TAV")
    private Integer tav;        // 평균온도 (temp의 온도 평균)
    @JsonProperty("CTT")
    private String ctt;            // CT Table
    @JsonProperty("LST")
    private String lst;
    @JsonProperty("TEMP")
    private String temp;        // 온도데이터
    @JsonProperty("TFF")
    private String tff;            // 온도1 시간

    public String useRtAsKey() {
        return rt;
    }

}
