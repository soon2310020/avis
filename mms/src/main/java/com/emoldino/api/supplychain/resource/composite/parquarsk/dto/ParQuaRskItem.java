package com.emoldino.api.supplychain.resource.composite.parquarsk.dto;

import java.math.BigDecimal;

import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParQuaRskItem {
	private Long partId;
	private String partName;
	private String partCode;

	private Long moldId;
	private String moldCode;

	private Long supplierId;
	private String supplierName;
	private String supplierCode;

	private Long locationId;
	private String locationName;
	private String locationCode;

	private Integer prodQty;
	private Integer goodProdQty;

	private BigDecimal estimYieldRate;

	private BigDecimal trend;

	public ParQuaRskItem(//
			Long partId, String partName, String partCode, //
			Long moldId, String moldCode, //
			Long supplierId, String supplierName, String supplierCode, //
			Long locationId, String locationName, String locationCode, //
			Integer prodQty, Integer goodProdQty, Float estimYieldRate//
	) {
		this(partId, partName, partCode, moldId, moldCode, supplierId, supplierName, supplierCode, locationId, locationName, locationCode, prodQty, goodProdQty,
				ValueUtils.toBigDecimal(estimYieldRate == null ? 0d : estimYieldRate.doubleValue(), 1, 0d), null);
	}

	public Integer getProdQty() {
		return ValueUtils.toInteger(prodQty, 0);
	}

	public Integer getGoodProdQty() {
		return ValueUtils.toInteger(goodProdQty, 0);
	}
}
