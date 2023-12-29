package com.emoldino.api.analysis.resource.composite.cyctimflu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycTimFluItem {
	private Long supplierId;
	private String supplierName;
	private String supplierCode;
	private Long moldCount;

	private Double averageCycleTime;
	private Double ctFluctuation;
	private Double l1Limit;
	private Double nctf;
	private Double ctfTrend;

	public CycTimFluItem(Long supplierId, String supplierName, String supplierCode, Long moldCount, Float averageCycleTime, Float ctFluctuation, Float l1Limit, Float nctf,
			Double ctfTrend) {
		this.supplierId = supplierId;
		this.supplierName = supplierName;
		this.supplierCode = supplierCode;
		this.moldCount = moldCount;
		this.averageCycleTime = averageCycleTime == null ? null : Double.valueOf(averageCycleTime);
		this.ctFluctuation = ctFluctuation == null ? null : Double.valueOf(ctFluctuation);
		this.l1Limit = l1Limit == null ? null : Double.valueOf(l1Limit);
		this.nctf = nctf == null ? null : Double.valueOf(nctf);
		this.ctfTrend = ctfTrend;
	}
}
