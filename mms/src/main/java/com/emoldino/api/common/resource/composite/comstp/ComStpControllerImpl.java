package com.emoldino.api.common.resource.composite.comstp;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpGetIn;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpGetOut;
import com.emoldino.api.common.resource.composite.comstp.service.ComStpService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class ComStpControllerImpl implements ComStpController {

	@Override
	public ComStpGetOut get(ComStpGetIn input, Pageable pageable) {
		return BeanUtils.get(ComStpService.class).get(input, pageable);
	}

	@Override
	public SuccessOut disableBatch(ComStpGetIn input, BatchIn batchin) {
		BeanUtils.get(ComStpService.class).disableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableBatch(ComStpGetIn input, BatchIn batchin) {
		BeanUtils.get(ComStpService.class).enableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postNoteBatch(ComStpGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(ComStpService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

	@Override
	public void export(ComStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException {
		BeanUtils.get(ComStpService.class).export(input, batchin, pageable == null ? null : pageable.getSort(), response);
	}

	@Override
	public SuccessOut moveTabItemsBatch(ComStpGetIn input, BatchIn batchin, String toTabName) {
		BeanUtils.get(ComStpService.class).moveTabItemsBatch(input, batchin, toTabName);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut deleteTabItemsBatch(ComStpGetIn input, BatchIn batchin) {
		BeanUtils.get(ComStpService.class).deleteTabItemsBatch(input, batchin);
		return SuccessOut.getDefault();
	}

}
