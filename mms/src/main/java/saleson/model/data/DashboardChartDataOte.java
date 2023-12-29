package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.mapper.CodeMapperType;
import saleson.model.Company;

import java.util.List;

@Data
@Builder
 @NoArgsConstructor
public class DashboardChartDataOte {
	private String title;
	private Integer data;
	private Integer total;
	private List<ChartDataOte> chartDataOteList;

	public DashboardChartDataOte(String title, Integer data, Integer total, List<ChartDataOte> chartDataOteList) {
		this.title = title;
		this.data = data;
		this.total = total;
		this.chartDataOteList = chartDataOteList;
	}

}
