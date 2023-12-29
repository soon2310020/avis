package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;


@Getter
@Setter
public class MapChartData {
	private String locationName;			// Location Name
	private String address;			// Address
	private OperatingStatus operatingStatus;			// 국가명
	private Long value;				// Tool Count
	private String partName;
	private OperatingStatus counterOperatingStatus;
	private EquipmentStatus counterEquipmentStatus;

	@QueryProjection
	public MapChartData(String locationName, String address, OperatingStatus operatingStatus, Long value){
		this.locationName = locationName;
		this.address = address;
		this.operatingStatus = operatingStatus;
		this.value = value;
	}

	@QueryProjection
	public MapChartData(String locationName, String address, OperatingStatus operatingStatus, Long value, String partName){
		this.locationName = locationName;
		this.address = address;
		this.operatingStatus = operatingStatus;
		this.value = value;
		this.partName = partName;
	}

	@QueryProjection
	public MapChartData(String locationName, String address, OperatingStatus operatingStatus
						,EquipmentStatus counterEquipmentStatus, OperatingStatus counterOperatingStatus, Long value){
		this.locationName = locationName;
		this.address = address;
		this.operatingStatus = operatingStatus;
		this.counterEquipmentStatus = counterEquipmentStatus;
		this.counterOperatingStatus = counterOperatingStatus;
		this.value = value;
	}
}
