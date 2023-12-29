package saleson.api.dataRequest;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import saleson.common.enumeration.DataCompletionRequestStatus;
import saleson.common.enumeration.DataRequestStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.RequestDataType;
import saleson.model.DataRequest;
import saleson.model.QDataRequest;
import saleson.model.QDataRequestObject;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Repository
public class DataRequestRepositoryImpl  extends QuerydslRepositorySupport implements DataRequestRepositoryCustom {
    public DataRequestRepositoryImpl() {
        super(DataRequest.class);
    }

    @Override
    public List<DataRequest> findDataRequestIdByObjectType(ObjectType objectType) {
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestObject dataRequestObject = QDataRequestObject.dataRequestObject;
        JPQLQuery query = from(dataRequest).distinct().where(dataRequest.id.in(
                JPAExpressions.select(dataRequestObject.dataRequestId).distinct()
                .from(dataRequestObject)
                .where(dataRequestObject.objectType.eq(objectType))).and(dataRequest.requestDataType.eq(RequestDataType.DATA_COMPLETION)));
        return query.fetch();
    }

    @Override
    public List<DataRequest> findAllByDueDateAndStatusIn(Instant dueDate, List<DataCompletionRequestStatus> dataRequestStatusList) {
        QDataRequest dataRequest = QDataRequest.dataRequest;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(dataRequest.dataRequestStatus.in(dataRequestStatusList))
                .and(Expressions.booleanTemplate("DATE({0}) = DATE({1})", dueDate, dataRequest.dueDate));
        return from(dataRequest).where(builder).fetch();
    }

    @Override
    public List<DataRequest> findByRequestDataTypeAndObjectIdAndObjectTypeAndStatus(RequestDataType requestDataType, Long objectId, ObjectType objectType) {
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestObject dataRequestObject = QDataRequestObject.dataRequestObject;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(dataRequest.requestDataType.eq(requestDataType))
                .and(dataRequestObject.objectId.eq(objectId))
                .and(dataRequestObject.objectType.eq(objectType))
                .and(dataRequest.dataRequestStatus.in(Arrays.asList(DataCompletionRequestStatus.REQUESTED, DataCompletionRequestStatus.IN_PROGRESS)));
        return from(dataRequest).innerJoin(dataRequestObject)
                .on(dataRequest.id.eq(dataRequestObject.dataRequestId))
                .where(builder).fetch();
    }
}
