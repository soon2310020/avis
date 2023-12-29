package saleson.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.model.support.UserDateAudit;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DataCompletionRate extends UserDateAudit {

    @Id
    @GeneratedValue
    private Long id;

    private Long companyId;
    private String companyCode;
    private String companyName;

    private Double companyRate;
    private Double locationRate;
    private Double categoryRate;
    private Double partRate;
    private Double moldRate;
    private Double machineRate;
}
