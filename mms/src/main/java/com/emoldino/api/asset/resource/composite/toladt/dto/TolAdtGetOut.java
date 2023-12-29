package com.emoldino.api.asset.resource.composite.toladt.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class TolAdtGetOut extends PageImpl<TolAdtItem> {
	private List<TolAdtDistribution> distributions;
	private TolAdtUtilizationSummary utilizationSummary;
	private TolAdtStatusSummary statusSummary;
	private TolAdtAreaSummary areaSummary;

	public TolAdtGetOut(Page<TolAdtItem> page) {
		super(page.getContent(), page.getPageable(), page.getTotalElements());
	}

	public TolAdtGetOut(List<TolAdtItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public TolAdtGetOut(List<TolAdtItem> content) {
		super(content);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TolAdtDistribution {
		@DataLeakDetector(disabled = true)
		private Long locationId;
		private String locationName;
		private Double latitude;
		private Double longitude;
		private String address;

//		private long inProduction;
//		private long idle;
//		private long inactive;
//		private long sensorOffline;
//		private long sensorDetached;
//		private long onStandby;
//		private long noSensor;
	}

	@Data
	@NoArgsConstructor
	public static class TolAdtUtilizationSummary {
		private long low;
		private long medium;
		private long high;
		private long prolonged;

		public long getTotal() {
			return low + medium + high + prolonged;
		}
	}

	@Data
	@NoArgsConstructor
	public static class TolAdtStatusSummary {
		private long inProduction;
		private long idle;
		private long inactive;
		private long sensorDetached;
		private long sensorOffline;
		private long onStandby;
		private long noSensor;

		public long getTotal() {
			return inProduction + idle + inactive + sensorDetached + sensorOffline + onStandby + noSensor;
		}
	}

	@Data
	@NoArgsConstructor
	public static class TolAdtAreaSummary {
		private long production;
		private long warehouse;
		private long maintenance;
		private long unknown;

		public long getTotal() {
			return production + warehouse + maintenance + unknown;
		}
	}
}
