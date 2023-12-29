package saleson.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MachineDowntimeAlertUser extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;

    private Long machineDowntimeAlertId;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machineDowntimeId", insertable = false, updatable = false)
    private MachineDowntimeAlert machineDowntimeAlert;

    @Enumerated(EnumType.STRING)
    private MachineDowntimeAlertStatus notificationStatus;

    private Long userId;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}
