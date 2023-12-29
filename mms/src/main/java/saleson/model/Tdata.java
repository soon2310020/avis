package saleson.model;

import lombok.Getter;
import lombok.Setter;
import saleson.service.transfer.VersionChecker;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.emoldino.framework.util.BeanUtils;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Tdata {
	@Id
	//@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String ti;
	private String dh;
	private String ip;
	private String gw;
	private String dn;
	private Integer rc;
	private String tv;

	// R2 (TDATA) - 2019.11.27.
	private String nt; // network(WIFI or LAN)

	// R2 (TDATA) - 2020.02.14
	private String dn2;

	private Instant createdAt;
	private String ver;

	public void determineVersion() {
		this.ver = BeanUtils.get(VersionChecker.class).determine(this);
	}
}
