package com.emoldino.api.common.resource.composite.notbel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelGetIn;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelGetOut;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelPostEmailIn;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelPostOneIn;
import com.emoldino.api.common.resource.composite.notbel.service.NotBelService;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class NotBelControllerImpl implements NotBelController {

	@Autowired
	private NotBelService service;

	@Override
	public NotBelGetOut get(NotBelGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public SuccessOut postOne(NotBelPostOneIn input) {
		service.postOne(input);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postDummy(Long id, String code, String email) {
		service.postDummy(id, code, email);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postEmail(String title, String to, String content) {
		service.postEmail(NotBelPostEmailIn.builder()//
				.title(title)//
				.to(to)//
				.content(content)//
				.build());
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut turnOnOff(Boolean on) {
		service.turnOnOff(on);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut read(Long id) {
		service.read(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut read(NotiCategory notiCategory) {
		service.read(notiCategory);
		return SuccessOut.getDefault();
	}

}
