package com.emoldino.api.common.resource.composite.flt.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;

import saleson.api.category.CategoryRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.part.PartRepository;
import saleson.model.Category;
import saleson.model.Company;
import saleson.model.Part;

public class FltUtils {

	public static FltProduct getProductById(Long id) {
		if (id == null) {
			return null;
		}
		Category part = BeanUtils.get(CategoryRepository.class)//
				.findById(id)//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Category.class, "id", id));
		FltProduct fltProduct = new FltProduct(part);
		return fltProduct;
	}

	public static FltPart getPartById(Long id) {
		if (id == null) {
			return null;
		}
		Part part = BeanUtils.get(PartRepository.class)//
				.findById(id)//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Part.class, "id", id));
		FltPart fltPart = new FltPart(part);
		return fltPart;
	}

	public static FltSupplier getSupplierById(Long id) {
		if (id == null) {
			return null;
		}
		Company supplier = BeanUtils.get(CompanyRepository.class)//
				.findById(id)//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Company.class, "id", id));
		FltSupplier fltSupplier = new FltSupplier(supplier);
		return fltSupplier;
	}

	public static List<FltSupplier> getSuppliersByIds(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return null;
		}
		List<FltSupplier> list = ids.stream().map(id -> getSupplierById(id)).collect(Collectors.toList());
		return list;
	}

}
