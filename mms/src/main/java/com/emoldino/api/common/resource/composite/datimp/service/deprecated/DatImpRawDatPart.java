package com.emoldino.api.common.resource.composite.datimp.service.deprecated;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Deprecated
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DatImpRawDatPart extends DatImpRawDatBase {
	private String productName;
	private String partCode;
	private String name;
	private String resinCode;
	private String resinGrade;
	private String size;
	private String sizeUnit;
	private String weight;
	private String weightUnit;
	private String weeklyDemand;
	private String quantityRequired;

	public DatImpRawDatPart(int rowIndex) {
		this.rowIndex = rowIndex;
	}
}
