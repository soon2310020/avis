package saleson.model.data;

import lombok.Getter;
import lombok.Setter;
import saleson.common.util.DateUtils;

import java.time.Instant;

@Getter
@Setter
public class CdataCounter {
	private String ci;
	private String equipmentCode; // COUNTER > EQUIPMENT_CODE
	private Instant lastTime;

	public CdataCounter(String ci, String equipmentCode, Instant lastTime) {
		this.ci = ci;
		this.equipmentCode = equipmentCode;
		this.lastTime = lastTime;
	}

	public String getMessage() {
		if (equipmentCode == null) {
			return "The counter is not registered.";
		}
		return "The counter is not connected to tooling.";
	}

	public String getLastDateTime() {
		return DateUtils.getDateTime(getLastTime());
	}
}
