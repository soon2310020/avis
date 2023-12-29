package com.emoldino.api.analysis.resource.composite.ovrutl.dto;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetOut.OvrUtlDetail;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("serial")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Data
public class OvrUtlDetailsGetOut extends PageImpl<OvrUtlDetail> {
	private FltCompany supplier;
	private FltPart part;

	public OvrUtlDetailsGetOut(List<OvrUtlDetail> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public OvrUtlDetailsGetOut(List<OvrUtlDetail> content, Pageable pageable, long total, FltCompany supplier, FltPart part) {
		super(content, pageable, total);
		this.supplier = supplier;
		this.part = part;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OvrUtlDetail {
		private Long moldId;
		private String moldCode;

		private List<OvrUtlPart> parts;

		private int accumShotCount;
		private int designedShotCount;
		private double utilizationRate;
		@Enumerated(EnumType.STRING)
		private ToolingUtilizationStatus utilizationStatus;

		public OvrUtlDetail(Long moldId, String moldCode, int accumShotCount, int designedShotCount, double utilizationRate) {
			this.moldId = moldId;
			this.moldCode = moldCode;
			this.accumShotCount = accumShotCount;
			this.designedShotCount = designedShotCount;
			this.utilizationRate = utilizationRate;
		}
	}

}
