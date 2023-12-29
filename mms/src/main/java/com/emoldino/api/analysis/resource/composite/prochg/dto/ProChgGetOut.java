package com.emoldino.api.analysis.resource.composite.prochg.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@SuppressWarnings("serial")
public class ProChgGetOut extends PageImpl<ProChgItem> {
	private long totalProcChgCount;
	private List<ProChgTopItem> topItems;
	private List<ProChgChartItem> chartItems;

	public ProChgGetOut(Page<ProChgItem> page) {
		super(page.getContent(), page.getPageable(), page.getTotalElements());
		topItems = new ArrayList<>();
		chartItems = new ArrayList<>();
	}

	public ProChgGetOut(List<ProChgItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
		topItems = new ArrayList<>();
		chartItems = new ArrayList<>();
	}

	public ProChgGetOut(List<ProChgItem> content) {
		super(content);
		topItems = new ArrayList<>();
		chartItems = new ArrayList<>();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ProChgTopItem {
		private Long moldId;
		private String moldCode;
		private Long procChgCount;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ProChgChartItem {
		private String title;
		private long procChgCount;
		private long prodQty;
	}
}
