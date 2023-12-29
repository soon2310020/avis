package com.emoldino.api.analysis.resource.composite.cdtisp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CdtIspHourly {
	private String ci;
	private String ti;
	private String moldCode;
	private String hour;
	private String tffFrom;
	private String tffTo;
	private String rtFrom;
	private String rtTo;
	private int sc;
	private int shotCount;
	private int shotCountVal;

	private long transferCount;
	private long statisticsCount;

	private List<CdtIspStatistics> items;

	public void addItem(CdtIspStatistics item) {
		if (items == null) {
			items = new ArrayList<>();
		}
		items.add(item);
	}
}
