package saleson.common.enumeration;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum EquipmentStatus implements CodeMapperType {
	@Deprecated
	DISCARDED("Discarded"), //
	DISPOSED("Disposed"), //
	INSTALLED("Installed"), //
	FAILURE("Failure"), //
	AVAILABLE("Available"), //
	DETACHED("Detached");

	private String title;

	EquipmentStatus(String title) {
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

	private static final Map<String, EquipmentStatus> ENUM_MAP;
	static {
		Map<String, EquipmentStatus> map = new ConcurrentHashMap<String, EquipmentStatus>();
		for (EquipmentStatus instance : EquipmentStatus.values()) {
			map.put(instance.getTitle().toLowerCase(), instance);
		}
		ENUM_MAP = Collections.unmodifiableMap(map);
	}

	public static EquipmentStatus get(String title) {
		return ENUM_MAP.get(title.toLowerCase());
	}
}
