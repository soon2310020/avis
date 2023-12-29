package com.emoldino.api.supplychain.resource.base.product.dto;

import lombok.Data;

@Data
public class ProductSupplierGetIn {
	private boolean producibleOnly;
	private String date;

	public String getFilterCode() {
		return "COMMON";
	}
}
