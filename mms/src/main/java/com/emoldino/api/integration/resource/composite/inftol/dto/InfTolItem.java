package com.emoldino.api.integration.resource.composite.inftol.dto;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.model.Mold;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfTolItem {
	private String toolingId;
	private String toolingType;
	private String toolDescription;

	private String updateDateTime;

	private Long toolSizeWidth;
	private Long toolSizeLength;
	private Long toolSizeHeight;
	private SizeUnit toolSizeUnit;

	private Long toolWeight;
	private WeightUnit toolWeightUnit;

	private Long warrantyShotCount;

	public InfTolItem(Mold mold) {
		this.toolingId = mold.getEquipmentCode();
		this.toolingType = mold.getToolingType();
		this.toolDescription = mold.getToolDescription();

		this.updateDateTime = DateUtils2.format(mold.getUpdatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);

		if (!ObjectUtils.isEmpty(mold.getSize())) {
			String[] size = StringUtils.tokenizeToStringArray(mold.getSize(), "x");
			if (size.length == 3) {
				this.toolSizeWidth = toLong(size[0]);
				this.toolSizeLength = toLong(size[1]);
				this.toolSizeHeight = toLong(size[2]);
			}
		}
		this.toolSizeUnit = mold.getSizeUnit();

		this.toolWeight = toLong(mold.getWeight());
		this.toolWeightUnit = mold.getWeightUnit();

		this.warrantyShotCount = ValueUtils.toLong(mold.getDesignedShot(), 0L);
	}

	private static Long toLong(String value) {
		return ValueUtils.isNumber(value) ? ValueUtils.toLong(value, 0L) : null;
	}
}
