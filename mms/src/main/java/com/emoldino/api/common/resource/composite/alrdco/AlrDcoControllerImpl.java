
package com.emoldino.api.common.resource.composite.alrdco;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoGetIn;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoTerminalsGetOut;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoToolingsGetOut;
import com.emoldino.api.common.resource.composite.alrdco.service.AlrDcoService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AlrDcoControllerImpl implements AlrDcoController {

	@Override
	public AlrDcoTerminalsGetOut getTerminals(AlrDcoGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrDcoService.class).getTerminals(input, pageable);
	}

	@Override
	public AlrDcoToolingsGetOut getToolings(AlrDcoGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrDcoService.class).getToolings(input, pageable);
	}

	@Override
	public SuccessOut postTerminalsNoteBatch(AlrDcoGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrDcoService.class).postTerminalsNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();

	}

	@Override
	public SuccessOut postToolingsNoteBatch(AlrDcoGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrDcoService.class).postToolingsNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
