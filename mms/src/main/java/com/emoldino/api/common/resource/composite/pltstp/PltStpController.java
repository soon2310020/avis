package com.emoldino.api.common.resource.composite.pltstp;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpArea;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpGetIn;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Plant Config")
@RequestMapping("/api/common/plt-stp")
public interface PltStpController {
	public static final String NAME = "Plant";
	public static final String NAME_PLURAL = "Plants";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	PltStpGetOut get(PltStpGetIn input, Pageable pageable);

	@ApiOperation("Get " + NAME + " Areas")
	@GetMapping("/{locationId}/areas")
	ListOut<PltStpArea> getAreas(@PathVariable(name = "locationId") Long locationId);

	@ApiOperation("Post " + NAME + " Areas")
	@PostMapping("/{locationId}/areas")
	SuccessOut postAreas(@PathVariable(name = "locationId") Long locationId, @RequestBody ListIn<PltStpArea> areas);

	@ApiOperation("Disable " + NAME_PLURAL)
	@PutMapping("/disable")
	SuccessOut disable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Enable " + NAME_PLURAL)
	@PutMapping("/enable")
	SuccessOut enable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Disable " + NAME_PLURAL + " Batch")
	@PutMapping("/disable-batch")
	SuccessOut disableBatch(PltStpGetIn input, BatchIn batchin);

	@ApiOperation("Enable " + NAME_PLURAL + " Batch")
	@PutMapping("/enable-batch")
	SuccessOut enableBatch(PltStpGetIn input, BatchIn batchin);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(PltStpGetIn input, BatchIn batchin, @RequestBody NoteIn body);

	@ApiOperation("Export Static " + NAME_PLURAL)
	@GetMapping("/export")
	void export(PltStpGetIn input, BatchIn batchin, Pageable pageable, HttpServletResponse response) throws IOException;

	@ApiOperation("Move " + NAME_PLURAL + " Tab Items Batch")
	@PutMapping("/move-tab-items-batch")
	SuccessOut moveTabItemsBatch(PltStpGetIn input, BatchIn batchin, @RequestParam(name = "toTabName") String toTabName);

	@ApiOperation("Remove " + NAME_PLURAL + " Tab Items Batch")
	@DeleteMapping("/tab-items-batch")
	SuccessOut deleteTabItemsBatch(PltStpGetIn input, BatchIn batchin);

}
