package saleson.model.data.supplierReport;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;
import saleson.model.data.ChartData;
import saleson.model.data.PartProductionData;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SupplierProductionData {

    private Long id;
    private String code;
    private String companyName;
    private Mold mold;


    private Double normalProductionPercent;
    private Double warmUpProductionPercent;
    private Double coolDownProductionPercent;
    private Double abnormalProductionPercent;

    private Double normalEvaluationPercent;
    private Double warmUpEvaluationPercent;
    private Double coolDownEvaluationPercent;
    private Double abnormalEvaluationPercent;

    private Integer totalShot;
    private Integer totalNormalProduction;
    private Integer totalWarmUpProduction;
    private Integer totalCoolDownProduction;
    private Integer totalAbnormalProduction;

    private List<ChartData> chartNormalData= new ArrayList<>();
    private List<ChartData> chartWarmUpData= new ArrayList<>();
    private List<ChartData> chartCoolDownData= new ArrayList<>();
    private List<ChartData> chartAbnormalData= new ArrayList<>();

    private Integer numberPart;
    private List<PartProductionData> partProductionList;

    @QueryProjection
    public SupplierProductionData(Integer totalProductivity) {
        this.totalShot = totalProductivity;
    }

    @QueryProjection
    public SupplierProductionData(Long id, String code, String companyName, Integer totalShot) {
        this.id = id;
        this.code = code;
        this.companyName = companyName;
        this.totalShot = totalShot;
    }

    @QueryProjection
    public SupplierProductionData(Integer totalShot, Integer totalNormalProduction, Integer totalWarmUpProduction, Integer totalCoolDownProduction, Integer totalAbnormalProduction) {
        this.totalShot = totalShot;
        this.totalNormalProduction = totalNormalProduction;
        this.totalWarmUpProduction = totalWarmUpProduction;
        this.totalCoolDownProduction = totalCoolDownProduction;
        this.totalAbnormalProduction = totalAbnormalProduction;
    }

    @QueryProjection
    public SupplierProductionData(Long id, String code, String companyName, Integer totalShot, Integer totalNormalProduction, Integer totalWarmUpProduction, Integer totalCoolDownProduction, Integer totalAbnormalProduction) {
        this.id = id;
        this.code = code;
        this.companyName = companyName;
        this.totalShot = totalShot;
        this.totalNormalProduction = totalNormalProduction;
        this.totalWarmUpProduction = totalWarmUpProduction;
        this.totalCoolDownProduction = totalCoolDownProduction;
        this.totalAbnormalProduction = totalAbnormalProduction;
    }
    @QueryProjection
    public SupplierProductionData(Long id, String code, String companyName, Integer totalShot
            , Integer totalNormalProduction, Integer totalWarmUpProduction, Integer totalCoolDownProduction
            , Integer totalAbnormalProduction,Mold mold) {
        this.id = id;
        this.code = code;
        this.companyName = companyName;
        this.totalShot = totalShot;
        this.totalNormalProduction = totalNormalProduction;
        this.totalWarmUpProduction = totalWarmUpProduction;
        this.totalCoolDownProduction = totalCoolDownProduction;
        this.totalAbnormalProduction = totalAbnormalProduction;
        this.mold=mold;
    }
}
