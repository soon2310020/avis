package com.emoldino.api.common.resource.composite.tmnstp;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetIn;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Terminal Config")
@RequestMapping("/api/common/tmn-stp")
public interface TmnStpController {
	String NAME = "Terminal";
	String NAME_PLURAL = "Terminals";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	TmnStpGetOut get(TmnStpGetIn input, Pageable pageable);

	@ApiOperation("Disable " + NAME_PLURAL)
	@PutMapping("/disable")
	SuccessOut disable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Enable " + NAME_PLURAL)
	@PutMapping("/enable")
	SuccessOut enable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Disable " + NAME_PLURAL + " Batch")
	@PutMapping("/disable-batch")
	SuccessOut disableBatch(TmnStpGetIn input, BatchIn batchin);

	@ApiOperation("Enable " + NAME_PLURAL + " Batch")
	@PutMapping("/enable-batch")
	SuccessOut enableBatch(TmnStpGetIn input, BatchIn batchin);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(TmnStpGetIn input, BatchIn batchin, @RequestBody NoteIn body);

	@ApiOperation("Move " + NAME_PLURAL + " Tab Items Batch")
	@PutMapping("/move-tab-items-batch")
	SuccessOut moveTabItemsBatch(TmnStpGetIn input, BatchIn batchin, @RequestParam(name = "toTabName") String toTabName);

	@ApiOperation("Remove " + NAME_PLURAL + " Tab Items Batch")
	@DeleteMapping("/tab-items-batch")
	SuccessOut deleteTabItemsBatch(TmnStpGetIn input, BatchIn batchin);

}
