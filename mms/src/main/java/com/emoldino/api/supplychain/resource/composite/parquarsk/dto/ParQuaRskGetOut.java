package com.emoldino.api.supplychain.resource.composite.parquarsk.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class ParQuaRskGetOut extends PageImpl<ParQuaRskItem> {
	private List<ParQuaRskChartItem> chartItems;
	private Long moldId;
	private String moldCode;
	private List<ParQuaRskHeatmapItem> heatmapItems;

	public ParQuaRskGetOut(List<ParQuaRskItem> content, Pageable pageable, long total, //
			List<ParQuaRskChartItem> chartItems, //
			Long moldId, String moldCode, List<ParQuaRskHeatmapItem> heatmapItems//
	) {
		super(content, pageable, total);
		this.chartItems = chartItems;
		this.moldId = moldId;
		this.moldCode = moldCode;
		this.heatmapItems = heatmapItems;
	}

	public ParQuaRskGetOut(List<ParQuaRskItem> content) {
		super(content);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ParQuaRskChartItem {
		private String title;
		@JsonIgnore
		private String timeValue;
		private Integer goodProdQty;
		private Integer prodQty;

		public ParQuaRskChartItem(String timeValue, Integer prodQty, Integer goodProdQty) {
			this.timeValue = timeValue;
			this.prodQty = prodQty;
			this.goodProdQty = goodProdQty;
		}

		public Integer getProdQty() {
			return ValueUtils.toInteger(prodQty, 0);
		}

		public Integer getGoodProdQty() {
			return ValueUtils.toInteger(goodProdQty, 0);
		}

		public Integer getBadProdQty() {
			return getProdQty() - getGoodProdQty();
		}

		public BigDecimal getEstimYieldRate() {
			return getProdQty() == 0L ? ValueUtils.toBigDecimal(0d, 1, 0d)//
					: ValueUtils.toBigDecimal(ValueUtils.toDouble(getGoodProdQty(), 0d) / ValueUtils.toDouble(getProdQty(), 0d) * 100, 1, 0d);
		}
	}
}
