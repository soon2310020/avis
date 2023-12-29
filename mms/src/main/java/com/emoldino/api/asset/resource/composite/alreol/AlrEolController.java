package com.emoldino.api.asset.resource.composite.alreol;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolGetIn;
import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolGetOut;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset / Alert / End of Life")
@RequestMapping("/api/asset/alr-eol")
public interface AlrEolController {
	String NAME = "End of Life";
	String NAME_PLURAL = "End of Life";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	AlrEolGetOut get(AlrEolGetIn input, Pageable pageable);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(AlrEolGetIn input, BatchIn batchin, @RequestBody NoteIn body);

}
