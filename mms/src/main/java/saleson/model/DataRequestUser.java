package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DataRequestUser extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "DATA_REQUEST_ID", insertable = false, updatable = false)
    private Long dataRequestId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATA_REQUEST_ID")
    private DataRequest dataRequest;

    @Column(name = "USER_ID", insertable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    public DataRequestUser( DataRequest dataRequest, Long userId) {
        this.dataRequestId = dataRequest.getId();
        this.dataRequest = dataRequest;
        this.userId = userId;
        User userAssign = new User();
        userAssign.setId(userId);
        this.user = userAssign;
    }
}
