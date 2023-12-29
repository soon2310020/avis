
package com.emoldino.api.common.resource.composite.pltstp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpArea;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpGetIn;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpGetOut;
import com.emoldino.api.common.resource.composite.pltstp.service.PltStpService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class PltStpControllerImpl implements PltStpController {

	@Override
	public PltStpGetOut get(PltStpGetIn input, Pageable pageable) {
		return BeanUtils.get(PltStpService.class).get(input, pageable);
	}

	@Override
	public ListOut<PltStpArea> getAreas(Long locationId) {
		return BeanUtils.get(PltStpService.class).getAreas(locationId);
	}

	@Override
	public SuccessOut postAreas(Long locationId, ListIn<PltStpArea> areas) {
		BeanUtils.get(PltStpService.class).postAreas(locationId, areas);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disable(List<Long> id) {
		BeanUtils.get(PltStpService.class).disable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enable(List<Long> id) {
		BeanUtils.get(PltStpService.class).enable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disableBatch(PltStpGetIn input, BatchIn batchin) {
		BeanUtils.get(PltStpService.class).disableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableBatch(PltStpGetIn input, BatchIn batchin) {
		BeanUtils.get(PltStpService.class).enableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postNoteBatch(PltStpGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(PltStpService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();
	}

	@Override
	public void export(PltStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException {
		BeanUtils.get(PltStpService.class).export(input, batchin, pageable == null ? null : pageable.getSort(), response);
	}

	@Override
	public SuccessOut moveTabItemsBatch(PltStpGetIn input, BatchIn batchin, String toTabName) {
		BeanUtils.get(PltStpService.class).moveTabItemsBatch(input, batchin, toTabName);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut deleteTabItemsBatch(PltStpGetIn input, BatchIn batchin) {
		BeanUtils.get(PltStpService.class).deleteTabItemsBatch(input, batchin);
		return SuccessOut.getDefault();
	}

}
