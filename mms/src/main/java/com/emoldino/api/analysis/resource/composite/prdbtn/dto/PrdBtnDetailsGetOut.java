package com.emoldino.api.analysis.resource.composite.prdbtn.dto;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetOut.PrdBtnDetails;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.EquipmentStatus;

@SuppressWarnings("serial")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PrdBtnDetailsGetOut extends PageImpl<PrdBtnDetails> {
	private List<FltSupplier> suppliers;
	private FltProduct product;
	private FltPart part;

	public PrdBtnDetailsGetOut(List<PrdBtnDetails> content, Pageable pageable, long total, List<FltSupplier> suppliers, FltProduct product, FltPart part) {
		super(content, pageable, total);
		this.suppliers = suppliers;
		this.product = product;
		this.part = part;
	}

	public PrdBtnDetailsGetOut(List<PrdBtnDetails> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class PrdBtnDetails {
		private Long moldId;
		private String moldCode;
		@Enumerated(EnumType.STRING)
		private EquipmentStatus moldStatus;

		private Long supplierId;
		private String supplierName;
		private String supplierCode;

		private Double utilizationRate;
		private Double approvedCycleTime;
		private Double averageCycleTime;
		private Long producedQuantity;
		private long predictedQuantity;
		private Long productionCapacity;
	}
}
