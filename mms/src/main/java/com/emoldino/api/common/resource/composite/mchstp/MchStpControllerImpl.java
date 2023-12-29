package com.emoldino.api.common.resource.composite.mchstp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpGetIn;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpGetOut;
import com.emoldino.api.common.resource.composite.mchstp.service.MchStpService;
import com.emoldino.api.common.resource.composite.tmnstp.service.TmnStpService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class MchStpControllerImpl implements MchStpController {

	@Override
	public MchStpGetOut get(MchStpGetIn input, Pageable pageable) {
		return BeanUtils.get(MchStpService.class).get(input, pageable);
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
	public SuccessOut disableBatch(MchStpGetIn input, BatchIn batchin) {
		BeanUtils.get(MchStpService.class).disableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableBatch(MchStpGetIn input, BatchIn batchin) {
		BeanUtils.get(MchStpService.class).enableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postNoteBatch(MchStpGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(MchStpService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut unmatchBatch(MchStpGetIn input, BatchIn batchin) {
		BeanUtils.get(MchStpService.class).unmatchBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public void export(MchStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException {
		BeanUtils.get(MchStpService.class).export(input, batchin, pageable == null ? null : pageable.getSort(), response);
	}

	@Override
	public SuccessOut moveTabItemsBatch(MchStpGetIn input, BatchIn batchin, String toTabName) {
		BeanUtils.get(MchStpService.class).moveTabItemsBatch(input, batchin, toTabName);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut deleteTabItemsBatch(MchStpGetIn input, BatchIn batchin) {
		BeanUtils.get(MchStpService.class).deleteTabItemsBatch(input, batchin);
		return SuccessOut.getDefault();
	}

}
