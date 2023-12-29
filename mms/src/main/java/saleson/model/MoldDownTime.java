package saleson.model;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import saleson.common.enumeration.CorrectiveStatus;
import saleson.common.enumeration.CurrencyType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DateUtils;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity
public class MoldDownTime extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "moldId")
	private Mold mold;

	@Column(insertable = false, updatable = false)
	private Long moldId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_CORRECTIVE_ID")
	private MoldCorrective moldCorrective;

	private Long downTime;

	private String year;
	private String month;
	private String day;
	private String week;

	public MoldDownTime(MoldCorrective moldCorrective, Long downTime, Instant serverDate) {
		this.mold = moldCorrective.getMold();
		this.moldCorrective = moldCorrective;
		this.downTime = downTime;
		String dateTime = DateUtils.getDate(serverDate, DateUtils.DEFAULT_DATE_FORMAT);
		this.year = DateUtils.getYear(dateTime);
		this.month = DateUtils.getYearMonth(dateTime);
		this.week = DateUtils.getYearWeek(dateTime);
		this.day = DateUtils.getDay(dateTime);
	}
}
