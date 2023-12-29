package com.emoldino.api.production.resource.composite.alrmchdtm;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmGetIn;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(protocols = "http, https", tags = "Production / Alert / Machine Downtime")
@RequestMapping("/api/production/alr-mch-dtm")
public interface AlrMchDtmController {
	String NAME = "Machine Downtime";
	String NAME_PLURAL = "Machine Downtimes";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	AlrMchDtmGetOut get(AlrMchDtmGetIn input, Pageable pageable);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(AlrMchDtmGetIn input, BatchIn batchin, @RequestBody NoteIn body);

}
