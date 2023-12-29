package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Getter
@Setter
@ToString
@Entity
/*@Table(indexes = {
		@Index(name="IDX_CDATA_01", columnList = "YEAR"),
		@Index(name="IDX_CDATA_02", columnList = "WEEK"),
		@Index(name="IDX_CDATA_05", columnList = "YEAR, WEEK"),
		@Index(name="IDX_CDATA_03", columnList = "MOLD_CODE"),
		@Index(name="IDX_CDATA_04", columnList = "PART_CODE")
})*/
@Table(indexes = { //
		@Index(name = "IDX_CDATA_01", columnList = "year"), //
		@Index(name = "IDX_CDATA_02", columnList = "year,day"), //
		@Index(name = "IDX_CDATA_03", columnList = "year,week"), //
		@Index(name = "IDX_CDATA_04", columnList = "year,month"), //
		@Index(name = "IDX_CDATA_05", columnList = "moldId"), //
		@Index(name = "IDX_CDATA_06", columnList = "moldCode"), //
		@Index(name = "IDX_CDATA_07", columnList = "ci, ti"), //
		@Index(name = "IDX_CDATA_08", columnList = "day"), //
		@Index(name = "IDX_CDATA_09", columnList = "moldId,year,day,ct")//
})
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class Statistics implements Cloneable {
	@Id
	@GeneratedValue
	private Long id;

	private Long cdataId;

	private Long moldId;
	private String moldCode;

	private String ti;
	private String ci;
	private Integer fsc; // 시작 shot(이전 데이터로 추출)
	private Integer sc; // last shot (Cdata -> sc)
	private String fst; // first shot time (lst과 ct 값으로 추정함.)
	private String lst;
	private Double ct;

	private Integer shotCount; // 실제 가동 시간 동안 찍은 shot count.
	private Long uptimeSeconds; // 실제 가동 시간.

	// v1.2 CDATA
	private Double tp; // 온도 (0 : 정보없음)
	private String ra; // 금형 부착 여부 (Y:부착, N:제거, X:정보없음)
	private String pw; // 외부 전원 여부 (Y:부착, N:제거, X:정보없음)

	private String year;
	private String month;
	private String day;
	private String week;

	// R2 - 2020.04.24
	private String hour;
	private String tff; // 온도1 시간
	private Integer tlo; // 최저온도
	private Integer thi; // 최고온도
	private Integer tav; // 평균온도 (temp의 온도 평균)
	private Double llct; // LLCT
	private Double ulct; // ULCT
	private String rt; // R2버전 (통계기준시간)

	// First shot flag to detect if it's a very first data of the counter
	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean firstData;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;

	private Double ctVal;
	private Integer shotCountVal; // 실제 가동 시간 동안 찍은 shot count.
	private Double llctVal; // LLCT
	private Double ulctVal; // ULCT

	public Statistics() {

	}

	public Statistics(Cdata cdata) {
		this.moldId = cdata.getMoldId();
		this.moldCode = cdata.getMoldCode();
		this.cdataId = cdata.getId();
		this.ti = cdata.getTi();
		this.ci = cdata.getCi();
		this.sc = cdata.getSc();
		this.lst = cdata.getLst();
		this.ct = cdata.getCt();
		this.year = cdata.getYear();
		this.month = cdata.getMonth();
		this.week = cdata.getWeek();
		this.day = cdata.getDay();

		// v1.2
		this.tp = cdata.getTp();
		this.ra = cdata.getRa();
		this.pw = cdata.getPw();

		// R2-2020.04.24
		this.tff = cdata.getTff(); // 온도1 시간
		this.tlo = cdata.getTlo(); // 최저온도
		this.thi = cdata.getThi(); // 최고온도
		this.tav = cdata.getTav(); // 평균온도 (temp의 온도 평균)
		this.llct = cdata.getLlct(); // LLCT
		this.ulct = cdata.getUlct(); // ULCT
		this.rt = cdata.getRt(); // R2버전 (통계기준시간)

		this.createdAt = cdata.getCreatedAt();
	}

	//

	public String getFirstShotDateTime() {
		return getDateTime(fst);
	}

	public String getLastShotDateTime() {
		return getDateTime(lst);
	}

	public double getUptimeMinutes() {
		return Math.round((double) uptimeSeconds / 60 * 10) / 10.0;
	}

	public double getCycleTimeSeconds() {
		return (double) Math.round(ct * 0.1 * 10) / 10;
	}

	private String getDateTime(String dateTime) {
		if (dateTime != null && dateTime.length() == 14) {
			return new StringBuilder().append(dateTime.substring(0, 4)).append("-").append(dateTime.substring(4, 6)).append("-").append(dateTime.substring(6, 8)).append(" ")
					.append(dateTime.substring(8, 10)).append(":").append(dateTime.substring(10, 12)).append(":").append(dateTime.substring(12, 14)).toString();
		}
		return "";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
