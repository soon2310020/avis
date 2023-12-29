package com.emoldino.api.common.resource.composite.ssrstp;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpGetIn;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpGetOut;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpPutSubscriptionTermIn;
import com.emoldino.api.common.resource.composite.ssrstp.service.SsrStpService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class SsrStpControllerImpl implements SsrStpController {
	@Override
	public SsrStpGetOut get(SsrStpGetIn input, Pageable pageable) {
		return BeanUtils.get(SsrStpService.class).get(input, pageable);
	}

	@Override
	public SuccessOut disable(List<Long> id) {
		BeanUtils.get(SsrStpService.class).disable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enable(List<Long> id) {
		BeanUtils.get(SsrStpService.class).enable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disableBatch(SsrStpGetIn input, BatchIn batchin) {
		BeanUtils.get(SsrStpService.class).disableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableBatch(SsrStpGetIn input, BatchIn batchin) {
		BeanUtils.get(SsrStpService.class).enableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postNoteBatch(SsrStpGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(SsrStpService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut putSubscriptionTermBatch(SsrStpGetIn input, BatchIn batchin, SsrStpPutSubscriptionTermIn body) {
		BeanUtils.get(SsrStpService.class).putSubscriptionTermBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut moveTabItemsBatch(SsrStpGetIn input, BatchIn batchin, String toTabName) {
		BeanUtils.get(SsrStpService.class).moveTabItemsBatch(input, batchin, toTabName);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut deleteTabItemsBatch(SsrStpGetIn input, BatchIn batchin) {
		BeanUtils.get(SsrStpService.class).deleteTabItemsBatch(input, batchin);
		return SuccessOut.getDefault();
	}

}
