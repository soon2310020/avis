package com.emoldino.api.analysis.resource.composite.toldat.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.base.data.util.MoldDataUtils;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut.MldDatItem;
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

@SuppressWarnings("serial")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TolDatGetOut extends PageImpl<MldDatItem> {
	public TolDatGetOut(List<MldDatItem> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonInclude(Include.NON_NULL)
	public static class MldDatItem {
		private String time;
		private Double approvedCt;
		private Double wact;
		private Double actualCt;
		private Double ctLl2;
		private Double ctLl1;
		private Double ctUl1;
		private Double ctUl2;
		private Double temperature;
		private Double injectionTime;
		private Double packingTime;
		private Double coolingTime;

		public boolean isIdle() {
			return MoldDataUtils.isCtIdle(actualCt);
		}

		public boolean isOutOfL1() {
			return MoldDataUtils.isCtOutOfL1(actualCt, ctLl2, ctLl1, ctUl1, ctUl2);
		}

		public boolean isOutOfL2() {
			return MoldDataUtils.isCtOutOfL2(actualCt, ctLl2, ctUl2);
		}

		public MldDatItem(String time, Double approvedCt, Double wact, Double actualCt, Double ctLl2, Double ctLl1, Double ctUl1, Double ctUl2, Double temperature) {
			this.time = time;
			this.approvedCt = approvedCt;
			this.wact = wact;
			this.actualCt = actualCt;
			this.ctLl1 = ctLl1;
			this.ctLl2 = ctLl2;
			this.ctUl1 = ctUl1;
			this.ctUl2 = ctUl2;
			this.temperature = temperature;
		}
	}
}
