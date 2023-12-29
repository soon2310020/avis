package com.emoldino.api.common.resource.composite.fletmp;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.common.resource.composite.fletmp.dto.FleTmpPostOut;
import com.emoldino.api.common.resource.composite.fletmp.service.FleTmpService;

@RestController
public class FleTmpControllerImpl implements FleTmpController {
	@Autowired
	FleTmpService service;

	@Override
	public void get(String id, HttpServletResponse response) {
		if (id.contains("~")) {
			id = id.substring(id.lastIndexOf("~") + 1);
		}
		service.get(id, response);
	}

	@Override
	public FleTmpPostOut post(MultipartFile file) throws IOException {
		return service.post(file);
	}

}
