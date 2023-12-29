package com.emoldino.api.common.resource.composite.mchstp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpGetIn;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Machine Config")
@RequestMapping("/api/common/mch-stp")
public interface MchStpController {
	public static final String NAME = "Machine";
	public static final String NAME_PLURAL = "Machines";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	MchStpGetOut get(MchStpGetIn input, Pageable pageable);

	@ApiOperation("Disable " + NAME_PLURAL)
	@PutMapping("/disable")
	SuccessOut disable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Enable " + NAME_PLURAL)
	@PutMapping("/enable")
	SuccessOut enable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Disable " + NAME_PLURAL + " Batch")
	@PutMapping("/disable-batch")
	SuccessOut disableBatch(MchStpGetIn input, BatchIn batchin);

	@ApiOperation("Enable " + NAME_PLURAL + " Batch")
	@PutMapping("/enable-batch")
	SuccessOut enableBatch(MchStpGetIn input, BatchIn batchin);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(MchStpGetIn input, BatchIn batchin, @RequestBody NoteIn body);

	@ApiOperation("Unmatch " + NAME_PLURAL + " Batch")
	@PutMapping("/unmatch-batch")
	SuccessOut unmatchBatch(MchStpGetIn input, BatchIn batchin);

	@ApiOperation("Export Static " + NAME_PLURAL)
	@GetMapping("/export")
	void export(MchStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException;

	@ApiOperation("Move " + NAME_PLURAL + " Tab Items Batch")
	@PutMapping("/move-tab-items-batch")
	SuccessOut moveTabItemsBatch(MchStpGetIn input, BatchIn batchin, @RequestParam(name = "toTabName") String toTabName);

	@ApiOperation("Remove " + NAME_PLURAL + " Tab Items Batch")
	@DeleteMapping("/tab-items-batch")
	SuccessOut deleteTabItemsBatch(MchStpGetIn input, BatchIn batchin);

}
