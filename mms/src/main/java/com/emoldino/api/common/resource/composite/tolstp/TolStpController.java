package com.emoldino.api.common.resource.composite.tolstp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpExportDataIn;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpGetIn;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.dto.TimeSetting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Tooling Config")
@RequestMapping("/api/common/tol-stp")
public interface TolStpController {
	public static final String NAME = "Tooling";
	public static final String NAME_PLURAL = "Toolings";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	TolStpGetOut get(TolStpGetIn input, Pageable pageable);

	@ApiOperation("Disable " + NAME_PLURAL)
	@PutMapping("/disable")
	SuccessOut disable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Enable " + NAME_PLURAL)
	@PutMapping("/enable")
	SuccessOut enable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Enable " + NAME_PLURAL + " Batch")
	@PutMapping("/enable-batch")
	SuccessOut enableBatch(TolStpGetIn input, BatchIn batchin);

	@ApiOperation("Disable " + NAME_PLURAL + " Batch")
	@PutMapping("/disable-batch")
	SuccessOut disableBatch(TolStpGetIn input, BatchIn batchin);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(TolStpGetIn input, BatchIn batchin, @RequestBody NoteIn body);

	/*
	@ApiOperation("Export Static " + NAME_PLURAL)
	@GetMapping("/export")
	void export(TolStpExportDataIn input, //
			@Deprecated //
			@RequestParam(required = false) //
			Integer timezoneOffsetClient, //
			BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException;
	*/

	@ApiOperation("Export Static " + NAME_PLURAL)
	@GetMapping("/export")
	void export(TolStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException;

	@ApiOperation("Export Dynamic " + NAME_PLURAL)
	@GetMapping("/export-dynamic-data")
	ResponseEntity<String> exportDynamicData(TolStpExportDataIn input, TimeSetting timeSetting, BatchIn batchin, Pageable pageable, HttpServletResponse response);

	@ApiOperation("Move " + NAME_PLURAL + " Tab Items Batch")
	@PutMapping("/move-tab-items-batch")
	SuccessOut moveTabItemsBatch(TolStpGetIn input, BatchIn batchin, @RequestParam(name = "toTabName") String toTabName);

	@ApiOperation("Remove " + NAME_PLURAL + " Tab Items Batch")
	@DeleteMapping("/tab-items-batch")
	SuccessOut deleteTabItemsBatch(TolStpGetIn input, BatchIn batchin);

}
