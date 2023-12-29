package com.emoldino.api.common.resource.composite.ssrstp;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpGetIn;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpGetOut;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpPutSubscriptionTermIn;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Sensor Config")
@RequestMapping("/api/common/ssr-stp")
public interface SsrStpController {
	String NAME = "Sensor";
	String NAME_PLURAL = "Sensors";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	SsrStpGetOut get(SsrStpGetIn input, Pageable pageable);

	@ApiOperation("Disable " + NAME_PLURAL)
	@PutMapping("/disable")
	SuccessOut disable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Eanble " + NAME_PLURAL)
	@PutMapping("/enable")
	SuccessOut enable(@RequestParam(name = "id") List<Long> id);

	@ApiOperation("Disable " + NAME_PLURAL + " Batch")
	@PutMapping("/disable-batch")
	SuccessOut disableBatch(SsrStpGetIn input, BatchIn batchin);

	@ApiOperation("Enable " + NAME_PLURAL + " Batch")
	@PutMapping("/enable-batch")
	SuccessOut enableBatch(SsrStpGetIn input, BatchIn batchin);

	@ApiOperation("Post " + NAME_PLURAL + " Note Batch")
	@PostMapping("/note-batch")
	SuccessOut postNoteBatch(SsrStpGetIn input, BatchIn batchin, @RequestBody NoteIn body);

	@ApiOperation("Put " + NAME_PLURAL + " Subscription Term Batch")
	@PutMapping("/subscription-term-batch")
	SuccessOut putSubscriptionTermBatch(SsrStpGetIn input, BatchIn batchin, @RequestBody SsrStpPutSubscriptionTermIn body);

	@ApiOperation("Move " + NAME_PLURAL + " Tab Items Batch")
	@PutMapping("/move-tab-items-batch")
	SuccessOut moveTabItemsBatch(SsrStpGetIn input, BatchIn batchin, @RequestParam(name = "toTabName") String toTabName);

	@ApiOperation("Remove " + NAME_PLURAL + " Tab Items Batch")
	@DeleteMapping("/tab-items-batch")
	SuccessOut deleteTabItemsBatch(SsrStpGetIn input, BatchIn batchin);

}
