package com.emoldino.api.production.resource.composite.alrutm;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmGetIn;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmGetOut;
import com.emoldino.api.production.resource.composite.alrutm.service.AlrUtmService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AlrUtmControllerImpl implements AlrUtmController {

	@Override
	public AlrUtmGetOut get(AlrUtmGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrUtmService.class).get(input, pageable);
	}

	@Override
	public SuccessOut postNoteBatch(AlrUtmGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrUtmService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
