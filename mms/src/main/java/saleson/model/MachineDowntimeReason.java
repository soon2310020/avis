package saleson.model;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.emoldino.api.common.resource.base.code.repository.codedata.CodeData;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.api.machineDowntimeAlert.data.MachineDowntimeReasonItem;
import saleson.model.support.UserDateAudit;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MachineDowntimeReason extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;

	private Long machineDowntimeAlertId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "machineDowntimeAlertId", insertable = false, updatable = false)
	private MachineDowntimeAlert machineDowntimeAlert;

	private Long codeDataId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "codeDataId", insertable = false, updatable = false)
	private CodeData codeData;
	private Instant startTime;
	private Instant endTime;

	private String note;

	public MachineDowntimeReason(Long machineDowntimeAlertId, MachineDowntimeReasonItem item) {
		this.machineDowntimeAlertId = machineDowntimeAlertId;
		this.codeDataId = item.getCodeDataId();
		this.startTime = item.getStartTime();
		this.endTime = item.getEndTime();
		this.note = item.getNote();
	}
}
