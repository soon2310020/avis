package com.emoldino.api.common.resource.composite.tolstp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpExportDataIn;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpGetIn;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpGetOut;
import com.emoldino.api.common.resource.composite.tolstp.service.TolStpService;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

@RestController
public class TolStpControllerImpl implements TolStpController {

	@Override
	public TolStpGetOut get(TolStpGetIn input, Pageable pageable) {
		return BeanUtils.get(TolStpService.class).get(input, pageable);
	}

	@Override
	public SuccessOut disable(List<Long> id) {
		BeanUtils.get(TolStpService.class).disable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enable(List<Long> id) {
		BeanUtils.get(TolStpService.class).enable(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableBatch(TolStpGetIn input, BatchIn batchin) {
		BeanUtils.get(TolStpService.class).enableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disableBatch(TolStpGetIn input, BatchIn batchin) {
		BeanUtils.get(TolStpService.class).disableBatch(input, batchin);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postNoteBatch(TolStpGetIn input, BatchIn batchin, NoteIn body) {
		BeanUtils.get(TolStpService.class).postNoteBatch(input, batchin, body);
		return SuccessOut.getDefault();

	}

	@Override
	public void export(TolStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException {
		BeanUtils.get(TolStpService.class).export(input, batchin, pageable == null ? null : pageable.getSort(), response);
	}
	/*
	@Override
	public void export(TolStpExportDataIn input, Integer timezoneOffsetClient, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException {
		OutputStream outputStream = response.getOutputStream();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=tooling-report-" + new Date().getTime() + ".xlsx");
		outputStream.write(BeanUtils.get(TolStpService.class).exportStatic(input, timezoneOffsetClient, batchin, pageable).toByteArray());
		outputStream.close();
	}
	*/

	@Override
	public ResponseEntity<String> exportDynamicData(TolStpExportDataIn input, TimeSetting timeSetting, BatchIn batchin, Pageable pageable, HttpServletResponse response) {
		try {
			BeanUtils.get(TolStpService.class).exportDynamicData(input, timeSetting, batchin, pageable, response);
		} catch (BizException e) {
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		return ResponseEntity.ok("Success.");
	}

	@Override
	public SuccessOut moveTabItemsBatch(TolStpGetIn input, BatchIn batchin, String toTabName) {
		BeanUtils.get(TolStpService.class).moveTabItemsBatch(input, batchin, toTabName);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut deleteTabItemsBatch(TolStpGetIn input, BatchIn batchin) {
		BeanUtils.get(TolStpService.class).deleteTabItemsBatch(input, batchin);
		return SuccessOut.getDefault();
	}

}
