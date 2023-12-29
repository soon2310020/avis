package com.emoldino.api.common.resource.composite.alrdco;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoGetIn;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoTerminalsGetOut;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoToolingsGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Alert / Disconnection")
@RequestMapping("/api/common/alr-dco")
public interface AlrDcoController {
	public static final String NAME = "Disconnection";
	public static final String NAME_PLURAL = "Disconnections";

	@ApiOperation("Get Terminal " + NAME_PLURAL)
	@GetMapping("/terminals")
	AlrDcoTerminalsGetOut getTerminals(AlrDcoGetIn input, Pageable pageable);

	@ApiOperation("Get Tooling " + NAME_PLURAL)
	@GetMapping("/toolings")
	AlrDcoToolingsGetOut getToolings(AlrDcoGetIn input, Pageable pageable);

	@ApiOperation("Post Terminal " + NAME_PLURAL + " Note Batch")
	@PostMapping("/terminals/note-batch")
	SuccessOut postTerminalsNoteBatch(AlrDcoGetIn input, BatchIn batchin, @RequestBody NoteIn body);

	@ApiOperation("Post Tooling " + NAME_PLURAL + " Note Batch")
	@PostMapping("/toolings/note-batch")
	SuccessOut postToolingsNoteBatch(AlrDcoGetIn input, BatchIn batchin, @RequestBody NoteIn body);

}
