package com.emoldino.api.analysis.resource.composite.mldcht.dto.get;

import java.util.ArrayList;
import java.util.List;

import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut.MldChtItem;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class MldChtGetOut extends ListOut<MldChtItem> {
	@ApiModelProperty(value = "Optimal Cycle Time Strategy")
	private String octs;

	@Data
	@NoArgsConstructor
	@JsonInclude(Include.NON_NULL)
	public static class MldChtItem {
		@ApiModelProperty(value = "Group of X Axis")
		private String group;
		@ApiModelProperty(value = "Title of X Axis")
		private String title;
		@ApiModelProperty(value = "yyyyMMddHHmm")
		private String time;

		// Shot Count Fields Group
		@ApiModelProperty(value = "Shot Count", example = "1")
		private Long sc;
		@ApiModelProperty(value = "Cavities", example = "1")
		private Double cvt;

		@ApiModelProperty(value = "Produced Quantity", example = "1")
		public Long getQty() {
			if (sc == null || cvt == null) {
				return null;
			} else if (sc <= 0L || cvt <= 0d) {
				return 0L;
			} else {
				return ValueUtils.toLong(sc * cvt, null);
			}
		}

		// Cycle Time Fields Group
		@ApiModelProperty(value = "Optimal Cycle Time", example = "1")
		private Double oct;
//		@ApiModelProperty(value = "Approved Cycle Time")
//		private Double act;
//		@ApiModelProperty(value = "Weighted Average Cycle Time")
//		private Double wact;
		@ApiModelProperty(value = "Cycle Time Within", example = "1")
		private Double ctIn;
		@ApiModelProperty(value = "Cycle Time L1", example = "1")
		private Double ctL1;
		@ApiModelProperty(value = "Cycle Time L2", example = "1")
		private Double ctL2;
		@ApiModelProperty(value = "Min Cycle Time", example = "1")
		private Double minCt;
		@ApiModelProperty(value = "Max Cycle Time", example = "1")
		private Double maxCt;

		@ApiModelProperty(value = "Cycle Time Upper Control Limit L2", example = "1")
		private Double ctUclL2;
		@ApiModelProperty(value = "Cycle Time Upper Control Limit L1", example = "1")
		private Double ctUclL1;
		@ApiModelProperty(value = "Cycle Time Lower Control Limit L1", example = "1")
		private Double ctLclL1;
		@ApiModelProperty(value = "Cycle Time Lower Control Limit L2", example = "1")
		private Double ctLclL2;

		@ApiModelProperty(value = "Average Temperature", example = "1")
		private Integer tav;
		@ApiModelProperty(value = "Highest Temperature", example = "1")
		private Integer thi;
		@ApiModelProperty(value = "Lowest Temperature", example = "1")
		private Integer tlo;

		@ApiModelProperty(value = "Uptime", example = "1")
		private Long uptime;

		@ApiModelProperty(value = "Parts")
		private List<MldChtItemPart> parts;

		public void addPart(MldChtItemPart part) {
			if (parts == null) {
				parts = new ArrayList<>();
			}
			parts.add(part);
		}
	}

	@Data
	public static class MldChtItemPart {
		@ApiModelProperty(value = "Shot Count", example = "1")
		private Long sc;
		@ApiModelProperty(value = "Cavities", example = "1")
		private Double cvt;
		@ApiModelProperty(value = "Part")
		private String part;
		@ApiModelProperty(value = "Produced Quantity", example = "1")
		private Long qty;
	}
}
