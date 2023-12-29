package com.emoldino.api.analysis.resource.composite.cyctimdev.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetOut.CycTimDevDetails;
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
public class CycTimDevDetailsGetOut extends PageImpl<CycTimDevDetails> {
	private FltSupplier supplier;
	@Deprecated
	private FltPart part;

	public CycTimDevDetailsGetOut(List<CycTimDevDetails> content, Pageable pageable, long total, FltSupplier supplier, FltPart part) {
		super(content, pageable, total);
		this.supplier = supplier;
		this.part = part;
	}

	public CycTimDevDetailsGetOut(List<CycTimDevDetails> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CycTimDevDetails {
		private Long moldId;
		private String moldCode;
		private Double approvedCycleTime;
		private Double averageCycleTime;
		private Double nctd;
		private Double nctdTrend;
		private List<CycTimDevPart> parts;

		public CycTimDevDetails(Long moldId, String moldCode, Float approvedCycleTime, Float averageCycleTime, Float nctd, Double nctdTrend) {
			this.moldId = moldId;
			this.moldCode = moldCode;
			this.approvedCycleTime = ValueUtils.toDouble(approvedCycleTime, null);
			this.averageCycleTime = ValueUtils.toDouble(averageCycleTime, null);
			this.nctd = ValueUtils.toDouble(nctd, null);
			this.nctdTrend = nctdTrend;
		}

		public Double getApprovedCycleTimeSecond() {
			return approvedCycleTime != null ? approvedCycleTime / 10 : null;
		}

		public Double getAverageCycleTimeSecond() {
			return averageCycleTime != null ? averageCycleTime / 10 : null;
		}
	}

}
