package com.emoldino.api.common.resource.composite.mdtadj;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.mdtadj.service.MdtAdjService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class MdtAdjControllerImpl implements MdtAdjController {

	@Override
	public SuccessOut post() {
		BeanUtils.get(MdtAdjService.class).post();
		return SuccessOut.getDefault();
	}

}
