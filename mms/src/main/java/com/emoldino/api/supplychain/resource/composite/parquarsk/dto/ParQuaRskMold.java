package com.emoldino.api.supplychain.resource.composite.parquarsk.dto;

import java.math.BigDecimal;

import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParQuaRskMold {
	@JsonIgnore
	private String partName;

	private Long moldId;
	private String moldCode;

	@JsonIgnore
	private Long supplierId;
	@JsonIgnore
	private String supplierName;

	@JsonIgnore
	private Long locationId;
	@JsonIgnore
	private String locationName;

	@JsonIgnore
	private Integer prodQty;
	@JsonIgnore
	private BigDecimal estimYieldRate;

	public ParQuaRskMold(//
			String partName, //
			Long moldId, String moldCode, //
			Long supplierId, String supplierName, //
			Long locationId, String locationName, //
			Integer prodQty, Float estimYieldRate//
	) {
		this(partName, moldId, moldCode, supplierId, supplierName, locationId, locationName, prodQty, ValueUtils.toBigDecimal(ValueUtils.toDouble(estimYieldRate, 0d), 1, 0d));
	}

//	public Long getId() {
//		return moldId;
//	}
//
//	public String getEquipmentCode() {
//		return moldCode;
//	}
//
//	public String getName() {
//		return moldCode;
//	}
//
//	public String getCode() {
//		return moldCode;
//	}
}
