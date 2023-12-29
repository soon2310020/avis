package saleson.model.data.dashboard.otd;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.OTDStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTDDetailsData {
    private Long companyId;
    private String companyName;
    private Integer weeklyDemand;
    private Integer weeklyOutput;
    private Integer remainingCapacity;
    private Double weeklyOTD;
    private OTDStatus otdStatus;

    @QueryProjection
    public OTDDetailsData(Long companyId, String companyName, Integer weeklyOutput){
        this.companyId = companyId;
        this.companyName = companyName;
        this.weeklyOutput = weeklyOutput;
    }
}
