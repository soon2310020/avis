package com.emoldino.api.asset.resource.composite.alrrlo;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloGetIn;
import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloGetOut;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset / Alert / Relocation")
@RequestMapping("/api/asset/alr-rlo")
public interface AlrRloController {
	public static final String NAME = "Relocation";
	public static final String NAME_PLURAL = "Relocations";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	AlrRloGetOut get(AlrRloGetIn input, Pageable pageable);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(AlrRloGetIn input, BatchIn batchin, @RequestBody NoteIn body);
}
