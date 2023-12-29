package saleson.api.machine.payload;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.util.StringUtils;
import saleson.model.Machine;
import saleson.model.QMachine;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MachineStatisticsPayload extends SearchParam {
	private Long machineId;
	private Machine machine;

	private Instant date;
	private String day;

	private String startDateStr;
	private String endDateStr;

	private Integer dailyWorkingHour;
	private String dailyWorkingHourNote;

	private Integer plannedDowntimeFrom;
	private Integer plannedDowntimeTo;
	private Integer plannedDowntime;
	private String plannedDowntimeType;
	private String plannedDowntimeNote;

	private Integer unplannedDowntimeFrom;
	private Integer unplannedDowntimeTo;
	private Integer unplannedDowntime;
	private String unplannedDowntimeType;
	private String unplannedDowntimeNote;

	public Predicate getPredicate() {
		BooleanBuilder builder = new BooleanBuilder();

		QMachine machine = QMachine.machine;

		if (!StringUtils.isEmpty(getStatus()) && "disabled".equalsIgnoreCase(getStatus())) {
			builder.and(machine.enabled.isFalse());
		} else {
			builder.and(machine.enabled.isTrue());
		}

		String query = getQuery();
		if (!StringUtils.isEmpty(query)) {
			builder.and(machine.machineCode.contains(query).or(machine.machineMaker.contains(query)).or(machine.machineType.contains(query)));
		}
		checkDeleted(machine, builder);
		return builder;
	}

	private void checkDeleted(QMachine machine, BooleanBuilder builder) {
		if (getDeleted() == null) {
			builder.and(machine.deleted.isNull().or(machine.deleted.isFalse()));
		} else if (getDeleted()) {
			builder.and(machine.deleted.isTrue());
		}
	}
}
