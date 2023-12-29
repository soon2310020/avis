package com.emoldino.api.supplychain.resource.base.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Company;
import saleson.model.Part;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartSupplier {
	private Long productId;

	private Long partId;
	private String partName;
	private String partCode;

	private Long supplierId;
	private String supplierName;
	private String supplierCode;

	public PartSupplier(Part part, Company supplier) {
		productId = part.getCategoryId();
		partId = part.getId();
		partName = part.getName();
		partCode = part.getPartCode();
		supplierId = supplier.getId();
		supplierName = supplier.getName();
		supplierCode = supplier.getCompanyCode();
	}
}
