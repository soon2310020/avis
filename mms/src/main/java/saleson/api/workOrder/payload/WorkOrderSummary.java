package saleson.api.workOrder.payload;

import lombok.Data;
import saleson.common.enumeration.WorkOrderType;
import saleson.service.util.NumberUtils;

@Data
public class WorkOrderSummary {
    private WorkOrderType type;
    private Long total;
    private Long open;
    private Long overdue;
    private Long completed;

    public Double getOpenPercent() {
        return total == null || total == 0 ? 0D : (open == null ? 0D : NumberUtils.roundToOneDecimalDigit((open.doubleValue()/total) * 100));
    }
    public Double getOverduePercent() {
        return total == null || total == 0 ? 0D : (overdue == null ? 0D : NumberUtils.roundToOneDecimalDigit((overdue.doubleValue()/total) * 100));
    }
    public Double getCompletedPercent() {
        return total == null || total == 0 ? 0D : (completed == null ? 0D : NumberUtils.roundToOneDecimalDigit((completed.doubleValue()/total) * 100));
    }
}
