package com.emoldino.api.common.resource.composite.nothis;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.noti.dto.NotiUserItem;
import com.emoldino.api.common.resource.composite.nothis.dto.NotHisGetIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Notification History")
@RequestMapping("/api/common/not-his")
public interface NotHisController {
	public static final String NAME = "Notification History";
	public static final String NAME_PLURAL = "Notification Histories";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	Page<NotiUserItem> get(NotHisGetIn input, Pageable pageable);

	@ApiOperation("Read " + NAME + " by ID")
	@PostMapping("/{id}/read")
	SuccessOut read(@PathVariable("id") Long id);

}
