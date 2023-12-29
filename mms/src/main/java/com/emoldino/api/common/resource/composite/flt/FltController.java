package com.emoldino.api.common.resource.composite.flt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Filters")
@RequestMapping("/api/common/flt")
public interface FltController {

	@GetMapping("/companies")
	Page<FltCompany> getCompanies(FltCompanyIn input, Pageable pageable);

	@GetMapping("/users")
	Page<FltUser> getUsers(FltIn input, Pageable pageable);

	@GetMapping("/products")
	Page<FltProduct> getProducts(FltIn input, Pageable pageable);

	@GetMapping("/parts")
	Page<FltPart> getParts(FltIn input, Pageable pageable);

	@GetMapping("/suppliers")
	Page<FltCompany> getSuppliers(FltIn input, Pageable pageable);

	@GetMapping("/toolmakers")
	Page<FltCompany> getToolmakers(FltIn input, Pageable pageable);

	@GetMapping("/plants")
	Page<FltPlant> getPlants(FltIn input, Pageable pageable);

	@GetMapping("/molds")
	Page<FltMold> getMolds(FltIn input, Pageable pageable);

	@GetMapping("/machines")
	Page<FltMachine> getMachines(FltIn input, Pageable pageable);

	@ApiOperation("Get Master Filters")
	@GetMapping("/{filterCode}")
	ListOut<FltResource> get(@PathVariable("filterCode") String filterCode);

	@ApiOperation("Save Master Filters by ResourceType")
	@PostMapping("/{filterCode}/{resourceType}")
	SuccessOut post(//
			@PathVariable("filterCode") String filterCode, //
			@PathVariable("resourceType") MasterFilterResourceType resourceType, //
			@RequestBody FltResource input//
	);

	@ApiOperation("Delete Master Filters")
	@DeleteMapping("/{filterCode}")
	SuccessOut delete(@PathVariable("filterCode") String filterCode);

	@ApiOperation("Delete Master Filters by ResourceType")
	@DeleteMapping("/{filterCode}/{resourceType}")
	SuccessOut delete(@PathVariable("filterCode") String filterCode, @PathVariable("resourceType") MasterFilterResourceType resourceType);

	@ApiOperation("Get ResourceTypes of Master Filter")
	@GetMapping("/{filterCode}/resource-types")
	ListOut<FltResourceType> getResourceTypes(@PathVariable("filterCode") String filterCode);

}
