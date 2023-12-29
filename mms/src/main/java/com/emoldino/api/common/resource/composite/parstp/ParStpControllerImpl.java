package com.emoldino.api.common.resource.composite.parstp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpGetIn;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpGetOut;
import com.emoldino.api.common.resource.composite.parstp.service.ParStpService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class ParStpControllerImpl implements ParStpController {

	@Override
	public ParStpGetOut get(ParStpGetIn input, Pageable pageable) {
		return BeanUtils.get(ParStpService.class).get(input, pageable);
	}

	@Override
	public SuccessOut disable(List<Long> id) {
		BeanUtils.get(ParStpService.class).disable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enable(List<Long> id) {
		BeanUtils.get(ParStpService.class).enable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disableBatch(ParStpGetIn input, BatchIn batchin) {
		BeanUtils.get(ParStpService.class).disableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableBatch(ParStpGetIn input, BatchIn batchin) {
		BeanUtils.get(ParStpService.class).enableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postNoteBatch(ParStpGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(ParStpService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

	@Override
	public void export(ParStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException {
		BeanUtils.get(ParStpService.class).export(input, batchin, pageable == null ? null : pageable.getSort(), response);
	}

	@Override
	public SuccessOut moveTabItemsBatch(ParStpGetIn input, BatchIn batchin, String toTabName) {
		BeanUtils.get(ParStpService.class).moveTabItemsBatch(input, batchin, toTabName);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut deleteTabItemsBatch(ParStpGetIn input, BatchIn batchin) {
		BeanUtils.get(ParStpService.class).deleteTabItemsBatch(input, batchin);
		return SuccessOut.getDefault();
	}
}
