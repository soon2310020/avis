package com.emoldino.api.common.resource.composite.alrrst;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstGetIn;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Alert / Reset")
@RequestMapping("/api/common/alr-rst")
public interface AlrRstController {
	public static final String NAME = "Reset";
	public static final String NAME_PLURAL = "Resets";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	AlrRstGetOut get(AlrRstGetIn input, Pageable pageable);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(AlrRstGetIn input, BatchIn batchin, @RequestBody NoteIn body);

}
