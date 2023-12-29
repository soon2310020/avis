package saleson.api.dataRequest;

import saleson.common.enumeration.DataCompletionRequestStatus;
import saleson.common.enumeration.DataRequestStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.RequestDataType;
import saleson.model.DataRequest;

import java.time.Instant;
import java.util.List;

public interface DataRequestRepositoryCustom {
    List<DataRequest> findDataRequestIdByObjectType(ObjectType objectType);

    List<DataRequest> findAllByDueDateAndStatusIn(Instant dueDate, List<DataCompletionRequestStatus> dataRequestStatusList);

    List<DataRequest> findByRequestDataTypeAndObjectIdAndObjectTypeAndStatus(RequestDataType requestDataType, Long objectId, ObjectType objectType);
}
