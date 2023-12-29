package com.emoldino.api.common.resource.composite.parstp.dto;

import java.time.Instant;
import java.util.List;

import com.emoldino.api.common.resource.base.object.dto.FieldValue;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.common.util.StringUtils;
import saleson.model.Part;

@Data
@NoArgsConstructor
public class ParStpItem {
	private Long id;
	private String name;
	private String partCode;

	private boolean enabled;

	private Long productId;
	private String productName;
	private String categoryName;

	private Long quantityRequired;
	private Integer weeklyDemand;
	private Long producedQuantity;

	private Integer moldCount;
	private Integer activeMoldCount;
	private Integer idleMoldCount;
	private Integer inactiveMoldCount;
	private Integer disconMoldCount;
	private Integer machineCount;

	private String resinCode;
	private String resinGrade;
	private String designRevision;
	private String size;
	private SizeUnit sizeUnit;
	private String weight;
	private WeightUnit weightUnit;

	private String partSize;
	private String partWeight;

	private String creationDate;

	private List<FieldValue> customFields;

	public ParStpItem(Part part, Integer machineCount) {
		ValueUtils.map(part, this);
		this.productId = part.getCategoryId();
		this.productName = part.getProjectName();
		this.machineCount = machineCount;
		this.producedQuantity = ValueUtils.toLong(part.getTotalProduced(), 0L);
	}

	@QueryProjection
	public ParStpItem(Long id, String name, String partCode, //
			boolean enabled, //
			Long productId, String productName, String categoryName, //
			Long quantityRequired, Integer weeklyDemand, Long producedQuantity, //
			Integer moldCount, Integer activeMoldCount, Integer idleMoldCount, Integer inactiveMoldCount, Integer disconMoldCount, Integer machineCount, //
			String resinCode, String resinGrade, String designRevision, //
			String size, SizeUnit sizeUnit, String weight, WeightUnit weightUnit, //
			Instant createdAt) {

		this.id = id;
		this.name = name;
		this.partCode = partCode;
		this.enabled = enabled;
		this.productId = productId;
		this.productName = productName;
		this.categoryName = categoryName;

		this.quantityRequired = quantityRequired;
		this.weeklyDemand = weeklyDemand;
		this.producedQuantity = producedQuantity;

		this.moldCount = moldCount;
		this.activeMoldCount = activeMoldCount;
		this.idleMoldCount = idleMoldCount;
		this.inactiveMoldCount = inactiveMoldCount;
		this.disconMoldCount = disconMoldCount;
		this.machineCount = machineCount;
		this.resinCode = resinCode;
		this.resinGrade = resinGrade;
		this.designRevision = designRevision;
		this.size = size;
		this.sizeUnit = sizeUnit;
		this.weight = weight;
		this.weightUnit = weightUnit;

		// TODO by user timezone
		this.creationDate = DateUtils2.format(createdAt, DatePattern.yyyy_MM_dd, Zone.GMT);

		this.partSize = formatPartSize(size, sizeUnit);
		this.partWeight = formatPartWeight(weight, weightUnit);
	}

	public String getSizeValue() {
		return formatPartSize(this.size);
	}

	private static String formatPartSize(String size, SizeUnit sizeUnit) {
		String sizeValue = formatPartSize(size);
		if (StringUtils.isEmpty(sizeValue)) {
			return sizeValue;
		}
		String unit = sizeUnit != null ? sizeUnit.getTitle() : "";
		if (sizeUnit != null) {
			if (sizeUnit.equals(SizeUnit.MM) || sizeUnit.equals(SizeUnit.CM) || sizeUnit.equals(SizeUnit.M)) {
				unit += "³";
			}
		}
		return sizeValue + " " + unit;
	}

	private static String formatPartSize(String size) {
		if (size == null || size.equalsIgnoreCase("") || size.equalsIgnoreCase("undefinedxundefinedxundefined") || size.equalsIgnoreCase("xx")) {
			return "";
		}
		size = size.replaceAll("undefined", "0");
		if (size.startsWith("x")) {
			size = "0" + size;
		}
		size = size.replaceAll("x\\s*x", "x 0 x");
		if (size.endsWith("x")) {
			size = size + "0";
		}
		size = size.replaceAll("x", " x ").replaceAll("\\s+", " ").trim();

		return size;
	}

	// partWeight
	private static String formatPartWeight(String weight, WeightUnit weightUnit) {
		if (weight == null || weight.equalsIgnoreCase("")) {
			return "";
		}
		return weight + " " + (weightUnit != null ? weightUnit.getTitle() : "");
	}

}
