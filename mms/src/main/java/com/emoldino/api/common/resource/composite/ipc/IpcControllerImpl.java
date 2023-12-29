package com.emoldino.api.common.resource.composite.ipc;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersion;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersionsIn;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersionsPullIn;
import com.emoldino.api.common.resource.composite.ipc.service.IpcService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class IpcControllerImpl implements IpcController {

	@Override
	public Map<String, Object> getAppVersionsLatest(String appCode) {
		return BeanUtils.get(IpcService.class).getAppVersionsLatest(appCode);
	}

	@Override
	public Page<IpcAppVersion> getAppVersions(IpcAppVersionsIn input, Pageable pageable) {
		return BeanUtils.get(IpcService.class).getAppVersions(input, pageable);
	}

	@Override
	public SuccessOut pullAppVersions(IpcAppVersionsPullIn input) {
		BeanUtils.get(IpcService.class).pullAppVersions(input);
		return SuccessOut.getDefault();
	}

}
