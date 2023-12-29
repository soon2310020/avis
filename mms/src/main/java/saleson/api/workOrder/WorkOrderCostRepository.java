package saleson.api.workOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.WorkOrderCost;

import java.util.List;

public interface WorkOrderCostRepository  extends JpaRepository<WorkOrderCost, Long> {
    List<WorkOrderCost> findAllByWorkOrderId(Long workOrderId);

    void deleteAllByWorkOrderId(Long workOrderId);

    List<WorkOrderCost> findAllByWorkOrderIdIn(List<Long> workOrderIdList);
}
