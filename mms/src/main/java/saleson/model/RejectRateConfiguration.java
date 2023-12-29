package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.common.enumeration.Frequent;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Builder
public class RejectRateConfiguration extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Frequent frequent;
}
