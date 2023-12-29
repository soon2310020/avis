package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.DateUtils;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class StatisticsPreset {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

//	private String ci;		// 카운터 ID
//	private String preset;		// PRESET
	private Integer shotCount; // #25 PRESET 신청 시점의 counter > shotCount 정보를 저장. (Misconfigure alert 시 이후 증가분 만큼을 추가하여 제안)
//	private String comment;		// PRESET 신청 시 comment
//	private String applyDesc; 	// 오류내용

	private Integer shotMissing;//mold > shotMissing
	private Integer presetId;

	private Long moldId;
	private String moldCode;

	private String year;
	private String month;
	private String week;
	private String day;
	private String hour;

	private Integer missingDays;//for preset

	@JsonIgnore
	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedBy
	private Long updatedBy;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;

	public StatisticsPreset(Integer presetId, Long moldId, String moldCode, Integer shotMissing, Integer missingDays, Integer shotCount) {
		this.presetId = presetId;
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.shotMissing = shotMissing;
		String today = DateUtils.getToday();
		this.year = DateUtils.getYear(today);
		this.month = DateUtils.getYearMonth(today);
		this.day = DateUtils.getDay(today);
		this.hour = DateUtils.getHour(today);
		this.week = DateUtils.getYearWeek(today);
		this.missingDays = missingDays;
		this.shotCount = shotCount;
	}
}
