package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PresetStatus;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class Preset {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String ci; // 카운터 ID
	private String preset; // PRESET
	private Integer shotCount; // #25 PRESET 신청 시점의 counter > shotCount 정보를 저장. (Misconfigure alert 시 이후 증가분 만큼을 추가하여 제안)
	private String comment; // PRESET 신청 시 comment
	private String applyDesc; // 오류내용

	@Enumerated(EnumType.STRING)
	private PresetStatus presetStatus; // 적용상태

	private Long triggeredBy;

	// R2 (TDATA) - 2019.11.27.
	private String presetYn = "Y";

	private Integer alarmShot; // 알람 설정값

	private String alarmYn = "N"; // 알람 설정

	private String alarmRepeatYn = "N"; // 카운터 SHOT이 알람 설정 값 만큼 증가시 통신 (Y:연속, N:1회)

	private Integer shockValue; // 충격 임계값 (0~255)

	private String shockYn = "N";

	private Integer shotMissing;//mold > shotMissing

	private Integer forecastedMaxShots;//mold > new value for designedShot
	private Integer missingDays;//for preset

//	@Column(length = 1)
//	@Convert(converter = BooleanYnConverter.class)
//	private Boolean applied;

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

	public Preset(String ci) {
		this.ci = ci;
	}

	public Preset(String ci, String preset) {
		this.ci = ci;
		this.preset = preset;
	}

	private static final String COMMA = ",";

	// 터미널에 보내줄 데이터
	public String responseToTerminal(Transfer transfer) {
		StringBuilder sb = new StringBuilder();

		sb.append(getPreset());

		if (transfer.isVersionR2()) {
			sb.append(COMMA);
			sb.append(getPresetYn());

			sb.append(COMMA);
			sb.append(nullToZero(getAlarmShot()));

			sb.append(COMMA);
			sb.append(getAlarmRepeatYn());

			sb.append(COMMA);
			sb.append(getAlarmYn());

			sb.append(COMMA);
			sb.append(nullToZero(getShockValue()));

			sb.append(COMMA);
			sb.append(getShockYn());
		}

		return sb.toString();
	}

	private Integer nullToZero(Integer value) {
		return value == null ? 0 : value;
	}

	public static String failureResponse(Transfer transfer) {
		StringBuilder sb = new StringBuilder();

		sb.append("AUTH_FAIL");

		if (transfer.isVersionR2()) {
			sb.append(",N,0,N,N,0,N");
		}
		return sb.toString();
	}
}
