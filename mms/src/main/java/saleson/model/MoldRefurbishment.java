package saleson.model;

import java.time.Instant;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.EndOfLifeCycleStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.RefurbishmentStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.EndLifeCycleBase;

@Getter
@Setter
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class MoldRefurbishment extends EndLifeCycleBase {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_END_LIFE_CYCLE_ID")
	private MoldEndLifeCycle moldEndLifeCycle;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LOCATION_ID")
	private Location location;

	@Enumerated(EnumType.STRING)
	private RefurbishmentStatus refurbishmentStatus;
	private Instant expectedEndTime;
	private Instant startTime;
	private Instant endTime;
	private Instant failureTime;
	private Instant repairTime;

	@Enumerated(EnumType.STRING)
	private CurrencyType currencyType;
	private Integer cost;
	private Integer numberOfWorkingCavity;
	private Integer estimateExtendedLife;

	@Lob
	private String memo;

	@Lob
	private String failureReason;

	private String disapprovedBy;
	private Instant disapprovedAt;
	@Lob
	private String disapprovedMessage;

//	@Convert(converter = BooleanYnConverter.class)
//	private boolean deleted;

	@Deprecated
	@Convert(converter = BooleanYnConverter.class)
	private Boolean latest;

	//if history recode is true
	@Convert(converter = BooleanYnConverter.class)
	private Boolean lastChecked;

//	@Transient
//	private String failureDateTime;

//	@Transient
//	private String repairDateTime;

	@Transient
	private MultipartFile[] files;

//	public void convertStringToInstant() {
//		if (failureDateTime != null && !failureDateTime.isEmpty()) {
//			failureTime = DateUtils2.toInstant(failureDateTime, DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(getMold()));
//		}
//
//		if (repairDateTime != null && !repairDateTime.isEmpty()) {
//			failureTime = DateUtils2.toInstant(repairDateTime, DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(getMold()));
//		}
//	}

	public String getFailureDate() {
		return DateUtils2.format(getFailureTime(), DatePattern.yyyy_MM_dd, LocationUtils.getZoneIdByMold(getMold()));
	}

	public String getFailureDateTime() {
		return DateUtils2.format(getFailureTime(), DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(getMold()));
	}

	public String getRepairDate() {
		return DateUtils2.format(getRepairTime(), DatePattern.yyyy_MM_dd, LocationUtils.getZoneIdByMold(getMold()));
	}

	public String getRepairDateTime() {
		return DateUtils2.format(getRepairTime(), DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByMold(getMold()));
	}

	public MoldRefurbishment(Mold mold, Instant issueDate, PriorityType priority, EndOfLifeCycleStatus status) {
		super(mold, issueDate, priority, status);
	}
}
