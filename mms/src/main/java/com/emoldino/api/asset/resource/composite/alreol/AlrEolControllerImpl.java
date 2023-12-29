
package com.emoldino.api.asset.resource.composite.alreol;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolGetIn;
import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolGetOut;
import com.emoldino.api.asset.resource.composite.alreol.service.AlrEolService;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AlrEolControllerImpl implements AlrEolController {

	@Override
	public AlrEolGetOut get(AlrEolGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrEolService.class).get(input, pageable);
	}

	@Override
	public SuccessOut postNoteBatch(AlrEolGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrEolService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
