package com.emoldino.api.common.resource.composite.appverstp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.version.dto.AppVersionGetPageIn;
import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersion;
import com.emoldino.api.common.resource.base.version.service.app.AppVersionService;
import com.emoldino.api.common.resource.composite.appverstp.dto.AppVerStpGetPageIn;

@Service
public class AppVerStpService {
	@Autowired
	private AppVersionService service;

	public Page<AppVersion> get(AppVerStpGetPageIn input, Pageable pageable) {
		AppVersionGetPageIn reqin = new AppVersionGetPageIn();
		reqin.setAppCode("MMS");
		reqin.setEnabled(input.getEnabled());
		return service.get(reqin, pageable);
	}

	public void post(AppVersion data) {
		service.post(data);
	}

	public void put(Long id, AppVersion data) {
		service.put(id, data);
	}

	public void disable(List<Long> id) {
		service.disable(id);
	}

	public void enable(List<Long> id) {
		service.enable(id);
	}

}
