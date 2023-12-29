
package com.emoldino.api.common.resource.composite.alrdet;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetGetIn;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetGetOut;
import com.emoldino.api.common.resource.composite.alrdet.service.AlrDetService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AlrDetControllerImpl implements AlrDetController {

	@Override
	public AlrDetGetOut get(AlrDetGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrDetService.class).get(input, pageable);
	}

	@Override
	public SuccessOut postNoteBatch(AlrDetGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrDetService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
