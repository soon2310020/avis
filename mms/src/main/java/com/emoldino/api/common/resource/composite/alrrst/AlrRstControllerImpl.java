
package com.emoldino.api.common.resource.composite.alrrst;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstGetIn;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstGetOut;
import com.emoldino.api.common.resource.composite.alrrst.service.AlrRstService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AlrRstControllerImpl implements AlrRstController {

	@Override
	public AlrRstGetOut get(AlrRstGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrRstService.class).get(input, pageable);
	}

	@Override
	public SuccessOut postNoteBatch(AlrRstGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrRstService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
