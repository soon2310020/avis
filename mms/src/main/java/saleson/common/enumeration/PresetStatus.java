package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum PresetStatus implements CodeMapperType {
	READY("적용대기"), // Counter와 Mold에 Reset된 ShotCount가 적용 된 상태
	CANCELED("취소됨"), // READY 상태인데 추가로 READY 데이터가 등록되는 경우 이전 READY 상태를 CANCELED 로 변경함.
	APPLIED("적용완료");

	private String title;

	PresetStatus(String title) {
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
