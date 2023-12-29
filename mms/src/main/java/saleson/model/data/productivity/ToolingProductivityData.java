package saleson.model.data.productivity;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;
import saleson.model.data.ChartData;
import saleson.model.data.MoldCapacityReportData;
import saleson.model.data.PartProductionData;

import java.util.List;

@Data
@Builder
@AllArgsConstructor @NoArgsConstructor
public class ToolingProductivityData{
    private Long moldId;
    private String moldCode;
    private Mold mold;
    private String companyCode;
    private Integer producedQuantity;
    private Integer maxCapacity;
    private Double percentageProductivity;
    private Integer dailyMaxCapacity;
    private Double meetTargetDay;
    private Double lowProductivityDay;
    private Double noOperationDay;
    private Double trend;
    private Double percentageTotalProductivity;
    private List<ChartData> chartData;
    private List<MoldCapacityReportData> capacityReportDataList;
    private Integer availableDowntime = 0;


    private Long id;
    private String code;
    private String name;
//    private Company company;

    private Integer numberPart;
    private List<PartProductionData> partProductionList;

    @QueryProjection
    public ToolingProductivityData(Long moldId, String moldCode, String companyCode, Integer producedQuantity){
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.id = moldId;
        this.code = moldCode;
        this.companyCode = companyCode;
        this.producedQuantity = producedQuantity;
//        this.maxCapacity = maxCapacity;
//        this.dailyMaxCapacity = dailyMaxCapacity;
    }

    @QueryProjection
    public ToolingProductivityData(Long moldId, Integer maxCapacity, Integer dailyMaxCapacity){
        this.moldId = moldId;
        this.id = moldId;
        this.maxCapacity = maxCapacity;
        this.dailyMaxCapacity = dailyMaxCapacity;
    }

    @QueryProjection
    public ToolingProductivityData(Long moldId, String moldCode, Mold mold, String companyCode, Integer producedQuantity
            , Double percentageProductivity, Integer dailyMaxCapacity, Integer maxCapacity
            , Integer numberPart){
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.id = moldId;
        this.code = moldCode;
        this.mold = mold;
        this.companyCode = companyCode;
        this.producedQuantity = producedQuantity;
        this.percentageProductivity = percentageProductivity;
        this.dailyMaxCapacity = dailyMaxCapacity;
        this.maxCapacity = maxCapacity;
        this.numberPart = numberPart;
    }
    @QueryProjection
    public ToolingProductivityData(Long id, String code, String name, Integer producedQuantity, Double percentageProductivity, Integer dailyMaxCapacity, Integer maxCapacity){
        this.id = id;
        this.code = code;
        this.name = name;
//        this.company = company;
//        this.companyCode = companyCode;
        this.producedQuantity = producedQuantity;
        this.percentageProductivity = percentageProductivity;
        this.dailyMaxCapacity = dailyMaxCapacity;
        this.maxCapacity = maxCapacity;
    }

}
