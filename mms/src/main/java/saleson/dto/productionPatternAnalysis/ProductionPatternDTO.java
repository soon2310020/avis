package saleson.dto.productionPatternAnalysis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ProductionPatternDTO {
    @JsonProperty("index_col")
    private Long indexCol;
    @JsonProperty("risk")
    private String risk;
    @JsonProperty("shot_count")
    private Long shotCount;
    @JsonProperty("time")
    private String time;
    @JsonProperty("date")
    private String date;
    @JsonProperty("det_sect")
    private Double detSect;

    public ProductionPatternDTO(Long indexCol, String time, String date){
        this.indexCol = indexCol;
        this.time = time;
        this.date = date;
    }
}
