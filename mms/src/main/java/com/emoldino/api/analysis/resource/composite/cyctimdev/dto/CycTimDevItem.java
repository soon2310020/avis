package com.emoldino.api.analysis.resource.composite.cyctimdev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycTimDevItem {
	private Long supplierId;
	private String supplierName;
	private String supplierCode;
	private Long moldCount;

	private Double aboveTolerance;
	private Double withinUpperL2Tolerance;
	private Double withinL1Tolerance;
	private Double withinLowerL2Tolerance;
	private Double belowTolerance;

	//sc
	private Integer shotCount;
	private Integer aboveToleranceSc;
	private Integer withinUpperL2ToleranceSc;
	private Integer withinL1ToleranceSc;
	private Integer withinLowerL2ToleranceSc;
	private Integer belowToleranceSc;

	private Double nctd;
	private Double nctdTrend;

	public CycTimDevItem(//
			Long supplierId, //
			String supplierName, //
			String supplierCode, //
			long moldCount, //
			Float aboveTolerance, //
			Float withinUpperL2Tolerance, //
			Float withinL1Tolerance, //
			Float withinLowerL2Tolerance, //
			Float belowTolerance, //
			Integer shotCount, //
			Integer aboveToleranceSc, //
			Integer withinUpperL2ToleranceSc, //
			Integer withinL1ToleranceSc, //
			Integer withinLowerL2ToleranceSc, //
			Integer belowToleranceSc, //
			Float nctd, //
			Double nctdTrend//
	) {
		this.supplierId = supplierId;
		this.supplierName = supplierName;
		this.supplierCode = supplierCode;
		this.moldCount = moldCount;
		this.aboveTolerance = aboveTolerance == null ? null : Double.valueOf(aboveTolerance);
		this.withinUpperL2Tolerance = withinUpperL2Tolerance == null ? null : Double.valueOf(withinUpperL2Tolerance);
		this.withinL1Tolerance = withinL1Tolerance == null ? null : Double.valueOf(withinL1Tolerance);
		this.withinLowerL2Tolerance = withinLowerL2Tolerance == null ? null : Double.valueOf(withinLowerL2Tolerance);
		this.belowTolerance = belowTolerance == null ? null : Double.valueOf(belowTolerance);
		this.shotCount = shotCount;
		this.aboveToleranceSc = aboveToleranceSc;
		this.withinUpperL2ToleranceSc = withinUpperL2ToleranceSc;
		this.withinL1ToleranceSc = withinL1ToleranceSc;
		this.withinLowerL2ToleranceSc = withinLowerL2ToleranceSc;
		this.belowToleranceSc = belowToleranceSc;
		this.nctd = nctd == null ? null : Double.valueOf(nctd);
		this.nctdTrend = nctdTrend;
	}
}
