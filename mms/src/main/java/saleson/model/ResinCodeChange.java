package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.model.support.DateAudit;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ResinCodeChange extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    private Long partId;
    private String oldResinCode;
    private String newResinCode;
    private Instant time;
    private String month;
    private String week;
    private String day;
    private String hour;
    private Double currentWact;
}
