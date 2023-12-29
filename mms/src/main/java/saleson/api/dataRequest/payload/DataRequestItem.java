package saleson.api.dataRequest.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.DataCompletionRequestStatus;
import saleson.common.enumeration.RequestDataType;
import saleson.model.DataRequest;
import saleson.model.User;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataRequestItem {
    private Long id;
    private String requestId;
    private RequestDataType requestType;
    private Long createdById;
    private User createdBy;
    private List<User> assignedTo;

    private Instant dueDate;
    private int progress;
    private DataCompletionRequestStatus dataRequestStatus;


    public DataRequestItem(Long id, String requestId, RequestDataType requestType, Instant dueDate, int progress, DataCompletionRequestStatus dataRequestStatus) {
        this.id = id;
        this.requestId = requestId;
        this.requestType = requestType;
        this.dueDate = dueDate;
        this.progress = progress;
        this.dataRequestStatus = dataRequestStatus;
    }

    public DataRequestItem(DataRequest dataRequest) {
        this.id = dataRequest.getId();
        this.requestId = dataRequest.getRequestId();
        this.requestType = dataRequest.getRequestDataType();
        this.createdById = dataRequest.getCreatedById();
        this.createdBy = dataRequest.getCreatedByUser();
        this.dueDate = dataRequest.getDueDate();
    }
}
