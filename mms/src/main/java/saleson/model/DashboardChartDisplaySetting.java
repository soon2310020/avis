package saleson.model;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.DashboardChartType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

@Deprecated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DashboardChartDisplaySetting extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;

	private DashboardChartType chartType;

	private Long userId;

	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

}
