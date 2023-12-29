package com.emoldino.api.analysis.resource.composite.cdtisp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdtIspItem {
	private Long id;
	private String createdAtStr;
	private String ci;
	private String ti;
	private String tv;
	private String ds;
	private int sn;

	private List<TrsViwTransfer> transfer;

	public void addTransfer(TrsViwTransfer transfer) {
		if (this.transfer == null) {
			this.transfer = new ArrayList<>();
		}
		this.transfer.add(transfer);
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TrsViwTransfer {
		private Long id;
		private String createdAtStr;
		private TrsViwCdata cdata;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TrsViwCdata {
		private String rt;
		private String lst;
		private String tff;
		private Long moldId;
		private int sc;

		private List<TrsViwStatistics> statistics;

		public void addStatistics(TrsViwStatistics statistics) {
			if (this.statistics == null) {
				this.statistics = new ArrayList<>();
			}
			this.statistics.add(statistics);
		}
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TrsViwStatistics {
		private Long id;
		private String createdAtStr;
		private String lst;
		private int fsc;
		private int shotCount;
	}
}
