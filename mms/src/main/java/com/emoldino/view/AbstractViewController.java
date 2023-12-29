package com.emoldino.view;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;

public abstract class AbstractViewController {

	private String path;

	private String getPath() {
		if (path == null) {
			Class<?> clazz = getClass();
			RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
			LogicUtils.assertNotEmpty(mapping, "@RequestMapping");
			LogicUtils.assertNotEmpty(mapping.value(), "@RequestMapping.path");
			path = mapping.value()[0];
		}
		return path;
	}

	protected String getIndexPath() {
		String modPath = getPath();
		LogicUtils.assertNotEmpty(modPath, "modPath");
		HttpServletRequest req = HttpUtils.getRequest();
		String url = req.getRequestURI();
		if (!url.startsWith("/")) {
			url = "/" + url;
		}
		if (url.contains("?")) {
			url = url.substring(0, url.indexOf("?"));
		}
		int index = url.indexOf(modPath);

		if (index < 0) {
			throw new LogicException("INVALID_MOD_PATH", //
					Property.builder().name("url").value(url).build(), //
					Property.builder().name("modPath").value(modPath).build()//
			);
		}

		StringBuilder buf = new StringBuilder(index == 0 ? url : url.substring(index));
		return buf.append("/index").toString().substring(1);
	}

	@GetMapping("/**/*")
	public String get() {
		return getIndexPath();
	}

	@GetMapping
	public String root() {
		return "/empty";
	}
}
