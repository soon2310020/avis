package com.emoldino.api.supplychain.resource.composite.cappln;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetOut;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnGetOut.CapPlnItem;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnPartsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnProductsGetIn;
import com.emoldino.framework.dto.ListOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Supply Chain / Capacity Planning")
@RequestMapping("/api/supplychain/cap-pln")
public interface CapPlnController {

	@ApiOperation("Get Products")
	@GetMapping("/products")
	Page<FltProduct> getProducts(CapPlnProductsGetIn input, Pageable pageable);

	@ApiOperation("Get Parts")
	@GetMapping("/parts")
	Page<FltPart> getParts(CapPlnPartsGetIn input, Pageable pageable);

	@ApiOperation("Get Capacity Planning List")
	@GetMapping
	ListOut<CapPlnItem> get(CapPlnGetIn input, Pageable pageable);

	@ApiOperation("Get Capacity Planning Details")
	@GetMapping("/details")
	CapPlnDetailsGetOut getDetails(CapPlnDetailsGetIn input, Pageable pageable);

}
