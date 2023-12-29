package com.emoldino.api.supplychain.resource.composite.cappln;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetOut;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetOut.CapPlnItem;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnPartsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnProductsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.service.CapPlnService;
import com.emoldino.framework.dto.ListOut;

@RestController
public class CapPlnControllerImpl implements CapPlnController {
	@Autowired
	private CapPlnService service;

	@Override
	public Page<FltProduct> getProducts(CapPlnProductsGetIn input, Pageable pageable) {
		return service.getProducts(input, pageable);
	}

	@Override
	public Page<FltPart> getParts(CapPlnPartsGetIn input, Pageable pageable) {
		return service.getParts(input, pageable);
	}

	@Override
	public ListOut<CapPlnItem> get(CapPlnGetIn input, Pageable pageable) {
		return service.get(input, pageable.getSort());
	}

	@Override
	public CapPlnDetailsGetOut getDetails(CapPlnDetailsGetIn input, Pageable pageable) {
		return service.getDetails(input, pageable.getSort());
	}

}
