package saleson.common.enumeration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum OperatingStatus implements CodeMapperType {
	WORKING("Active"),
	IDLE("Idle"),
	NOT_WORKING("Inactive"),
	DISCONNECTED("Disconnected");

	private String title;
	private int checkStartDay;
	private int checkEndDay;

	OperatingStatus(String title) {
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
	private static final Map<String,OperatingStatus> ENUM_MAP;
	static {
		Map<String,OperatingStatus> map = new ConcurrentHashMap<String, OperatingStatus>();
		for (OperatingStatus instance : OperatingStatus.values()) {
			map.put(instance.getTitle().toLowerCase(),instance);
		}
		ENUM_MAP = Collections.unmodifiableMap(map);
	}

	public static OperatingStatus get(String title) {
		return ENUM_MAP.get(title.toLowerCase());
	}
}
