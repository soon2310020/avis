package saleson.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import saleson.common.enumeration.AlertType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

import javax.persistence.*;

@Getter @Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
        @Index(name = "IDX_LOG_USER_ALERT_01", columnList = "userId,alertType"),
})
public class LogUserAlert extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    private Long alertId;

    @Column(length = 1, nullable = false)
    @Convert(converter = BooleanYnConverter.class)
    private boolean email;

    public LogUserAlert(){}

    public LogUserAlert(Long userId, AlertType alertType, Long alertId){
        this.userId = userId;
        this.alertId = alertId;
        this.alertType = alertType;
    }

    public LogUserAlert(Long userId, AlertType alertType, Long alertId, Boolean email){
        this.userId = userId;
        this.alertId = alertId;
        this.alertType = alertType;
        this.email = email;
    }
}
