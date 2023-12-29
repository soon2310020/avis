package saleson.model.data;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.querydsl.core.annotations.*;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataOte implements Comparable<ChartDataOte> {

	private Long moldId;
	private String moldCode;
	private Instant moldCreatedAt;

	private String title;
	private Integer data = 0;
	private Double dataPercent = 0.0;
//	private Double trend;
	private Integer moldCount = 0;

	private Long uptime = 0L; // 분단위

	private Double cycleTime = 0.0;
	/*
	 * private Double maxCycleTime = 0.0; private Double minCycleTime = 0.0;
	 * 
	 * private Double cycleTimeMinusL1 = 0.0; private Double cycleTimeMinusL2 = 0.0; private Double cycleTimePlusL1 = 0.0; private Double cycleTimePlusL2 = 0.0;
	 * 
	 * 
	 * private Double cycleTimeWithin = 0.0; private Double cycleTimeL1 = 0.0; private Double cycleTimeL2 = 0.0;
	 */

	private Integer approvedUptime = 0;
	private Integer approvedCycleTime = 0;
	private Double contractedCycleTime = 0.0;
	/*
	 * 
	 * private Double avgCavities = 0.0;
	 * 
	 * private List<MoldShotData> moldShots;
	 * 
	 * private Double ulct; // average cycle time for c_i > c_1 private Double mfct; // most frequent cycle time c_1 private Double llct; // average cycle time for
	 * c_i < c_1
	 * 
	 * private Integer thi; // highest temperature private Integer tav; // average temperature private Integer tlo; // lowest temperature
	 * 
	 * private String compressedData; private Integer resetValue;
	 */

	private Long companyId;
	private String companyName;
	private String companyCode;
	//
	private Integer targetTimeOfToolingInHours;
	private Integer theoreticalNumberOfParts;
	private Integer totalNumberOfPartsProduced;
	private Integer numberOfRejectedParts;

	@QueryProjection
	public ChartDataOte(Long moldId, String moldCode, String companyCode, String companyName, String title, Instant moldCreatedAt, Long totalUptime, Double totalCycleTime,
			Integer targetTimeOfToolingInHours, Integer theoreticalNumberOfParts, Integer approvedCycleTime) {
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.companyCode = companyCode;
		this.companyName = companyName;
		this.title = title;
		this.moldCreatedAt = moldCreatedAt;
		this.uptime = totalUptime;
		this.cycleTime = totalCycleTime;
		this.targetTimeOfToolingInHours = targetTimeOfToolingInHours;
		this.theoreticalNumberOfParts = theoreticalNumberOfParts;
//		this.approvedUptime = approvedUptime;
		this.approvedCycleTime = approvedCycleTime;
	}

	@QueryProjection
	public ChartDataOte(Long moldId, String moldCode, String title, Integer totalNumberOfPartsProduced, Integer numberOfRejectedParts) {
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.title = title;
		this.totalNumberOfPartsProduced = totalNumberOfPartsProduced;
		this.numberOfRejectedParts = numberOfRejectedParts;
	}

	@JsonIgnore
	public String getMoldCode() {
		return moldCode;
	}

	public void setMoldCode(String moldCode) {
		this.moldCode = moldCode;
	}

	@Override
	public int compareTo(ChartDataOte o) {
		return Integer.compare(Integer.parseInt(title), Integer.parseInt(o.getTitle()));

	}
//
//	@Override
//	public String toString() {
//		return "ChartData{" +
//				"moldCode='" + moldCode + '\'' +
//				", title='" + title + '\'' +
//				", data=" + data +
//				", moldCount=" + moldCount +
//				", uptime=" + uptime +
//				", cycleTime=" + cycleTime +
//				", maxCycleTime=" + maxCycleTime +
//				", minCycleTime=" + minCycleTime +
//				", avgCavities=" + avgCavities +
//				"}";
//	}
}
