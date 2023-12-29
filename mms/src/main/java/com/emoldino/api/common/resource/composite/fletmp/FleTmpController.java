package com.emoldino.api.common.resource.composite.fletmp;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.common.resource.composite.fletmp.dto.FleTmpPostOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / File Temporary")
@RequestMapping("/api/common/fle-tmp")
public interface FleTmpController {
	public static final String NAME = "File Temporary";
	public static final String NAME_PLURAL = "Files Temporary";

	@ApiOperation("Download " + NAME)
	@GetMapping("/{id}")
	void get(@PathVariable(name = "id") String id, HttpServletResponse response);

	@ApiOperation("Post " + NAME)
	@PostMapping
	FleTmpPostOut post(MultipartFile file) throws IOException;

}
