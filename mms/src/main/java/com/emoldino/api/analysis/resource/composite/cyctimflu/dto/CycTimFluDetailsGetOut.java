package com.emoldino.api.analysis.resource.composite.cyctimflu.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("serial")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CycTimFluDetailsGetOut extends PageImpl<CycTimFluDetailsGetOut.CycTimFluDetails> {
	private FltSupplier supplier;
	@Deprecated
	private FltPart part;

	public CycTimFluDetailsGetOut(List<CycTimFluDetails> content, Pageable pageable, long total, FltSupplier supplier, FltPart part) {
		super(content, pageable, total);
		this.supplier = supplier;
		this.part = part;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CycTimFluDetails {
		private Long moldId;
		private String moldCode;
		private Double approvedCycleTime;
		private Double averageCycleTime;
		private Double ctFluctuation;
		private Double l1Limit;
		private Double nctf;
		private Double ctfTrend;
		private List<CycTimFluPart> parts;

		public CycTimFluDetails(Long moldId, String moldCode, Float approvedCycleTime, Float averageCycleTime, Float ctFluctuation, Float l1Limit, Float nctf, Double ctfTrend) {
			this.moldId = moldId;
			this.moldCode = moldCode;
			this.approvedCycleTime = ValueUtils.toDouble(approvedCycleTime, null);
			this.averageCycleTime = ValueUtils.toDouble(averageCycleTime, null);
			this.ctFluctuation = ValueUtils.toDouble(ctFluctuation, null);
			this.l1Limit = ValueUtils.toDouble(l1Limit, null);
			this.nctf = ValueUtils.toDouble(nctf, null);
			this.ctfTrend = ctfTrend;
		}

		public Double getApprovedCycleTimeSecond() {
			return approvedCycleTime != null ? approvedCycleTime / 10 : null;
		}

		public Double getAverageCycleTimeSecond() {
			return averageCycleTime != null ? averageCycleTime / 10 : null;
		}

		public Double getL1LimitSecond() {
			return l1Limit != null ? l1Limit / 10 : null;
		}

		public Double getCtFluctuationSecond() {
			return ctFluctuation != null ? ctFluctuation / 10 : null;
		}
	}

}
