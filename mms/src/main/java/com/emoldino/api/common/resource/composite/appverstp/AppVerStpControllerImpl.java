package com.emoldino.api.common.resource.composite.appverstp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersion;
import com.emoldino.api.common.resource.composite.appverstp.dto.AppVerStpGetPageIn;
import com.emoldino.api.common.resource.composite.appverstp.service.AppVerStpService;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class AppVerStpControllerImpl implements AppVerStpController {
	@Autowired
	private AppVerStpService service;

	@Override
	public Page<AppVersion> get(AppVerStpGetPageIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public SuccessOut post(AppVersion data) {
		service.post(data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut put(Long id, AppVersion data) {
		service.put(id, data);
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

}
