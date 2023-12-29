package saleson.model;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.emoldino.framework.util.BeanUtils;

import lombok.Data;
import saleson.service.transfer.VersionChecker;

@Entity
@Data
public class Adata {
	@Id
	private Long id;
	private Long moldId;
	private String moldCode;

	private String ti; // id[0] : 터미널 ID
	private String ci; // id[1] : 카운터 ID
	private String lst; // time[0] : 마지막 SHOT 시각 (측정시작 시각)
	private String rt; // time[1] : 데이터 수신 시각

	private String acc; // 가속도 정보
	private Integer sn; // 통신 순차 번호

	private String year;
	private String month;
	private String day;
	private String week;

	private String ver;
	private Instant createdAt;

	public void determineVersion() {
		this.ver = BeanUtils.get(VersionChecker.class).determine(this);
	}
}
