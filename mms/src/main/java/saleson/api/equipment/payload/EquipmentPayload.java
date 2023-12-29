package saleson.api.equipment.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.Location;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentPayload extends SearchParam {
	private String equipmentCode;
	private List<String> equipmentCodes;
	private List<EquipmentStatus> equipmentStatuses;
	private List<OperatingStatus> operatingStatuses;
	private OperatingStatus operatingStatus;
	private EquipmentStatus equipmentStatus;
	private String purchasedAt;
	private String installedAt;
	private String installedBy;
	private String memo;

	// Kepha ADD #913 [ add new parameter to allow the compatibility of legacy(operatingStatus, equipmentStatus)
	private String opStatus;
	private String equipStatus;
	//]

	private Long companyId;
	private Long locationId;

	private Location location;
}
