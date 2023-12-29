package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "V_COMPANY_TARGET_UPTIME_HOUR", catalog = "")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VCompanyTargetUptimeHour {
    @Id
    private Long id;
    private Long hourPerDay;

}
