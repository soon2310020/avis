package saleson.api.workOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.model.WorkOrder;

import java.time.Instant;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, QuerydslPredicateExecutor<WorkOrder>, WorkOrderRepositoryCustom {
    WorkOrder findFirstByStatusAndWorkOrderIdContainsAndEndAfter(WorkOrderStatus status, String id, Instant time);

    WorkOrder findFirstByStatusAndWorkOrderIdContainsAndEndBefore(WorkOrderStatus status, String id, Instant time);

    WorkOrder findFirstByOriginalIdOrderByIdDesc(Long originalId);
}
