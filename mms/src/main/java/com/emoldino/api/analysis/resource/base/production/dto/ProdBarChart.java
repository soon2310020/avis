package com.emoldino.api.analysis.resource.base.production.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdBarChart {
	private List<String> labels;
	private List<ProdBarChartRow> rows;
	private List<ProdBarChartSpot> spots;

	public void addRow(ProdBarChartRow row) {
		if (this.rows == null) {
			this.rows = new ArrayList<>();
		}
		this.rows.add(row);
	}

	public void addSpot(ProdBarChartSpot spot) {
		if (this.spots == null) {
			this.spots = new ArrayList<>();
		}
		this.spots.add(spot);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProdBarChartRow {
		private String type;
		private String label;
		private String unit;
		private String color;
		private List<ProdBarChartValue> columns;

		public void addColumn(ProdBarChartValue column) {
			if (this.columns == null) {
				this.columns = new ArrayList<>();
			}
			this.columns.add(column);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProdBarChartValue {
		private String label;
		private BigDecimal value;
		private ProdTooltip tooltip;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProdBarChartSpot {
		private String label;
		private BigDecimal value;
		private ProdTooltip tooltip;
	}
}
