package saleson.model;

import com.emoldino.api.common.resource.base.version.repository.appversionitem.AppVersionItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.DataCompletionRequestStatus;
import saleson.common.enumeration.RequestDataType;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DataRequest extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String requestId;

    @Enumerated(EnumType.STRING)
    private RequestDataType requestDataType;

    private Instant dueDate;

    private Instant requestDate;

    @Lob
    private String detail;

    @Lob
    private String cancelReason;

    @Column(name = "CANCELED_BY_ID", insertable = false, updatable = false)
    private Long canceledById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CANCELED_BY_ID")
    private User cancelledBy;

    @Lob
    private String declineReason;

    @Column(name = "DECLINED_BY_ID", insertable = false, updatable = false)
    private Long declinedById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DECLINED_BY_ID")
    private User declinedBy;

    @Enumerated(EnumType.STRING)
    private DataCompletionRequestStatus dataRequestStatus;

    @Column(name = "CREATED_BY_ID", insertable = false, updatable = false)
    private Long createdById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATED_BY_ID")
    private User createdByUser;

    private Integer numberCompanyRequest;
    private Integer numberLocationRequest;
    private Integer numberPartRequest;
    private Integer numberMoldRequest;
    private Integer numberMachineRequest;


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "DATA_REQUEST_ID")
    private List<DataRequestUser> dataRequestUserList = new ArrayList<>();

    public DataCompletionRequestStatus getStatus() {
        if (dueDate.isBefore(Instant.now()) &&  dataRequestStatus == DataCompletionRequestStatus.IN_PROGRESS) {
            return DataCompletionRequestStatus.OVERDUE;
        }
        return dataRequestStatus;
    }
}
