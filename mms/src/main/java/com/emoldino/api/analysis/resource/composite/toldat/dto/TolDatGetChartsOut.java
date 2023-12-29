package com.emoldino.api.analysis.resource.composite.toldat.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TolDatGetChartsOut {
	private TolDatChart cycleTimeChart;
	private TolDatChart2 accelerationChart;
	private TolDatChart temperatureChart;

	public TolDatChart getCycleTimeChart() {
		if (this.cycleTimeChart == null) {
			this.cycleTimeChart = new TolDatChart();
		}
		return cycleTimeChart;
	}

	public TolDatChart2 getAccelerationChart() {
		if (this.accelerationChart == null) {
			this.accelerationChart = new TolDatChart2();
		}
		return accelerationChart;
	}

	public TolDatChart getTemperatureChart() {
		if (this.temperatureChart == null) {
			this.temperatureChart = new TolDatChart();
		}
		return temperatureChart;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TolDatChart {
		private List<TolDatChartItem> items;

		public List<TolDatChartItem> getItems() {
			if (this.items == null) {
				this.items = new ArrayList<>();
			}
			return this.items;
		}

		public void addItem(TolDatChartItem item) {
			getItems().add(item);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Accessors(chain = true)
	public static class TolDatChartItem {
		private String x;
		private BigDecimal y;

		public TolDatChartItem setY(Double y) {
			this.y = ValueUtils.toBigDecimal(y, 1, 0d);
			return this;
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TolDatChart2 {
		private List<TolDatChartItem2> items;

		public List<TolDatChartItem2> getItems() {
			if (this.items == null) {
				this.items = new ArrayList<>();
			}
			return this.items;
		}

		public void addItem(TolDatChartItem2 item) {
			getItems().add(item);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Accessors(chain = true)
	public static class TolDatChartItem2 {
		private String x;
		private List<BigDecimal> y;

		public List<BigDecimal> getY() {
			if (this.y == null) {
				this.y = new ArrayList<>();
			}
			return this.y;
		}

		public TolDatChartItem2 addY(Double y) {
			getY().add(ValueUtils.toBigDecimal(y, 2, 0d));
			return this;
		}
	}
}
