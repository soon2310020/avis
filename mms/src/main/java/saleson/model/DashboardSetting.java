package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.DashboardSettingLevel;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DashboardSetting extends UserDateAudit
{
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private DashboardSettingLevel level;

    private Integer monthNumber;

    private Long userId;

    @Convert(converter = BooleanYnConverter.class)
    private boolean deleted;

    public DashboardSetting(DashboardSettingLevel level, Integer monthNumber, Long userId)
    {
        this.level = level;
        this.monthNumber = monthNumber;
        this.userId = userId;
        this.deleted = false;
    }
}
