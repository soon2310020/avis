package com.emoldino.api.production.resource.composite.alrutm;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmGetIn;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Production / Alert / Uptime")
@RequestMapping("/api/production/alr-utm")
public interface AlrUtmController {
	public static final String NAME = "Uptime";
	public static final String NAME_PLURAL = "Uptimes";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	AlrUtmGetOut get(AlrUtmGetIn input, Pageable pageable);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(AlrUtmGetIn input, BatchIn batchin, @RequestBody NoteIn body);
}
