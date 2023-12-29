package saleson.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;
import saleson.model.support.*;

@Entity
@Table(indexes = { @Index(name = "IDX_MOLD_STANDARD_VALUE", columnList = "MOLD_ID") })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Getter
@Setter
@NoArgsConstructor
public class MoldStandardValue extends DateAudit {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "MOLD_ID")
	private Long moldId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_ID", referencedColumnName = "ID", insertable = false, updatable = false)
	private Mold mold;

	private String month;

	@Column(length = 2)
	private int periodMonths;

	@Column(length = 6)
	private int minCdataCount;
	@Column(length = 6)
	private int maxCdataCount;

	@Column(length = 6)
	private int cdataCount;

	private Double standardValue;

	@Deprecated
	public MoldStandardValue(Mold mold, String month, Double standardValue) {
		this(mold == null ? null : mold.getId(), month, 1, 30, 50, 0, standardValue);
	}

//	@Deprecated
//	public MoldStandardValue(Mold mold, String month, int periodMonths, int minCdataCount, int maxCdataCount, int cdataCount, Double standardValue) {
//		this(mold == null ? null : mold.getId(), month, periodMonths, minCdataCount, maxCdataCount, cdataCount, standardValue);
//	}

	public MoldStandardValue(Long moldId, String month, int periodMonths, int minCdataCount, int maxCdataCount, int cdataCount, Double standardValue) {
//		this.mold = mold;
		this.moldId = moldId;
		this.month = month;
		this.periodMonths = periodMonths;
		this.minCdataCount = minCdataCount;
		this.maxCdataCount = maxCdataCount;
		this.cdataCount = cdataCount;
		this.standardValue = standardValue;
	}
}
