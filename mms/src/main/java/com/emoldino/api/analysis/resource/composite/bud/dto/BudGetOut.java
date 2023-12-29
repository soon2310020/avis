package com.emoldino.api.analysis.resource.composite.bud.dto;

import java.util.List;
import java.util.Map;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetOut.BudSupplierItem;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import saleson.common.enumeration.CurrencyType;

@SuppressWarnings("serial")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BudGetOut extends PageImpl<BudSupplierItem> {

	Map<String, BudGraphItem> graphContent; // fh, sh, fh1, sh1...

	public BudGetOut(List<BudSupplierItem> content, Pageable pageable, long total, Map<String, BudGraphItem> graphContent) {
		super(content, pageable, total);
		this.graphContent = graphContent;
	}

	@Builder
	@Data
	public static class BudSupplierItem {
		private Long supplierId;
		private String supplierCode;
		private Long moldCount;
		private Long totalCost;
		private Long totalSalvage;
		@Enumerated(EnumType.STRING)
		private CurrencyType costType;
	}

	@Builder
	@Data
	public static class BudGraphItem {
		private List<BudSupplierItem> supplierItems;
		private int totalMoldCount;
		private String year;
		private String half; // 1: 1st half , 2: 2nd half
	}

}
