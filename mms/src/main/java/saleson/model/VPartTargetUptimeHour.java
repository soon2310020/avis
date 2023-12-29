package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "V_PART_TARGET_UPTIME_HOUR", catalog = "")
@JsonIgnoreProperties(ignoreUnknown = true)
public class VPartTargetUptimeHour {
    @Id
    private Long id;
    private Long hourPerDay;

}
