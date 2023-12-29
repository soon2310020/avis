package saleson.model;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.model.support.UserDateAudit;

@Deprecated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DashboardChartParamSetting extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;

	private Long dashboardChartDisplaySettingId;

	private String paramName;

	private String paramValue;

	public DashboardChartParamSetting(Map.Entry<String, String> dashboardSettingDataMap, Long dashboardChartDisplaySettingId) {
		this.dashboardChartDisplaySettingId = dashboardChartDisplaySettingId;
		this.paramName = dashboardSettingDataMap.getKey();
		this.paramValue = dashboardSettingDataMap.getValue();
	}
}
