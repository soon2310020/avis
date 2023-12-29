package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;

import com.emoldino.framework.util.BeanUtils;

import lombok.Data;
import saleson.service.transfer.VersionChecker;

@Entity
/*@Table(indexes = {
		@Index(name="IDX_CDATA_01", columnList = "YEAR"),
		@Index(name="IDX_CDATA_02", columnList = "WEEK"),
		@Index(name="IDX_CDATA_05", columnList = "YEAR, WEEK"),
		@Index(name="IDX_CDATA_03", columnList = "MOLD_CODE"),
		@Index(name="IDX_CDATA_04", columnList = "PART_CODE")
})*/
@Data
public class Cdata {
	@Id
	private Long id;
	private Long moldId;
	private String moldCode;

	// Common(2rd, 3rd) Use 
	private String ti;
	private String ci;
	private String ver;
	private Integer sc;
	private String lst;
	private String rt;
	private String bs;
	private Integer sn;

	private Double ct;
	private String ctt; // CT Table
	private Double llct; // LLCT
	private Double ulct; // ULCT	

	private Double ctVal;
	private String cttVal; // CT Table
	private Double llctVal; // LLCT
	private Double ulctVal; // ULCT

	private String temp; // 온도데이터
	private Integer tnw; // 현재온도
	private String tff; // 온도1 시간	
	private Integer tlo; // 통신 구간 내 최저온도	
	private Integer thi; // 통신 구간 내 최고온도		
	private Integer tav; // 평균온도 (temp의 온도 평균)

	private String year;
	private String month;
	private String day;
	private String week;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	public void determineVersion() {
		this.ver = BeanUtils.get(VersionChecker.class).determine(this);
	}

	// TO-DO: Delete
	// 2rd Using (Save Normal Value), No code used as getter
	private Integer rc;
	private Integer rcn; // 통신 구간 내 제거횟수
	private String ac; // 통신 구간 내 충격 횟수

	// 2rd Used (Saved Normal Value, Currently Save Null) , Not currently use 
	private Integer rtr; // 통신 구간 내 누적제거시간(초)
	private String rtf; // 통신 구간 내 최초제거시각
	private String rtl; // 통신 구간 내 마지막부착시각
	private String epw; // 외부 전원 입력 (‘Y’ / ‘N’)

	// Not Used
	private String tv;
	private String cf;
	private Double tp; // 온도 (0 : 정보없음)
	private String pw; // 외부 전원 여부 (Y:부착, N:제거, X:정보없음)
	private String ra; // 금형 부착 여부 (Y:부착, N:제거, X:정보없음)	
	private String rat; // 현재 부착 상태(부착:‘Y’,제거:‘N’)

}
