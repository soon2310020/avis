package saleson.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Version {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String ti;
	private String tv;		// 터미널 버전
	private String description;		// 터미널 버전 설명
	private String upurl;			// 업데이트(다운로드) URL
	private String applyYn;		// 적용여부
	private Instant applyDate; 		// 적용일시

	@CreatedDate
	private Instant createdAt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTi() {
		return ti;
	}

	public void setTi(String ti) {
		this.ti = ti;
	}

	public String getTv() {
		return tv;
	}

	public void setTv(String tv) {
		this.tv = tv;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUpurl() {
		return upurl;
	}

	public void setUpurl(String upurl) {
		this.upurl = upurl;
	}

	public String getApplyYn() {
		return applyYn;
	}

	public void setApplyYn(String applyYn) {
		this.applyYn = applyYn;
	}

	public Instant getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(Instant applyDate) {
		this.applyDate = applyDate;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
