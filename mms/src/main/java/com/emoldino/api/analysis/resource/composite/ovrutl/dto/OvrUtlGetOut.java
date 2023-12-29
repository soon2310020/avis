package com.emoldino.api.analysis.resource.composite.ovrutl.dto;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetOut.OvrUtlItem;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
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
public class OvrUtlGetOut extends PageImpl<OvrUtlItem> {

	private UtilizationConfig config;

	public OvrUtlGetOut(List<OvrUtlItem> content, Pageable pageable, long total, UtilizationConfig config) {
		super(content, pageable, total);
		this.config = config;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OvrUtlItem {
		private Long supplierId;
		private String supplierName;
		private String supplierCode;

		@JsonIgnore
		private String firstPlantName;
		private List<FltPlant> plants;

		private long totalMoldCount;
		private long lowMoldCount;
		private long mediumMoldCount;
		private long highMoldCount;
		private long prolongedMoldCount;

		public OvrUtlItem(Long supplierId, String supplierName, String supplierCode, String firstPlantName, long totalMoldCount, long lowMoldCount, long mediumMoldCount,
				long highMoldCount, long prolongedMoldCount) {
			this.supplierId = supplierId;
			this.supplierName = supplierName;
			this.supplierCode = supplierCode;
			this.firstPlantName = firstPlantName;
			this.totalMoldCount = totalMoldCount;
			this.lowMoldCount = lowMoldCount;
			this.mediumMoldCount = mediumMoldCount;
			this.highMoldCount = highMoldCount;
			this.prolongedMoldCount = prolongedMoldCount;
		}
	}

}
