package saleson.api.workOrder;

import com.emoldino.api.common.resource.base.workorder.enumeration.WorkOrderParticipantType;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.User;
import saleson.model.WorkOrder;
import saleson.model.WorkOrderUser;

import java.util.List;

public interface WorkOrderUserRepository  extends JpaRepository<WorkOrderUser, Long>, QuerydslPredicateExecutor<WorkOrderUser> {
    void deleteAllByWorkOrderId(Long workOrderId);

  boolean existsByWorkOrderAndUserAndParticipantType (WorkOrder workOrder, User user, WorkOrderParticipantType workOrderParticipantType);
  void deleteAllByWorkOrderIdAndParticipantType(Long workOrderId, WorkOrderParticipantType workOrderParticipantType);
}
