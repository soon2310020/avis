package com.emoldino.api.common.resource.composite.usrstp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpData;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpGetPlantsOut;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpRole;
import com.emoldino.api.common.resource.composite.usrstp.service.UsrStpService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class UsrStpControllerImpl implements UsrStpController {

	@Autowired
	private UsrStpService service;

	@Override
	public UsrStpData get(Long id) {
		return service.get(id);
	}

	@Override
	public UsrStpGetPlantsOut getPlants(Long id, Long companyId) {
		return service.getPlants(id, companyId);
	}

	@Override
	public UsrStpGetPlantsOut getPlants(Long companyId) {
		return service.getPlants(null, companyId);
	}

	@Override
	public ListOut<UsrStpRole> getRoles(Long companyId) {
		return service.getRoles(companyId);
	}

	@Override
	public SuccessOut post(UsrStpData data) {
		service.post(data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut put(Long id, UsrStpData data) {
		service.put(id, data);
		return SuccessOut.getDefault();
	}

}
