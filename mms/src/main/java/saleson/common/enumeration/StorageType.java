package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum StorageType implements CodeMapperType {

	MOLD("Mold install photo"),
	MOLD_MAINTENANCE_DOCUMENT("Mold Maintenance Document "),
	MOLD_PO_DOCUMENT("Mold PO Document "),
	MOLD_PICTURE("Tooling Picture"),
	MOLD_INSTRUCTION_VIDEO("Instruction Video"),
	MOLD_CONDITION("Tooling Condition"),
	MOLD_MAINTENANCE("Mold Maintenance"),
	MOLD_COUNTER("Mold and Counter install photo"),
	MOLD_CORRECTIVE("Mold corrective photo"),
	TERMINAL("Terminal installation photo"),
	CORRESPONDENCE("Correspondence Document"),
	MOLD_REFURBISHMENT("Refurbishment Document"),
	PART_PICTURE("Part Picture"),
	MACHINE_PICTURE("Machine Picture"),
	WORK_ORDER_FILE("Work Order File"),
	WORK_ORDER_POW_FILE("Work Order Proof Of Work File"),
	WORK_ORDER_COST_FILE("Work Order Cost File"),
	WORK_ORDER_CHECKLIST_FILE("Work Order Checklist File"),
	PROJECT_IMAGE("Project Image"),
	IMPORT_FILE("Import File"),
	COMPANY_PICTURE("Company Picture"),
	LOCATION_PICTURE("Location Picture"),
	DATA_REQUEST_FILE("Data request file")
	;

	private String title;

	StorageType(String title) {
		this.title = title;
	}


	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return "";
	}
	@Override
	public Boolean isEnabled() {
		return true;
	}
}
