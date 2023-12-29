package saleson.api.dataRequest.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import saleson.common.enumeration.DataCompletionRequestStatus;
import saleson.common.enumeration.RequestDataType;
import saleson.common.util.SecurityUtils;
import saleson.model.QDataRequest;
import saleson.model.QDataRequestUser;

import java.util.Arrays;

@Data
public class DataRequestPayload  {
    private String type;

    private Boolean isCreatedByMe;

    private Boolean isAssignedToMe;

    public Predicate getPredicate() {
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestUser dataRequestUser = QDataRequestUser.dataRequestUser;
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.isNotBlank(type) && "History".equals(type)) {
            builder.and(dataRequest.dataRequestStatus.in(Arrays.asList(DataCompletionRequestStatus.CANCELLED,
                    DataCompletionRequestStatus.DECLINED,
                    DataCompletionRequestStatus.COMPLETED)));
        } else {
            builder.and(dataRequest.dataRequestStatus.notIn(Arrays.asList(DataCompletionRequestStatus.CANCELLED,
                    DataCompletionRequestStatus.DECLINED,
                    DataCompletionRequestStatus.COMPLETED)));
            if ("DATA_COMPLETION".equals(type)) {
                builder.and(dataRequest.requestDataType.eq(RequestDataType.DATA_COMPLETION));
            } else if ("DATA_REGISTRATION".equals(type)) {
                builder.and(dataRequest.requestDataType.eq(RequestDataType.DATA_REGISTRATION));
            }
        }

        if (isCreatedByMe != null && isCreatedByMe) {
            builder.and(dataRequest.createdById.eq(SecurityUtils.getUserId()));
        }

        if (isAssignedToMe != null && isAssignedToMe) {
            builder.and(dataRequest.id.in(
                    JPAExpressions.select(dataRequestUser.dataRequestId)
                    .from(dataRequestUser)
                    .where(dataRequestUser.userId.eq(SecurityUtils.getUserId()))
                    )
            );
        }

        return builder;
    }
}
