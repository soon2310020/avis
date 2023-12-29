package saleson.model.data;

import com.emoldino.framework.util.*;
import com.querydsl.core.annotations.*;

import lombok.*;
import saleson.common.enumeration.*;
import saleson.common.util.DataUtils;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MoldCapacityReportData {
	private Long id;
	private String title;
	private Integer maxCapacity;
	private Long maxCapacityLong = 0L;
	private Long outputCapacity = 0L;
	private Double outputCapacityPercent = 0.0;
	private Long availableOutput = 0L;
	private Double availableOutputPercent = 0.0;
	private Long availableDowntime = 0L;
	private Double availableDowntimePercent = 0.0;
	private Long overCapacity = 0L;
	private Double overCapacityPercent = 0.0;
	private Integer contractedCycleTime;

	private Integer data;
	private Long dataLong;
	Integer year;
	Integer weekOrMonth;
	Integer day;

	@QueryProjection
	public MoldCapacityReportData(Long id, String title, Integer maxCapacity, Integer outputCapacity) {
		this.id = id;
		this.title = title;
		this.maxCapacity = maxCapacity;
		this.outputCapacity = outputCapacity.longValue();
	}

	@QueryProjection
	public MoldCapacityReportData(String title, Integer maxCapacity, Integer outputCapacity) {
		this.title = title;
		this.maxCapacity = maxCapacity;
		this.outputCapacity = outputCapacity.longValue();
	}

	@QueryProjection
	public MoldCapacityReportData(Long id, String title, Integer data) {
		this.id = id;
		this.title = title;
		this.data = data;
		this.dataLong = ValueUtils.toLong(data, null);
	}

	@QueryProjection
	public MoldCapacityReportData(Long id, String title, Long data) {
		this.id = id;
		this.title = title;
		setDataLong(data);
	}

	@QueryProjection
	public MoldCapacityReportData(Long id, String title, Integer data, Integer outputCapacity, Integer contractedCycleTime) {
		this.id = id;
		this.title = title;
		this.data = data;
		this.dataLong = ValueUtils.toLong(data, null);
		this.contractedCycleTime = contractedCycleTime;
	}

	@QueryProjection
	public MoldCapacityReportData(Long id, String title, Long data, Integer outputCapacity, Integer contractedCycleTime) {
		this.id = id;
		this.title = title;
		setDataLong(data);
		this.contractedCycleTime = contractedCycleTime;
	}

	@QueryProjection
	public MoldCapacityReportData(String title, Integer data) {
		this.title = title;
		this.data = data;
		this.dataLong = ValueUtils.toLong(data, null);
	}

	public Long getDataLong() {
		return dataLong == null ? ValueUtils.toLong(data, null) : dataLong;
	}
	public void setDataLong(Long dataLong) {
		this.data = ValueUtils.toInteger(dataLong, null);
		this.dataLong = dataLong;
	}

	public String getKey() {
		return this.id + "_" + this.title;
	}

	public MoldCapacityReportData convert(Frequent frequent) {
		MoldCapacityReportData moldCapacityReportData = this;
//        moldPartYearWeekOrMonth.setCount(Long.valueOf(quantity));
		if (this.title != null && this.title.length() >= 4) {
			moldCapacityReportData.setYear(Integer.valueOf(this.title.substring(0, 4)));
		}
		if (frequent.equals(Frequent.DAILY) || frequent.equals(Frequent.WEEKLY) || frequent.equals(Frequent.MONTHLY)) {
			if (this.title != null && this.title.length() >= 6) {
				moldCapacityReportData.setWeekOrMonth(Integer.valueOf(this.title.substring(4, 6)));
			}
			if (frequent.equals(Frequent.DAILY) && this.title.length() >= 8) {
				moldCapacityReportData.setDay(Integer.valueOf(this.title.substring(6, 8)));
			}
		}
		return moldCapacityReportData;
	}

	public void makePercent() {
		if (this.getMaxCapacity() != null && !this.getMaxCapacity().equals(0)) {
			if (this.getMaxCapacity() < 0) {
				this.setOutputCapacityPercent(DataUtils.round(100 * Double.valueOf(this.getOutputCapacity()) / this.getMaxCapacityLong(), 2));
				this.setAvailableDowntimePercent(DataUtils.round(100 * Double.valueOf(this.getAvailableDowntime()) / this.getMaxCapacityLong(), 2));
				this.setAvailableOutputPercent(DataUtils.round(100 * Double.valueOf(this.getAvailableOutput()) / this.getMaxCapacityLong(), 2));
				this.setOverCapacityPercent(DataUtils.round(100 * Double.valueOf(this.getOverCapacity()) / this.getMaxCapacityLong(), 2));
			} else {
				this.setOutputCapacityPercent(DataUtils.round(100 * Double.valueOf(this.getOutputCapacity()) / this.getMaxCapacity(), 2));
				this.setAvailableDowntimePercent(DataUtils.round(100 * Double.valueOf(this.getAvailableDowntime()) / this.getMaxCapacity(), 2));
				this.setAvailableOutputPercent(DataUtils.round(100 * Double.valueOf(this.getAvailableOutput()) / this.getMaxCapacity(), 2));
				this.setOverCapacityPercent(DataUtils.round(100 * Double.valueOf(this.getOverCapacity()) / this.getMaxCapacity(), 2));
			}
		}
	}

	public void makeDefault() {
		this.setAvailableDowntime(0L);
		this.setAvailableDowntimePercent(0.0);
		this.setAvailableOutput(maxCapacity > 0 ? maxCapacity.longValue() : maxCapacityLong);
		this.setAvailableOutputPercent(100.0);
		this.setOutputCapacityPercent(0.0);
		this.setOverCapacity(0L);
		this.setOverCapacityPercent(0.0);
	}
}
