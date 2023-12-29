package com.emoldino.api.analysis.resource.composite.prdbtn.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetOut.PrdBtnItem;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.PriorityType;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PrdBtnGetOut extends ListOut<PrdBtnItem> {
	private List<PrdBtnChartItem> chartItems = new ArrayList<>();

	public PrdBtnGetOut(List<PrdBtnItem> content) {
		super(content);
	}

	public PrdBtnGetOut(List<PrdBtnItem> content, List<PrdBtnChartItem> chartItems) {
		super(content);
		this.chartItems = chartItems;
	}

	public void addChartItem(PrdBtnChartItem chartItem) {
		this.chartItems.add(chartItem);
	}

	public List<FltPart> getChartLegends() {
		if (chartItems == null) {
			return null;
		} else if (chartItems.isEmpty()) {
			return Collections.emptyList();
		}
		List<FltPart> parts = new ArrayList<>(chartItems.size());
		chartItems.forEach(item -> parts.add(new FltPart(item.getPartId(), item.getPartName(), item.getPartCode())));
		return parts;
	}

	public List<Map<String, Object>> getChartItems2() {
		if (chartItems == null) {
			return null;
		} else if (chartItems.isEmpty()) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> list = new ArrayList<>();
		PrdBtnChartItem firstItem = chartItems.get(0);
		for (int i = 1; i <= 50; i++) {
			String title = ValueUtils.getString(firstItem, "title" + i);
			if (ObjectUtils.isEmpty(title)) {
				break;
			}
			Map<String, Object> item2 = new LinkedHashMap<>();
			item2.put("title", title);

			String vfield = "value" + i;
			int len = chartItems.size();
			for (int j = 1; j <= len; j++) {
				PrdBtnChartItem item = chartItems.get(j - 1);
				String partId = ValueUtils.toString(item.getPartId());
				Long value = ValueUtils.getLong(item, vfield);
				item2.put(partId, value);
			}

			list.add(item2);
		}
		return list;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class PrdBtnItem {
		private Long partId;
		private String partName;
		private String partCode;
		private long moldCount;
		private long productionDemand;
		private long requiredQuantity;
		private long producedQuantity;
		private long predictedQuantity;
		private long productionCapacity;
		private PriorityType deliveryRiskLevel;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonInclude(Include.NON_NULL)
	public static class PrdBtnChartItem {
		private Long partId;
		private String partName;
		private String partCode;
		private String title1;
		private Long value1;
		private String title2;
		private Long value2;
		private String title3;
		private Long value3;
		private String title4;
		private Long value4;
		private String title5;
		private Long value5;
		private String title6;
		private Long value6;
		private String title7;
		private Long value7;
		private String title8;
		private Long value8;
		private String title9;
		private Long value9;
		private String title10;
		private Long value10;
		private String title11;
		private Long value11;
		private String title12;
		private Long value12;
		private String title13;
		private Long value13;
		private String title14;
		private Long value14;
		private String title15;
		private Long value15;
		private String title16;
		private Long value16;
		private String title17;
		private Long value17;
		private String title18;
		private Long value18;
		private String title19;
		private Long value19;
		private String title20;
		private Long value20;
		private String title21;
		private Long value21;
		private String title22;
		private Long value22;
		private String title23;
		private Long value23;
		private String title24;
		private Long value24;
		private String title25;
		private Long value25;
		private String title26;
		private Long value26;
		private String title27;
		private Long value27;
		private String title28;
		private Long value28;
		private String title29;
		private Long value29;
		private String title30;
		private Long value30;
		private String title31;
		private Long value31;
		private String title32;
		private Long value32;
		private String title33;
		private Long value33;
		private String title34;
		private Long value34;
		private String title35;
		private Long value35;
		private String title36;
		private Long value36;
		private String title37;
		private Long value37;
		private String title38;
		private Long value38;
		private String title39;
		private Long value39;
		private String title40;
		private Long value40;
		private String title41;
		private Long value41;
		private String title42;
		private Long value42;
		private String title43;
		private Long value43;
		private String title44;
		private Long value44;
		private String title45;
		private Long value45;
		private String title46;
		private Long value46;
		private String title47;
		private Long value47;
		private String title48;
		private Long value48;
		private String title49;
		private Long value49;
		private String title50;
		private Long value50;
	}
}
