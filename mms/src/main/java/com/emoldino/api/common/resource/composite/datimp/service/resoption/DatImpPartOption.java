package com.emoldino.api.common.resource.composite.datimp.service.resoption;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOption;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOptionGetter;
import com.emoldino.api.common.resource.composite.datimp.util.DatImpUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.api.category.CategoryRepository;
import saleson.api.part.PartController;
import saleson.api.part.PartRepository;
import saleson.api.part.PartService;
import saleson.api.part.payload.PartPayload;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.model.Category;
import saleson.model.Part;
import saleson.model.QPart;

@Getter
@NoArgsConstructor
public class DatImpPartOption implements ResOptionGetter<PartPayload> {
	private DatImpResourceType resourceType = DatImpResourceType.PART;
	private ResOption<PartPayload> resOption = new ResOption<PartPayload>(//
			// 1. Sheet Name
			Arrays.asList("Part"), //
			// 2. Object Type
			ObjectType.PART, //
			// 3. Item Class
			PartPayload.class, //
			// 4. Code Field
			Arrays.asList("partCode"),
			// 5. Populate
			item -> populate(item),
			// 6. Before Logic
			item -> doBefore(item), //
			// 7. Exists Logic
			item -> exists(item), //
			// 8. Post Logic
			item -> post(item), //
			// 9. Put Logic
			item -> put(item)//
	);

	private void populate(PartPayload item) {
		if (ObjectUtils.isEmpty(item.getPartCode())) {
			return;
		}
		Part part = BeanUtils.get(PartService.class).findByPartCode(item.getPartCode());
		if (part == null) {
			return;
		}
		ValueUtils.map(part, item);
	}

	private void doBefore(PartPayload item) {
		// Product Id
		if (!ObjectUtils.isEmpty(item.getProductName())) {
			Category product = BeanUtils.get(CategoryRepository.class).findFirstByName(item.getProductName())//
					.orElseThrow(() -> DataUtils.newDataNotFoundException("product", DatImpUtils.toXlsColumnTitle("productName"), item.getProductName()));
			item.setCategoryId(product.getId());
		}

		// Size Unit
		if (StringUtils.isNotBlank(item.getSizeUnitTitle())) {
			@SuppressWarnings("serial")
			Map<String, String> titleMap = new HashMap<String, String>() {
				{
					put("mm3", "mm");
					put("cm3", "cm");
					put("m3", "m");
				}
			};

			String title;
			if (titleMap.containsKey(item.getSizeUnitTitle())) {
				title = titleMap.get(item.getSizeUnitTitle());
			} else {
				title = item.getSizeUnitTitle();
			}

			SizeUnit enumItem = Arrays.stream(SizeUnit.values())//
					.filter(t -> t.getTitle().equalsIgnoreCase(title))//
					.findFirst()//
					.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "volume_unit", title));
			item.setSizeUnit(enumItem);
		}

		// Weight Unit
		if (StringUtils.isNotBlank(item.getWeightUnitTitle())) {
			WeightUnit enumItem = Arrays.stream(WeightUnit.values())//
					.filter(t -> t.getTitle().equalsIgnoreCase(item.getWeightUnitTitle()))//
					.findFirst()//
					.orElseThrow(() -> DataUtils.newDataValueInvalidException("tooling", "weight_unit", item.getWeightUnitTitle()));
			item.setWeightUnit(enumItem);
		}
	}

	private boolean exists(PartPayload item) {
		return BeanUtils.get(PartRepository.class).exists(new BooleanBuilder().and(QPart.part.partCode.eq(item.getPartCode())));
	}

	private void post(PartPayload item) {
		item.setEnabled(true);
		ResponseEntity<?> response = BeanUtils.get(PartController.class).newPart(item);
		if (response.getBody() instanceof Part && response.getBody() != null) {
			item.setId(((Part) response.getBody()).getId());
		}
		DatImpUtils.response(response);
	}

	private void put(PartPayload item) {
		Part part = BeanUtils.get(PartRepository.class)//
				.findOne(new BooleanBuilder().and(QPart.part.partCode.eq(item.getPartCode())))//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Part.class, DatImpUtils.toXlsColumnTitle("partCode"), item.getPartCode()));
		item.setEnabled(part.isEnabled());
		ResponseEntity<?> response = BeanUtils.get(PartController.class).editPart(part.getId(), item);
		DatImpUtils.response(response);
	}
}
