package saleson.api.mold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;

@Data @NoArgsConstructor @AllArgsConstructor
@Builder
public class MoldParam {

	private String query;
	private String status;
	private String extraStatus;
	private String sort;
	private Integer page;
	private Integer size;
	private OperatingStatus operatingStatus;
	private EquipmentStatus equipmentStatus;
	//private String partCode;
}
