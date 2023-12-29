package com.emoldino.api.common.resource.composite.alrdet;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetGetIn;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Detachment Alert")
@RequestMapping("/api/common/alr-det")
public interface AlrDetController {
	public static final String NAME = "Detachment";
	public static final String NAME_PLURAL = "Detachments";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	AlrDetGetOut get(AlrDetGetIn input, Pageable pageable);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(AlrDetGetIn input, BatchIn batchin, @RequestBody NoteIn body);
}
