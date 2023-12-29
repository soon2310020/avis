package com.emoldino.api.common.resource.composite.datimp.service.resoption;

import java.util.Arrays;

import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOption;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOptionGetter;
import com.emoldino.api.common.resource.composite.datimp.util.DatImpUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.api.category.CategoryRepository;
import saleson.api.category.CategoryService;
import saleson.api.category.payload.CategoryRequest;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.ObjectType;
import saleson.model.Category;

@Getter
@NoArgsConstructor
public class DatImpCategoryOption implements ResOptionGetter<CategoryRequest> {
	private DatImpResourceType resourceType = DatImpResourceType.CATEGORY;
	private ResOption<CategoryRequest> resOption = new ResOption<CategoryRequest>(//
			// 1. Sheet Name
			Arrays.asList("Category"), //
			// 2. Object Type
			ObjectType.CATEGORY, //
			// 3. Item Class
			CategoryRequest.class, //
			// 4. Code Field
			null,
			// 5. Populate
			null,
			// 6. Before Logic
			null, //
			// 7. Exists Logic
			item -> BeanUtils.get(CategoryService.class).existsName(item.getName(), null), //
			// 8. Post Logic
			item -> {
				item.setEnabled(true);
				BeanUtils.get(CategoryService.class).save(item.getModel());
			}, //
			// 9. Put Logic
			item -> {
				Category c = BeanUtils.get(CategoryRepository.class).findFirstByName(item.getName())//
						.orElseThrow(() -> DataUtils.newDataNotFoundException(Category.class, DatImpUtils.toXlsColumnTitle("name"), item.getName()));
				item.setEnabled(c.isEnabled());

				Category clonedCategory = new Category();
				org.springframework.beans.BeanUtils.copyProperties(item, clonedCategory);
				Category updated = item.getModelFull();
				clonedCategory.setChildren(null);
				saleson.common.util.DataUtils.mapCreateAndUpdateInfo(updated, clonedCategory);
				boolean isIdentical = true;
				try {
					isIdentical = saleson.common.util.DataUtils.deepCompare(clonedCategory, updated);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

				BeanUtils.get(CategoryService.class).save(item.getModel(c), item);

				Category categoryFinal = BeanUtils.get(CategoryService.class).findById(c.getId());
				if (!isIdentical) {
					BeanUtils.get(VersioningService.class).writeHistory(categoryFinal);
				}
			});
}
