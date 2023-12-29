package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.mapper.CodeMapperType;
import saleson.model.Company;

import java.util.List;

@Builder
@AllArgsConstructor @NoArgsConstructor
public class DashboardChartData implements Comparable<DashboardChartData> {

	private Long id;
	private String title;
	private Long data;
	private Double decimalData;

	@QueryProjection
	public DashboardChartData(String title, Long data){
		this.title = title;
		this.data = data;
	}

	@QueryProjection
	public DashboardChartData(CodeMapperType operatingStatus, Long data){
		if(operatingStatus != null) this.title = operatingStatus.getTitle();
		else this.title = "Never Been In Operation";
		this.data = data;
	}

	@QueryProjection
	public DashboardChartData(String title, Double decimalData){
		this.title = title;
		this.decimalData = decimalData;
	}

//    @QueryProjection
//    public DashboardChartData(EquipmentStatus equipmentStatus, Long data){
//        if(equipmentStatus != null) this.title = equipmentStatus.getTitle();
//        else this.title = "No operating status";
//        this.data = data;
//    }

//	@QueryProjection
//	public DashboardChartData(Long id, String title, Long data){
//	    this.id = id;
//	    this.title = title;
//		this.data = data;
//	}

	@QueryProjection
	public DashboardChartData(Company company, Long data) {
		if (company != null) {
			this.id = company.getId();
			this.title = company.getName() != null ? company.getName() : "";
		} else {
			this.title = "Not Assigned";
		}
		this.data = data;
	}

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getData() {
		return data;
	}

	public void setData(Long data) {
		this.data = data;
	}

	@Override
	public int compareTo(DashboardChartData o) {
		return Integer.compare(Integer.parseInt(title), Integer.parseInt(o.getTitle()));

	}
}
