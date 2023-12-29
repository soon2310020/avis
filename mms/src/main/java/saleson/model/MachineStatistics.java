package saleson.model;

import java.time.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;
import saleson.model.support.*;
import saleson.service.util.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MachineStatistics extends UserDateAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MACHINE_ID")
	private Machine machine;

	private Instant date;
	private String day;

	private Integer dailyWorkingHour;
	private Integer dailyWorkingHourMinute;
	private String dailyWorkingHourNote;
	private Instant dailyWorkingHourEditedAt;

	private Integer plannedDowntime; // hour
	private Integer plannedDowntimeMinute; // minute
	private String plannedDowntimeType;
	private String plannedDowntimeNote;

	private Integer unplannedDowntime; // hour
	private Integer unplannedDowntimeMinute; // minute
	private String unplannedDowntimeType;
	private String unplannedDowntimeNote;

	private Double oee;
	private Double fa;
	private Double fp;
	private Double fq;

	public Integer getPlannedDowntime() {
		return this.plannedDowntime != null ? this.plannedDowntime : 0;
	}

	public Integer getPlannedDowntimeMinute() {
		return this.plannedDowntimeMinute != null ? this.plannedDowntimeMinute : 0;
	}

	public Integer getUnplannedDowntime() {
		return this.unplannedDowntime != null ? this.unplannedDowntime : 0;
	}

	public Integer getUnplannedDowntimeMinute() {
		return this.unplannedDowntimeMinute != null ? this.unplannedDowntimeMinute : 0;
	}

	public Integer getDailyWorkingHour() {
		return this.dailyWorkingHour != null ? this.dailyWorkingHour : 24;
	}

	public Integer getDailyWorkingHourMinute() {
		return this.dailyWorkingHourMinute != null ? this.dailyWorkingHourMinute : 0;
	}

	public Double roundedDailyWorkHour() {
		return DateTimeUtils.roundedHourFromHourAndMinute(getDailyWorkingHour(), getDailyWorkingHourMinute());
	}

	public Double roundedPlannedDowntime() {
		return DateTimeUtils.roundedHourFromHourAndMinute(getPlannedDowntime(), getPlannedDowntimeMinute());
	}

	public Double roundedUnplannedDowntime() {
		return DateTimeUtils.roundedHourFromHourAndMinute(getUnplannedDowntime(), getUnplannedDowntimeMinute());
	}
}
