package saleson.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DateUtils;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;

@Getter @Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(indexes = {
		@Index(name = "IDX_MOLD_MAINTENANCE_01", columnList = "latest"),
})
public class MoldMaintenance extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;


	@Column(name = "MOLD_ID", insertable = false, updatable = false)
	private Long moldId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_ID")
	private Mold mold;


	@Column(name = "WORK_ORDER_ID", insertable = false, updatable = false)
	private Long workOrderId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORK_ORDER_ID")
	private WorkOrder workOrder;

	@Enumerated(EnumType.STRING)
	private MaintenanceStatus maintenanceStatus;

	@Lob
	private String checklist;

	private Integer preventCycle;
	private Integer preventUpcoming;
	private Integer periodStart;
	private Integer periodEnd;
	private Integer shotCount;

	private Integer lastShotMade; // last shot made since the last period

	private Instant maintenancedAt;
	private String maintenanceBy;			// 정비 완료 이름.

	private Instant startTime;
	private Instant endTime;

	private Instant overdueTime;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean latest;
	private Integer dueDate;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean registered;

	private Integer accumulatedShot;

	public MoldMaintenance() {}

	public MoldMaintenance(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMoldId() {
		return moldId;
	}

	public void setMoldId(Long moldId) {
		this.moldId = moldId;
	}

	public Mold getMold() {
		return mold;
	}

	public void setMold(Mold mold) {
		this.mold = mold;
	}


	public MaintenanceStatus getMaintenanceStatus() {
		return maintenanceStatus;
	}

	public void setMaintenanceStatus(MaintenanceStatus maintenanceStatus) {
		this.maintenanceStatus = maintenanceStatus;
	}

	public String getChecklist() {
		return checklist;
	}

	public void setChecklist(String checklist) {
		this.checklist = checklist;
	}

	public Integer getPeriodStart() {
		return periodStart;
	}

	public void setPeriodStart(Integer periodStart) {
		this.periodStart = periodStart;
	}

	public Integer getPreventCycle() {
		return preventCycle;
	}

	public void setPreventCycle(Integer preventCycle) {
		this.preventCycle = preventCycle;
	}

	public Integer getPreventUpcoming() {
		return preventUpcoming;
	}

	public void setPreventUpcoming(Integer preventUpcoming) {
		this.preventUpcoming = preventUpcoming;
	}

	public Integer getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(Integer periodEnd) {
		this.periodEnd = periodEnd;
	}

	public Integer getShotCount() {
		return shotCount;
	}

	public void setShotCount(Integer shotCount) {
		this.shotCount = shotCount;
	}

	public Instant getMaintenancedAt() {
		return maintenancedAt;
	}

	public void setMaintenancedAt(Instant maintenancedAt) {
		this.maintenancedAt = maintenancedAt;
	}

	public String getMaintenanceBy() {
		return maintenanceBy;
	}

	public void setMaintenanceBy(String maintenanceBy) {
		this.maintenanceBy = maintenanceBy;
	}


	public String getMaintenancedDate() {
		return DateUtils.getDate(getMaintenancedAt());
	}
	public String getMaintenancedDateTime() {
		return DateUtils.getDateTime(getMaintenancedAt());
	}

	public Instant getStartTime(){ return startTime; }

	public void setStartTime(Long startTime){ this.startTime = Instant.ofEpochSecond(startTime); }

	public Instant getEndTime(){ return endTime; }

	public void setEndTime(Long endTime){ this.endTime = Instant.ofEpochSecond(endTime); }

	public Boolean getLatest() { return latest; }

	public void setLatest(Boolean latest) { this.latest = latest; }

	public Instant getOverdueTime(){ return overdueTime; }

	public void setOverdueTime(Long overdueTime){ this.overdueTime = Instant.ofEpochSecond(overdueTime); }

	public void setOverdueTime(Instant overdueTime){ this.overdueTime = overdueTime; }

	public Integer getDueDate() {
		return dueDate;
	}

	public void setDueDate(Integer dueDate) {
		this.dueDate = dueDate;
	}

	public Integer getPmCheckpointPrediction() {
		return dueDate;
	}

	public Integer getShotUntilNextPM() {
		Integer val = null;
		if (preventCycle != null && lastShotMade != null) {
			val = preventCycle - lastShotMade;
		}
		return val;
	}


}
