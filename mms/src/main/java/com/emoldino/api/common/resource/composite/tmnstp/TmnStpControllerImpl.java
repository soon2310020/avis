package com.emoldino.api.common.resource.composite.tmnstp;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetIn;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetOut;
import com.emoldino.api.common.resource.composite.tmnstp.service.TmnStpService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class TmnStpControllerImpl implements TmnStpController {

	@Override
	public TmnStpGetOut get(TmnStpGetIn input, Pageable pageable) {
		return BeanUtils.get(TmnStpService.class).get(input, pageable);
	}

	@Override
	public SuccessOut disable(List<Long> id) {
		BeanUtils.get(TmnStpService.class).disable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enable(List<Long> id) {
		BeanUtils.get(TmnStpService.class).enable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disableBatch(TmnStpGetIn input, BatchIn batchin) {
		BeanUtils.get(TmnStpService.class).disableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableBatch(TmnStpGetIn input, BatchIn batchin) {
		BeanUtils.get(TmnStpService.class).enableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	public SuccessOut postNoteBatch(TmnStpGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(TmnStpService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut moveTabItemsBatch(TmnStpGetIn input, BatchIn batchin, String toTabName) {
		BeanUtils.get(TmnStpService.class).moveTabItemsBatch(input, batchin, toTabName);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut deleteTabItemsBatch(TmnStpGetIn input, BatchIn batchin) {
		BeanUtils.get(TmnStpService.class).deleteTabItemsBatch(input, batchin);
		return SuccessOut.getDefault();
	}

}
