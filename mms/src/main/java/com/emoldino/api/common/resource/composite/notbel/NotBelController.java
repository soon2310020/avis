package com.emoldino.api.common.resource.composite.notbel;

import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiCategory;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelGetIn;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelGetOut;
import com.emoldino.api.common.resource.composite.notbel.dto.NotBelPostOneIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Notification Bell")
@RequestMapping("/api/common/not-bel")
public interface NotBelController {
	public static final String NAME = "Notification Bell";
	public static final String NAME_PLURAL = "Notification Bells";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	NotBelGetOut get(NotBelGetIn input, Pageable pageable);

	@ApiOperation("Post " + NAME)
	@PostMapping("/one")
	SuccessOut postOne(@RequestBody NotBelPostOneIn input);

	@ApiOperation("Post Dummy " + NAME_PLURAL)
	@PostMapping("/dummy/{id}")
	SuccessOut postDummy(//
			@PathVariable(name = "id") Long id, //
			@RequestParam(name = "code") String code, //
			@RequestParam(name = "email") String email);

	@ApiOperation("Post " + NAME)
	@PostMapping(value = "/email", consumes = MediaType.TEXT_HTML_VALUE)
	SuccessOut postEmail(//
			@RequestParam(name = "title") String title, //
			@RequestParam(name = "to") String to, //
			@RequestBody String content);

	@ApiOperation("Turn " + NAME + " On/Off")
	@PostMapping("/turn-on-off")
	SuccessOut turnOnOff(@RequestParam(name = "on", required = true) Boolean on);

	@ApiOperation("Read " + NAME + " by ID")
	@PostMapping("/{id}/read")
	SuccessOut read(@PathVariable("id") Long id);

	@ApiOperation("Mark " + NAME_PLURAL + " - as read")
	@PostMapping("/read")
	SuccessOut read(@RequestParam(name = "notiCategory", required = false) NotiCategory notiCategory);

}
