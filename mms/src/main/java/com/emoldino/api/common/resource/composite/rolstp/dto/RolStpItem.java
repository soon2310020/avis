package com.emoldino.api.common.resource.composite.rolstp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RolStpItem {
	private Long id;
	private String authority;
	private String name;
	private String description;
	private Boolean emoldinoEnabled = false;
	private Boolean oemEnabled = false;
	private Boolean supplierEnabled = false;
	private Boolean toolmakerEnabled = false;
}
