package com.emoldino.api.common.resource.composite.alrdatapr;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.alrdatapr.dto.AlrDatAprGetIn;
import com.emoldino.api.common.resource.composite.alrdatapr.dto.AlrDatAprGetOut;
import com.emoldino.api.common.resource.composite.alrdatapr.service.AlrDatAprService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AlrDatAprControllerImpl implements AlrDatAprController {

	@Override
	public AlrDatAprGetOut get(AlrDatAprGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrDatAprService.class).get(input, pageable);
	}

	@Override
	public SuccessOut postNoteBatch(AlrDatAprGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrDatAprService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
