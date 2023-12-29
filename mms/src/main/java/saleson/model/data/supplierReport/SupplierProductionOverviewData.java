package saleson.model.data.supplierReport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductionOverviewData {
    private Double warmUpProductionPercent;
    private Double normalProductionPercent;
    private Double coolDownProductionPercent;
    private Double abnormalProductionPercent;

    private List<SupplierProductionData> top5Supplier;
    private Long totalElements;

    private Integer totalShot; // produced part quantity, max productivity of toolings
    private Integer totalNormalProduction;
    private Integer totalWarmUpProduction;
    private Integer totalCoolDownProduction;
    private Integer totalAbnormalProduction;

    public SupplierProductionOverviewData(Double normalProduction, Double warmUpProduction, Double coolDownProduction, Double abnormalProduction) {
        this.normalProductionPercent = normalProduction;
        this.warmUpProductionPercent = warmUpProduction;
        this.coolDownProductionPercent = coolDownProduction;
        this.abnormalProductionPercent = abnormalProduction;
    }

    public SupplierProductionOverviewData(Integer totalShot, Integer totalNormalProduction, Integer totalWarmUpProduction, Integer totalCoolDownProduction, Integer totalAbnormalProduction) {
        this.totalShot = totalShot;
        this.totalNormalProduction = totalNormalProduction;
        this.totalWarmUpProduction = totalWarmUpProduction;
        this.totalCoolDownProduction = totalCoolDownProduction;
        this.totalAbnormalProduction = totalAbnormalProduction;
    }
}
