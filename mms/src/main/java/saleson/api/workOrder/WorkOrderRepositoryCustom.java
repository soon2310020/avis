package saleson.api.workOrder;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.enumeration.WorkOrderType;
import saleson.dto.WorkOrderDTO;
import saleson.model.WorkOrder;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepositoryCustom {
    List<WorkOrderDTO> searchWorkOrder(Predicate predicate, Pageable pageable);
    List<WorkOrderDTO> searchWorkOrderWithAssetIds(Predicate predicate, Pageable pageable, List<Long> assetIds);
    long countByPredicate(Predicate predicate);
    long countByPredicateAndAssetIdsIn(Predicate predicate, List<Long> assetIds);

    Optional<WorkOrder> findFirstByStatusAndAssetIdOrderByCompletedOnDesc(WorkOrderStatus status, Long moldId);

    Optional<WorkOrder> findFirstByStatusNotInAndAssetIdAndOrderType(List<WorkOrderStatus> statuses, Long moldId, WorkOrderType type);

    Optional<WorkOrder> findFirstByStatusAndAssetIdAndOrderType(WorkOrderStatus status,Long moldId,WorkOrderType type);

    Long countByStatusInAndAssetId(List<WorkOrderStatus> statuses, Long moldId);

    List<WorkOrderDTO> getWorkOrderListForExport(Predicate predicate);

    Long countByOrderTypeAndAssetIdAndAssetType(WorkOrderType workOrderType, Long assetId, ObjectType assetType);

    Optional<WorkOrderDTO> findOptionalById(Long id);
}
