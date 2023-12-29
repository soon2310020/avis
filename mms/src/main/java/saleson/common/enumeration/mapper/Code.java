package saleson.common.enumeration.mapper;

public class Code {
	public static final String MAINTENANCE_STATUS = "MaintenanceStatus";
	public static final String USER_TYPE = "UserType";			//  회원 구분
	public static final String ROLE_TYPE = "RoleType";			//  역할구분
	public static final String COMPANY_TYPE = "CompanyType";			//  회사구분
	public static final String EQUIPMENT_STATUS = "EquipmentStatus";			//  EQUIPMENT_STATUS
	public static final String NOTIFICATION_STATUS = "NotificationStatus";


	public static final String SIZE_UNIT = "SizeUnit";
	public static final String WEIGHT_UNIT = "WeightUnit";
	public static final String RUNNER_TYPE = "RunnerType";
	public static final String TOOLING_CONDITION = "ToolingCondition";
	public static final String Currency_Type = "CurrencyType";


	private String code;
	private String title;
	private String description;
	private Boolean enabled;

	public Code(CodeMapperType codeMapperType) {
		code = codeMapperType.getCode();
		title = codeMapperType.getTitle();
		description = codeMapperType.getDescription();
		enabled = codeMapperType.isEnabled();
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	@Override
	public String toString() {
		return "Code{" +
				"code='" + code + '\'' +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", enabled=" + enabled +
				'}';
	}
}
