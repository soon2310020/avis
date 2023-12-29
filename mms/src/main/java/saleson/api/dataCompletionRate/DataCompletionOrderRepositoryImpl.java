package saleson.api.dataCompletionRate;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.dataCompletionRate.payload.DataCompletionGroupByType;
import saleson.model.DataCompletionOrder;
import saleson.model.QDataCompletionOrder;
import saleson.model.User;

import java.util.List;

public class DataCompletionOrderRepositoryImpl extends QuerydslRepositorySupport implements DataCompletionOrderRepositoryCustom{
    public DataCompletionOrderRepositoryImpl() {
        super(DataCompletionOrder.class);
    }

    @Override
    public List<DataCompletionOrder> findByAssignedUsersContainsAndTypeAndCompletedIsFalse(Predicate predicate) {
        QDataCompletionOrder dataCompletionOrder = QDataCompletionOrder.dataCompletionOrder;
        BooleanBuilder builder = new BooleanBuilder(predicate);
        return null;
    }
}
