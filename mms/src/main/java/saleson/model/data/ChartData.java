package saleson.model.data;

import java.time.*;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.querydsl.core.annotations.*;

import lombok.*;
import saleson.common.enumeration.*;
import saleson.service.util.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartData implements Comparable<ChartData> {

	private Long moldId;
	private String moldCode;
	private Instant moldCreatedAt;

	// X Axis
	private String title;

	// Quantity
	private Integer data = 0;
	private Double dataPercent = 0.0;
	private Double trend;
	private Integer moldCount = 0;

	// Maximum Capacity
	private Integer maxCapacity = 0;

	// Cycle Time
	private Double cycleTime = 0.0;
	private Double maxCycleTime = 0.0;
	private Double minCycleTime = 0.0;

	private Double cycleTimeMinusL1 = 0.0;
	private Double cycleTimeMinusL2 = 0.0;
	private Double cycleTimePlusL1 = 0.0;
	private Double cycleTimePlusL2 = 0.0;

	private Double cycleTimeWithin = 0.0;
	private Double cycleTimeL1 = 0.0;
	private Double cycleTimeL2 = 0.0;

	private Integer approvedCycleTime = 0;
	private Double contractedCycleTime = 0.0;

	// Uptime
	private Long uptime = 0L; // 분단위
	private Integer approvedUptime = 0;

	private Double avgCavities = 0.0;

	private List<MoldShotData> moldShots;

	private Double ulct; // average cycle time for c_i > c_1
	private Double mfct; // most frequent cycle time c_1
	private Double llct; // average cycle time for c_i < c_1

	private Integer thi; // highest temperature
	private Integer tav; // average temperature
	private Integer tlo; // lowest temperature

	private String compressedData;
	private Integer resetValue;
	private Integer lastShot;

	private Long companyId;
	private String companyName;
	private String companyCode;
	private int indexInHour = 0;// use for export

	private Integer totalPartProduced = 0;

	private List<PartShotData> partData = new ArrayList<>();

	private List<ResinCodeChangeData> resinCodeChangeData;

	/*
	 * @QueryProjection public ChartData(String moldCode, String title, Integer data){ this.moldCode = moldCode; this.title = title; this.data = data; }
	 *
	 * @QueryProjection public ChartData(String moldCode, String title, Long uptime){ this.moldCode = moldCode; this.title = title; this.uptime = uptime; }
	 *
	 * @QueryProjection public ChartData(String moldCode, String title, Double cycleTime, Double maxCycleTime, Double minCycleTime){ this.moldCode = moldCode;
	 * this.title = title; this.cycleTime = Math.round(cycleTime * 10) / 10.0; this.maxCycleTime = Math.round(maxCycleTime * 10) / 10.0; this.minCycleTime =
	 * Math.round(minCycleTime * 10) / 10.0; }
	 */

	// MoldCode -> MoldId
	@QueryProjection
	public ChartData(Long moldId, String title, Integer data) {
		this.moldId = moldId;
		this.moldCode = moldId != null ? Long.toString(moldId) : "";
		this.title = title;
		this.data = data;
	}

	@QueryProjection
	public ChartData(Long moldId, Long companyId, String title, Integer data) {
		this.moldId = moldId;
		this.moldCode = moldId != null ? Long.toString(moldId) : "";
		this.companyId = companyId;
		this.title = title;
		this.data = data;
	}
	/*
	 * @QueryProjection public ChartData( Long moldId, Long companyId, Long fakeId, String title, Float dataPercent){ this.moldCode =moldId!=null?
	 * Long.toString(moldId):""; this.companyId = companyId; this.title = title; this.dataPercent = dataPercent; }
	 */

	@QueryProjection
	public ChartData(Long moldId, String title, Long uptime) {
		this.moldId = moldId;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.uptime = uptime;
	}

	@QueryProjection
	public ChartData(Long moldId, String title, Integer data, Long uptime) {
		this.moldId = moldId;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.data = data;
		this.uptime = uptime;
	}

	@QueryProjection
	public ChartData(Long moldId, String title, Instant moldCreatedAt, Long totalUptime, Double totalCycleTime, Integer approvedUptime, Integer approvedCycleTime) {
		this.moldId = moldId;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.moldCreatedAt = moldCreatedAt;
		this.uptime = totalUptime;
		this.cycleTime = totalCycleTime;
		this.approvedUptime = approvedUptime;
		this.approvedCycleTime = approvedCycleTime;
	}

//CYCLE_TIME
	@QueryProjection
	public ChartData(Long moldId, String title, Double cycleTimeMultiplyShotCount, Integer shotCount, Double maxCycleTime, Double minCycleTime) {
		this.moldId = moldId;
		Double cycleTime = shotCount != 0 ? cycleTimeMultiplyShotCount / (double) shotCount : 0.0;
		this.data = shotCount;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.cycleTime = Math.round(cycleTime * 10.0) / 100.0;
		this.maxCycleTime = maxCycleTime != null ? Math.round(maxCycleTime * 10.0) / 100.0 : 0.0;
		this.minCycleTime = minCycleTime != null ? Math.round(minCycleTime * 10.0) / 100.0 : 0.0;
	}

	@QueryProjection
	public ChartData(Long moldId, String title, Integer data, Double avgCavities) {
		this.moldId = moldId;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.data = data;
		this.avgCavities = avgCavities;
	}

//CYCLE_TIME_ANALYSIS
	@QueryProjection
	public ChartData(Long moldId, String title, Integer data, Double ulct, Double mfct, Double llct, Long uptime) {
		this.moldId = moldId;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.data = data;
		this.ulct = ulct != null ? Math.round(ulct * 10.0) / 100.0 : 0.0;
		this.mfct = mfct != null ? Math.round(mfct * 10.0) / 100.0 : 0.0;
		this.llct = llct != null ? Math.round(llct * 10.0) / 100.0 : 0.0;
		this.uptime = uptime;
	}

	@QueryProjection
	public ChartData(Long moldId, String title, Integer data, Integer thi, Integer tav, Integer tlo) {
		this.moldId = moldId;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.data = data;
		this.thi = thi;
		this.tav = tav;
		this.tlo = tlo;
	}

	@QueryProjection
	public ChartData(String title, String compressedData) {
		this.title = title;
		this.compressedData = compressedData;
	}

	// CYCLE_TIME_ANALYSIS cycle time
	@QueryProjection
	public ChartData(Long moldId, String title, String compressedData) {
		this.moldId = moldId;
		this.moldCode = Long.toString(moldId);
		this.title = title;
		this.compressedData = compressedData;
	}

	@QueryProjection
	public ChartData(Long companyId, String companyCode, String companyName, Integer data) {
		this.companyId = companyId;
		this.companyCode = companyCode;
		this.companyName = companyName;
		this.data = data;
	}

//	@QueryProjection
//	public ChartData(Long moldId, String title, Double cycleTime){
//		this.moldCode = Long.toString(moldId);
//		this.title = title;
//		this.cycleTime = cycleTime;
//	}

	public ChartData(String title, Integer data) {
		this.title = title;
		this.data = data;
	}

	@QueryProjection
	public ChartData(String moldCode, String title, Double cycleTime) {
		this.moldCode = moldCode;
		this.title = title;
		this.cycleTime = cycleTime;
	}

	public ChartData(String title, Integer data, Integer moldCount) {
		this.title = title;
		this.data = data;
		this.moldCount = moldCount;
	}

	public ChartData(String title, Long uptime, Double avgCavities, Integer moldCount) {
		this.title = title;
		this.uptime = uptime; // minutes
		this.avgCavities = avgCavities;
		this.moldCount = moldCount;
	}

	public Double getUptimeMinute() {
		return uptime != null ? Math.round(((double) this.uptime / (60)) * 10) / 10.0 : 0.0;
	}

	public Double getUptimeHour() {
		return uptime != null ? Math.round(((double) this.uptime / (60 * 60)) * 10) / 10.0 : 0.0;
	}

	enum DisplayTime {
		HOUR(60 * 60), MINUTE(60), SECOND(1);

		private int divisionValue;

		DisplayTime(int divisionValue) {
			this.divisionValue = divisionValue;
		}

		public int getDivisionValue() {
			return divisionValue;
		}
	}

	@JsonIgnore
	public String getMoldCode() {
		return moldCode;
	}

	@Override
	public int compareTo(ChartData o) {
		return Integer.compare(Integer.parseInt(title), Integer.parseInt(o.getTitle()));

	}

	@Override
	public String toString() {
		return "ChartData{" + "moldCode='" + moldCode + '\'' + ", title='" + title + '\'' + ", data=" + data + ", moldCount=" + moldCount + ", uptime=" + uptime + ", cycleTime="
				+ cycleTime + ", maxCycleTime=" + maxCycleTime + ", minCycleTime=" + minCycleTime + ", avgCavities=" + avgCavities + "}";
	}

	public static void populate(List<ChartData> list, double contractedCt, int limit1, OutsideUnit limit1Unit, int limit2, OutsideUnit limit2Unit,
			List<AvgCavityStatisticsData> avgCavityStatisticsDataList) {
		// 3. Cycle Time 체크 (L1, L2)
		double baseCycleTime = contractedCt / 10.0; // 기준 사이클 타임 (contracted? ) --> 초로 계산
		double cycleTimeL1 = limit1Unit == null || limit1Unit.equals(OutsideUnit.PERCENTAGE) ? baseCycleTime * limit1 * 0.01 : (double) limit1;
		double cycleTimeL2 = limit2Unit == null || limit2Unit.equals(OutsideUnit.PERCENTAGE) ? baseCycleTime * limit2 * 0.01 : (double) limit2;

		// 3-1. 기준 범위 ===== -L2 === -L1 === base === +L1 === +L2 =====
		double minusL2 = baseCycleTime - cycleTimeL2;
		double minusL1 = baseCycleTime - cycleTimeL1;
		double plusL1 = baseCycleTime + cycleTimeL1;
		double plusL2 = baseCycleTime + cycleTimeL2;

		for (ChartData chartData : list) {
			// L1, L2
			chartData.setCycleTimeMinusL1(minusL1);
			chartData.setCycleTimeMinusL2(minusL2);
			chartData.setCycleTimePlusL1(plusL1);
			chartData.setCycleTimePlusL2(plusL2);

			chartData.setContractedCycleTime(contractedCt / 10.0); // 초로 계산 (100ms -> sec)

			double cycleTime = chartData.getCycleTime();
			if (cycleTime <= minusL2 || plusL2 <= cycleTime) {
				chartData.setCycleTimeWithin(0.0);
				chartData.setCycleTimeL1(0.0);
				chartData.setCycleTimeL2(cycleTime);

			} else if ((minusL2 < cycleTime && cycleTime <= minusL1) || (plusL1 <= cycleTime && cycleTime < plusL2)) {
				chartData.setCycleTimeWithin(0.0);
				chartData.setCycleTimeL1(cycleTime);
				chartData.setCycleTimeL2(0.0);
			} else {
				chartData.setCycleTimeWithin(cycleTime);
				chartData.setCycleTimeL1(0.0);
				chartData.setCycleTimeL2(0.0);
			}

			if (avgCavityStatisticsDataList != null) {
				Integer totalCavity = avgCavityStatisticsDataList.stream().filter(x -> x.getTitle().equals(chartData.getTitle())).map(m -> m.getTotalCavity()).findAny().orElse(0);
				if (chartData.getData() == 0) {
					chartData.setAvgCavities(NumberUtils.roundOffNumber(0.0));
				} else {
					Double avgCavities = (double) totalCavity / (double) chartData.getData();
					chartData.setAvgCavities(NumberUtils.roundOffNumber(avgCavities));
				}
			}
		}
	}
}
