package saleson.api.workOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.WorkOrderAsset;

import java.util.List;

public interface WorkOrderAssetRepository extends JpaRepository<WorkOrderAsset, Long>, QuerydslPredicateExecutor<WorkOrderAsset> {
    void deleteAllByWorkOrderId(Long workOrderId);

    List<WorkOrderAsset> findAllByWorkOrderIdIn(List<Long> workOrderIdList);
}
