package saleson.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import saleson.api.workOrder.payload.WorkOrderSummary;


@Data
public class WorkOrderResponse {
    private Page<WorkOrderDTO> workOrderList;
    private Long total;
    private Long open;
    private Long overdue;
    private Long emergency;
    private Long pm;
    private Long cm;
    private Long general;
    private Long inspection;
    private Long refurbishment;
    private Long disposal;
    private Long totalHistory ;
    private WorkOrderSummary pmSummary;
}
