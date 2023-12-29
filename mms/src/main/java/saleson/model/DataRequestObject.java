package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.ObjectType;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DataRequestObject extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "DATA_REQUEST_ID", insertable = false, updatable = false)
    private Long dataRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATA_REQUEST_ID")
    private DataRequest dataRequest;

    private Long objectId;

    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

    public DataRequestObject(DataRequest dataRequest, Long objectId, ObjectType objectType) {
        this.dataRequestId = dataRequest.getId();
        this.dataRequest = dataRequest;
        this.objectId = objectId;
        this.objectType = objectType;
    }
}
