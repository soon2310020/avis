package com.emoldino.api.common.resource.composite.flt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompanyIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltMachine;
import com.emoldino.api.common.resource.composite.flt.dto.FltMold;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.common.resource.composite.flt.dto.FltResource;
import com.emoldino.api.common.resource.composite.flt.dto.FltResourceType;
import com.emoldino.api.common.resource.composite.flt.dto.FltUser;
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class FltControllerImpl implements FltController {
	@Autowired
	private FltService service;

	@Override
	public Page<FltCompany> getCompanies(FltCompanyIn input, Pageable pageable) {
		return service.getCompanies(input, pageable);
	}

	@Override
	public Page<FltUser> getUsers(FltIn input, Pageable pageable) {
		return service.getUsers(input, pageable);
	}

	@Override
	public Page<FltProduct> getProducts(FltIn input, Pageable pageable) {
		return service.getProducts(input, pageable);
	}

	@Override
	public Page<FltPart> getParts(FltIn input, Pageable pageable) {
		return service.getParts(input, pageable);
	}

	@Override
	public Page<FltCompany> getSuppliers(FltIn input, Pageable pageable) {
		return service.getSuppliers(input, pageable);
	}

	@Override
	public Page<FltCompany> getToolmakers(FltIn input, Pageable pageable) {
		return service.getToolmakers(input, pageable);
	}

	@Override
	public Page<FltPlant> getPlants(FltIn input, Pageable pageable) {
		return service.getPlants(input, pageable);
	}

	@Override
	public Page<FltMold> getMolds(FltIn input, Pageable pageable) {
		return service.getMolds(input, pageable);
	}

	@Override
	public Page<FltMachine> getMachines(FltIn input, Pageable pageable) {
		return service.getMachines(input, pageable);
	}

	@Override
	public ListOut<FltResource> get(String filterCode) {
		return service.get("COMMON");
	}

	@Override
	public SuccessOut post(String filterCode, MasterFilterResourceType resourceType, FltResource input) {
		input.setResourceType(resourceType);
		service.post("COMMON", input);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut delete(String filterCode) {
		service.delete("COMMON", null);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut delete(String filterCode, MasterFilterResourceType resourceType) {
		service.delete("COMMON", resourceType);
		return SuccessOut.getDefault();
	}

	@Override
	public ListOut<FltResourceType> getResourceTypes(String filterCode) {
		return service.getResourceTypes("COMMON");
	}

}
