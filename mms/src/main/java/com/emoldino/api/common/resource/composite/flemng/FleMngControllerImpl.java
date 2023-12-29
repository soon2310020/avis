package com.emoldino.api.common.resource.composite.flemng;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.flemng.dto.FleMngGetIn;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngGroup;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngPostOut;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngPutVersionIn;
import com.emoldino.api.common.resource.composite.flemng.service.FleMngService;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class FleMngControllerImpl implements FleMngController {
	@Autowired
	private FleMngService service;

	@Override
	public Page<FleMngGroup> get(FleMngGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public FleMngGroup get(Long id) {
		return service.get(id);
	}

	@Override
	public FleMngPostOut post(FleMngGroup data) {
		return new FleMngPostOut(service.post(data));
	}

	@Override
	public SuccessOut put(Long id, FleMngGroup data) {
		service.put(id, data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut delete(Long id) {
		service.delete(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disable(List<Long> id) {
		service.disable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enable(List<Long> id) {
		service.enable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut release(Long id, FleMngGroup data) {
		service.release(id, data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut unrelease(Long id, FleMngGroup data) {
		service.unrelease(id, data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut putVersion(Long id, FleMngPutVersionIn data) {
		service.putVersion(id, data);
		return SuccessOut.getDefault();
	}

	@Override
	public List<FleMngGroup> getVersions(Long id) {
		return service.getVersions(id);
	}

}
