
package com.emoldino.api.production.resource.composite.alrmchdtm;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmGetIn;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmGetOut;
import com.emoldino.api.production.resource.composite.alrmchdtm.service.AlrMchDtmService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlrMchDtmControllerImpl implements AlrMchDtmController {

	@Override
	public AlrMchDtmGetOut get(AlrMchDtmGetIn input, Pageable pageable) {
		return BeanUtils.get(AlrMchDtmService.class).get(input, pageable);
	}

	@Override
	public SuccessOut postNoteBatch(AlrMchDtmGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(AlrMchDtmService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

}
