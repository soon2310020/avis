package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum RefurbishmentStatus implements CodeMapperType {
	END_OF_LIFECYCLE("End of lifecycle"),
	MEDIUM_HIGH("Medium + High"),
	REQUESTED("Requested"),
	DISCARDED("Discarded"),
	COMPLETED("Completed"),
	APPROVED("Approved"),
	DISAPPROVED("Disapproved"),
	REPAIRED("Repaired");

	private String title;

	RefurbishmentStatus(String title) {
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
