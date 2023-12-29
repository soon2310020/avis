package saleson.api.dataRequest.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import saleson.common.enumeration.DataCompletionRequestStatus;
import saleson.common.enumeration.RequestDataType;
import saleson.model.DataRequest;
import saleson.model.User;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataRequestDetail {
    private Long id;
    private String requestId;
    private RequestDataType requestType;
    private Integer companyNumber;
    private Integer locationNumber;
    private Integer partNumber;
    private Integer moldNumber;
    private Integer machineNumber;

    private Integer companyDoneNumber;
    private Integer locationDoneNumber;
    private Integer partDoneNumber;
    private Integer moldDoneNumber;
    private Integer machineDoneNumber;
//    private List<Long> companyIdList;
//    private List<Long> locationIdList;
//    private List<Long> partIdList;
//    private List<Long> moldIdList;
//    private List<Long> machineIdList;

    private Boolean isSelectedCompany;
    private Boolean isSelectedLocation;
    private Boolean isSelectedPart;
    private Boolean isSelectedMold;
    private Boolean isSelectedMachine;

    private List<User> assignedToList;

    private List<Long> assignedToIdList;
    private Instant dueDate;
    private Instant requestDate;
    private String detail;
    private String cancelReason;

    private String declineReason;
    private DataCompletionRequestStatus dataRequestStatus;

    private MultipartFile[] files;

    private User createdBy;

    private User cancelledBy;

    private User declinedBy;

    private boolean isReOpen = false;

    public void bindingData(DataRequest dataRequest) {
        dataRequest.setRequestId(requestId);
        dataRequest.setRequestDataType(requestType);
        dataRequest.setDetail(detail);
        dataRequest.setRequestDate(requestDate);
        dataRequest.setDueDate(dueDate);
        if (requestType == RequestDataType.DATA_REGISTRATION) {
            dataRequest.setNumberCompanyRequest(companyNumber);
            dataRequest.setNumberLocationRequest(locationNumber);
            dataRequest.setNumberPartRequest(partNumber);
            dataRequest.setNumberMoldRequest(moldNumber);
            dataRequest.setNumberMachineRequest(machineNumber);
        }
    }

    public DataRequestDetail(DataRequest dataRequest) {
        this.id = dataRequest.getId();
        this.requestId = dataRequest.getRequestId();
        this.requestType = dataRequest.getRequestDataType();
        this.companyNumber = dataRequest.getNumberCompanyRequest();
        this.locationNumber = dataRequest.getNumberLocationRequest();
        this.partNumber = dataRequest.getNumberPartRequest();
        this.moldNumber = dataRequest.getNumberMoldRequest();
        this.machineNumber = dataRequest.getNumberMachineRequest();
        this.dueDate = dataRequest.getDueDate();
        this.requestDate = dataRequest.getRequestDate();
        this.detail = dataRequest.getDetail();
        this.cancelReason = dataRequest.getCancelReason();
        this.declineReason = dataRequest.getDeclineReason();
        this.dataRequestStatus = dataRequest.getDataRequestStatus();
        this.createdBy = dataRequest.getCreatedByUser();
        this.declinedBy = dataRequest.getDeclinedBy();
        this.cancelledBy = dataRequest.getCancelledBy();
    }
}
