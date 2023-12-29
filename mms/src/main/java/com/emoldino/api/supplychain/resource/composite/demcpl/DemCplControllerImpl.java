package com.emoldino.api.supplychain.resource.composite.demcpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

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
import com.emoldino.api.supplychain.resource.composite.demcpl.service.DemCplService;
import com.emoldino.api.supplychain.resource.composite.demcpl.service.demand.DemCplDemandService;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.dto.TimeSetting;

@RestController
public class DemCplControllerImpl implements DemCplController {
	@Autowired
	private DemCplService service;
	@Autowired
	private DemCplDemandService demandService;

	@Override
	public Page<DemCplProduct> getProducts(DemCplProductsGetIn input, Pageable pageable) {
		return service.getProducts(input, pageable);
	}

	@Override
	public Page<DemCplPart> getParts(DemCplPartsGetIn input, Pageable pageable) {
		return service.getParts(input, pageable);
	}

	@Override
	public DemCplGetOut get(DemCplGetIn input, Pageable pageable) {
		return service.get(input, pageable.getSort());
	}

	@Override
	public DemCplDetailsGetOut getDetails(DemCplDetailsGetIn input, Pageable pageable) {
		return service.getDetails(input, pageable.getSort());
	}

	@Override
	public Page<DemCplProductDemand> getProductDemands(DemCplProductDemandsGetIn input, Pageable pageable) {
		return demandService.getProductDemands(input, pageable);
	}

	@Override
	public SuccessOut postProductDemands(ListIn<DemCplProductDemand> input, TimeSetting timeSetting) {
		demandService.postProductDemands(input, timeSetting);
		return SuccessOut.getDefault();
	}

	@Override
	public Page<PartPlanYearly> getPartDemands(DemCplPartDemandsGetIn input, Pageable pageable) {
		return demandService.getPartDemands(input, pageable);
	}

	@Override
	public SuccessOut postPartDemands(ListIn<PartPlanYearly> input, TimeSetting timeSetting) {
		demandService.postPartDemands(input, timeSetting);
		return SuccessOut.getDefault();
	}
}
