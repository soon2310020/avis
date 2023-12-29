package com.emoldino.api.asset.resource.composite.toleol.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("serial")
public class TolEolGetOut extends PageImpl<TolEolItem> {
	private List<TolEolChartItem> chartItems;

	public TolEolGetOut(List<TolEolItem> content, Pageable pageable, long total, List<TolEolChartItem> chartItems) {
		super(content, pageable, total);
		this.chartItems = chartItems;
	}

	public TolEolGetOut(List<TolEolItem> content) {
		super(content);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TolEolChartItem {
		private String title;
		private long eolMoldCount;
	}
}
