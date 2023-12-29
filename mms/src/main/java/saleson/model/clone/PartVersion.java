package saleson.model.clone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartVersion extends
        VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    @Column
    private String name;
    private String partCode;
    @Lob
    private String resinCode;
    private String resinGrade;
    private String designRevision;
    private String size;
    private String weight;
    private Long categoryId;
    @Enumerated(EnumType.STRING)
    private SizeUnit sizeUnit;
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;
    private boolean enabled;
}
