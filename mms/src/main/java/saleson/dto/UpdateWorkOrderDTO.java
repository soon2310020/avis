package saleson.dto;

import lombok.Data;
import saleson.common.enumeration.WorkOrderStatus;

@Data
public class UpdateWorkOrderDTO {
    private WorkOrderStatus status;
    private String reason;
    private Boolean approved;
}
