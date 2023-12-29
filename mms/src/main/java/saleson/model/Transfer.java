package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.service.transfer.VersionChecker;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class Transfer {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String at;
	private String ti;
	private String ci;
	private Integer sc;
	private String lst;
	private String rt;
	private String cf; // 정의서에는 INT(11) Java는 String => String으로 변경..
	private Double ct; // 정의서에는 INT(11) 소수점이 넘어와서 Double로 변경..
	private String bs;
	private Integer rc; // router count (R2 => 삭제됨)
	private String tv;
	private String dh; // 1, 0 (R2 => Y:유동, N:고정)
	private String ip;
	private String gw;
	private String dn;
	private Integer sn;

	@Column(length = 500)
	private String es;

	@Column(length = 500)
	private String ds; // Decrypt String

	// v1.2 CDATA
	private Double tp; // 온도 (0 : 정보없음)
	private String ra; // 금형 부착 여부 (Y:부착, N:제거, X:정보없음)
	private String pw; // 외부 전원 여부 (Y:부착, N:제거, X:정보없음)

	// R2 (TDATA) - 2019.11.27.
	private String nt; // network(WIFI or LAN)

	// R2 (TDATA) - 2020.02.14.
	private String dn2; // DNS2

	// R2 (CDATA) - 2019.11.27.
	//- rcn: Number of deletions in communication interval - ex) 1
	private Integer rcn; // 통신 구간 내 제거횟수
	//rtr: cumulative removal time in seconds - ex) 32
	private Integer rtr; // 통신 구간 내 누적제거시간(초)
	//rtf: Initial removal time in communication interval - ex) 20191101110938
	private String rtf; // 통신 구간 내 최초제거시각
	//rtl: Last attachment time in communication section - ex) 20191101110938
	private String rtl; // 통신 구간 내 마지막부착시각
	//Rat: Attachment status (Y/N) - ex) Y
	private String rat; // 현재 부착 상태(부착:‘Y’,제거:‘N’)
	private Integer tlo; // 통신 구간 내 최저온도
	private String tlf; // 통신 구간 내 최저온도의최초시각
	private Integer thi; // 통신 구간 내 최고온도
	private String thf; // 통신 구간 내 최고온도의최초시각
	private Integer tnw; // 현재온도
	private String ac; // 통신 구간 내 충격 횟수
	private String epw; // 외부 전원 입력 (‘Y’ / ‘N’)

	// R2 (CDATA) - 2020.02.14.
	private String ctt; // CT TABLE
	private String temp; // 온도데이터
	private String tff; // 온도1 시간
	private Integer tav; // 평균온도 (temp의 온도 평균)

	// R2 - 2020.04.24
	private Double llct; // LLCT
	private Double ulct; // ULCT

	// ADATA - 2020.06.18
	private String acc; // 가속도 정보

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt; // 데이터 수신 시간.

	// Log Transfer ID (Usable at some points)
	@Transient
	private Long li;

	public Tdata toTdata() {
		Tdata data = new Tdata();
		data.setId(id);
		data.setTi(ti);
		data.setDh(dh);
		data.setDn(dn);
		data.setGw(gw);
		data.setIp(ip);
		data.setRc(rc);
		data.setTv(tv);
		data.setCreatedAt(createdAt);

		// r2 - 2019.11.27
		data.setNt(nt);

		// r2 - 2019.11.27
		data.setDn2(dn2);

		data.determineVersion();
		return data;
	}

	public Cdata toCdata() {
		Cdata data = new Cdata();

		// Common(2rd, 3rd) Use 
		data.setId(id);
		data.setTi(ti);
		data.setCi(ci);
		data.determineVersion();
		data.setSc(sc);
		data.setLst(lst);
		data.setRt(rt);
		data.setBs(bs);
		data.setSn(sn);

		data.setCt(ct);
		data.setCtt(ctt);
		data.setLlct(llct);
		data.setUlct(ulct);

		data.setTemp(temp);
		data.setTnw(tnw);
		data.setTff(tff);
		data.setTlo(tlo);
		data.setThi(thi);
		data.setTav(tav);

		data.setYear(ValueUtils.abbreviate(lst, 4));
		data.setMonth(ValueUtils.abbreviate(lst, 6));
		data.setDay(ValueUtils.abbreviate(lst, 8));
		data.setWeek(DateUtils2.toOtherPattern(lst, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));

		data.setCreatedAt(createdAt);

		// TO-DO: Delete
		// 2rd Using (Save Normal Value), No code used as getter
		data.setRc(rc);
		data.setRcn(rcn);
		data.setAc(ac);

		// 2rd Used (Saved Normal Value, Currently Save Null) , Not currently use 
		data.setRtr(rtr);
		data.setRtf(rtf);
		data.setRtl(rtl);
		data.setEpw(epw);

		// Not Used
		data.setTv(tv);
		data.setCf(cf); // Only N
		data.setTp(tp); // Temperature
		data.setPw(pw);
		data.setRa(ra);
		data.setRat(rat);

		return data;
	}

	public Adata toAdata() {
		Adata data = new Adata();
		data.setId(id);
		data.setTi(ti);
		data.setCi(ci);
		data.setLst(lst);
		data.setRt(rt);
		data.setAcc(acc);
		data.setSn(sn);

		data.setYear(ValueUtils.abbreviate(lst, 4));
		data.setMonth(ValueUtils.abbreviate(lst, 6));
		data.setDay(ValueUtils.abbreviate(lst, 8));
		data.setWeek(DateUtils2.toOtherPattern(lst, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));

		data.setCreatedAt(createdAt);

		data.determineVersion();
		return data;
	}

	public LogTransfer toLogTransfer() {
		LogTransfer logTransfer = new LogTransfer();
		logTransfer.setAt(at);
		logTransfer.setTi(ti);
		logTransfer.setCi(ci);
		logTransfer.setEs(es);
		logTransfer.setDs(ds);

		// R2 - 2019.11.27.
		logTransfer.setTv(tv); // tv 정보를 추가로 등록
		logTransfer.setCreatedAt(createdAt);

		return logTransfer;
	}

	public boolean isVersionR2() {
		return VersionChecker.VERSION_R2.equals(BeanUtils.get(VersionChecker.class).determineFromTi(getTi()));
	}
}
