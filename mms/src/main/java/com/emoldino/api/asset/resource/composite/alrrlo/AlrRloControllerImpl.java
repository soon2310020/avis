package com.emoldino.api.asset.resource.composite.alrrlo;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloGetIn;
import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloGetOut;
import com.emoldino.api.asset.resource.composite.alrrlo.service.AlrRloService;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AlrRloControllerImpl implements AlrRloController {
	@Override
	public AlrRloGetOut get(AlrRloGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrRloService.class).get(input, pageable);
	}

	@Override
	public SuccessOut postNoteBatch(AlrRloGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrRloService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
