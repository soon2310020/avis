package saleson.restdocs.dto;

import lombok.Data;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;

@Data
public class MoldDto {
	private Long id;
	private String equipmentCode;
	private EquipmentStatus equipmentStatus;

	private String name;

	private String runnerType;
	private String size;
	private String sizeUnit;

	private String counterCode;

	private String contractedCycleTimeSeconds;

	private String locationTitle;
	private String locationCode;
	private String locationCity;

	private Long lastShot;
	private String lastShotDateTime;

	private OperatingStatus operatingStatus;
	private String operatedDateTime;



	private String companyName;
	private String toolMakerCompanyName;


	private String createdDateTime;



}
