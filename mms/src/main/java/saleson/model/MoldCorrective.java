package saleson.model;


import lombok.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import saleson.common.enumeration.CorrectiveStatus;
import saleson.common.enumeration.CurrencyType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DateUtils;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
/*@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")*/
public class MoldCorrective extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;


	/*@Column(name = "MOLD_ID", insertable = false, updatable = false)
	private Long moldId;*/

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_ID")
	private Mold mold;

	/*@Column(name = "LOCATION_ID", insertable = false, updatable = false)
	private Long locationId;*/

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LOCATION_ID")
	private Location location;


	private Instant failureTime;
	private Instant repairTime;


	private Integer timeToRepair;
	private Integer cost;
	private Integer numberOfBackups;

	@Enumerated(EnumType.STRING)
	private CurrencyType currencyType;

	@Enumerated(EnumType.STRING)
	private CorrectiveStatus correctiveStatus;

	@Lob
	private String memo;

	@Lob
	private String failureReason;

	@Lob
	private String disapprovedMessage;

	private String disapprovedBy;

	private Instant disapprovedAt;

	private Instant expectedEndTime;

	private Instant startTime;

	private Instant endTime;

//	private String day;
//	private String week;
//	private String month;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean latest;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean lastChecked;

	@Transient
	private Long moldId;		// mold.id

	@Transient
	private String failureDateTime;

	@Transient
	private String repairDateTime;

	@Transient
	private MultipartFile[] files;

	public void convertStringToInstant() {
		if (failureDateTime != null && !failureDateTime.isEmpty()) {
			//failureTime = DateUtils.getInstant(failureDateTime, "MM/dd/yyyy h:mm a", Locale.US);
			failureTime = DateUtils.getInstant(failureDateTime, "yyyy-MM-dd HH:mm:ss");
		}

		if (repairDateTime != null && !repairDateTime.isEmpty()) {
			//repairTime = DateUtils.getInstant(repairDateTime, "MM/dd/yyyy h:mm a", Locale.US);
			repairTime = DateUtils.getInstant(repairDateTime, "yyyy-MM-dd HH:mm:ss");
		}
	}

	public String getFailureDate() {
		return DateUtils.getDate(getFailureTime());
	}
	public String getFailureDateTime() {
		return DateUtils.getDateTime(getFailureTime());
	}
	public String getRepairDate() {
		return DateUtils.getDate(getRepairTime());
	}
	public String getRepairDateTime() {
		return DateUtils.getDateTime(getRepairTime());
	}

	public Machine getMachine() {
		return mold != null ? mold.getMachine() : null;
	}
}
