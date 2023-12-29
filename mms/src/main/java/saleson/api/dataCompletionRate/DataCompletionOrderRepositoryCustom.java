package saleson.api.dataCompletionRate;

import com.querydsl.core.types.Predicate;
import saleson.api.dataCompletionRate.payload.DataCompletionGroupByType;
import saleson.model.DataCompletionOrder;
import saleson.model.User;

import java.util.List;

public interface DataCompletionOrderRepositoryCustom {
    List<DataCompletionOrder> findByAssignedUsersContainsAndTypeAndCompletedIsFalse(Predicate predicate);
}
