package com.emoldino.api.common.resource.composite.comstp;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpGetIn;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Company Config")
@RequestMapping("/api/common/com-stp")
public interface ComStpController {
	public static final String NAME = "Company";
	public static final String NAME_PLURAL = "Companies";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	ComStpGetOut get(ComStpGetIn input, Pageable pageable);

	@ApiOperation("Disable " + NAME_PLURAL + " Batch")
	@PutMapping("/disable-batch")
	SuccessOut disableBatch(ComStpGetIn input, BatchIn batchin);

	@ApiOperation("Enable " + NAME_PLURAL + " Batch")
	@PutMapping("/enable-batch")
	SuccessOut enableBatch(ComStpGetIn input, BatchIn batchin);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(ComStpGetIn input, BatchIn batchin, @RequestBody NoteIn body);

	@ApiOperation("Export Static " + NAME_PLURAL)
	@GetMapping("/export")
	void export(ComStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException;

	@ApiOperation("Move " + NAME_PLURAL + " Tab Items Batch")
	@PutMapping("/move-tab-items-batch")
	SuccessOut moveTabItemsBatch(ComStpGetIn input, BatchIn batchin, @RequestParam(name = "toTabName") String toTabName);

	@ApiOperation("Remove " + NAME_PLURAL + " Tab Items Batch")
	@DeleteMapping("/tab-items-batch")
	SuccessOut deleteTabItemsBatch(ComStpGetIn input, BatchIn batchin);

}
