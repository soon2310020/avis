package com.emoldino.api.common.resource.composite.nothis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.noti.dto.NotiUserItem;
import com.emoldino.api.common.resource.composite.nothis.dto.NotHisGetIn;
import com.emoldino.api.common.resource.composite.nothis.service.NotHisService;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class NotHisControllerImpl implements NotHisController {

	@Autowired
	private NotHisService service;

	@Override
	public Page<NotiUserItem> get(NotHisGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public SuccessOut read(Long id) {
		service.read(id);
		return SuccessOut.getDefault();
	}

}
