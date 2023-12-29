package saleson.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "idx_config_category_fieldName", columnNames={"configCategory", "fieldName"})
})
public class GeneralConfig extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConfigCategory configCategory;

    private String fieldName;

    @Column(length = 1, nullable = false)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean required;

    @Column(length = 1, nullable = false)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean defaultInput;

    private String defaultInputValue;

    @Column(length = 1, nullable = false)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean deletedField;

}
