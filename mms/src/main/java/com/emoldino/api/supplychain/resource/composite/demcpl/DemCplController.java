package com.emoldino.api.supplychain.resource.composite.demcpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.supplychain.resource.base.product.dto.PartPlanYearly;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetOut;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplGetOut;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplPart;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplPartDemandsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplPartsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProduct;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProductDemand;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProductDemandsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProductsGetIn;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.dto.TimeSetting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Supply Chain / Demand Compliance")
@RequestMapping("/api/supplychain/dem-cpl")
public interface DemCplController {

	@ApiOperation("Get Products")
	@GetMapping("/products")
	Page<DemCplProduct> getProducts(DemCplProductsGetIn input, Pageable pageable);

	@ApiOperation("Get Parts")
	@GetMapping("/parts")
	Page<DemCplPart> getParts(DemCplPartsGetIn input, Pageable pageable);

	@ApiOperation("Get Demand Compliance")
	@GetMapping
	DemCplGetOut get(DemCplGetIn input, Pageable pageable);

	@ApiOperation("Get Demand Compliance Details")
	@GetMapping("/details")
	DemCplDetailsGetOut getDetails(DemCplDetailsGetIn input, Pageable pageable);

	@ApiOperation("Get Product Demands")
	@GetMapping("/product-demands")
	Page<DemCplProductDemand> getProductDemands(DemCplProductDemandsGetIn input, Pageable pageable);

	@ApiOperation("Input Product Demand table")
	@PostMapping("/product-demands")
	SuccessOut postProductDemands(@RequestBody ListIn<DemCplProductDemand> input, TimeSetting timeSetting);

	@ApiOperation("Get Part Demands")
	@GetMapping("/part-demands")
	Page<PartPlanYearly> getPartDemands(DemCplPartDemandsGetIn input, Pageable pageable);

	@ApiOperation("Input Part Demand table")
	@PostMapping("/part-demands")
	SuccessOut postPartDemands(@RequestBody ListIn<PartPlanYearly> input, TimeSetting timeSetting);

}
