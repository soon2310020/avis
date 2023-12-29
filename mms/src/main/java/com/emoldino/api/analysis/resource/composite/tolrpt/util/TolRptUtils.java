package com.emoldino.api.analysis.resource.composite.tolrpt.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.jxls.common.Context;
import org.jxls.common.JxlsException;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TolRptUtils {
	/**
	 * Static Export data to Excel using Jxls template.
	 * @see <a href="https://jxls.sourceforge.net/samples/object_collection.html"> Reference website </a>
	 * @param response : Response Out Put Excel
	 * @param templateName : Excel Template Name
	 * @param outputName : Out Put Excel Name
	 * @param varListName : Object Collection Name
	 * @param varList : Collection Name
	 * @return
	 */
	public void staticJxls(HttpServletResponse response, String templateName, String outputName, //
			String varListName, List<?> varList) throws Exception {
		Context context = new Context();
		context.putVar(varListName, varList);

		jxlsTemplateExport(response, templateName, outputName, context);
	}

	/**
	 * Dynamic Export data to Excel using Jxls template.
	 * @see <a href="https://jxls.sourceforge.net/samples/dynamic_grid.html"> Reference website </a>
	 * @param response : Response Out Put Excel
	 * @param templateName : Excel Template Name
	 * @param outputName : Out Put Excel Name
	 * @param varList : Collection Name
	 * @return
	 */
	public void dynamicJxls(HttpServletResponse response, String templateName, String outputName, List<?> varList) throws Exception {
		List<String> headers = varList.stream()//
				.findFirst()//
				.map(header -> {
					return Arrays.stream(header.getClass().getDeclaredFields())//
							.map(Field::getName)//
							.collect(Collectors.toList());
				})//
				.orElse(Collections.emptyList());

		Context context = new Context();
		context.putVar("headers", headers);
		context.putVar("data", varList);

		jxlsTemplateExport(response, templateName, outputName, context);
	}

	private void jxlsTemplateExport(HttpServletResponse response, String templateName, String outputName, Context context) throws Exception {
		try (InputStream is = getFileTemplateExcel(templateName); //
				OutputStream os = response.getOutputStream()) {

			response.setContentType("application/msexcel");
			response.setHeader("Content-Disposition", "attachment; filename=" + outputName);

			JxlsHelper.getInstance().processTemplate(is, os, context);
		} catch (IOException | JxlsException e) {
			log.error("An jxls export or I/O error occurred", e);
			throw new Exception("Excel export error", e);
		}
	}

	private static InputStream getFileTemplateExcel(String fileName) {
		try {
			DefaultResourceLoader loader = new DefaultResourceLoader();
			Resource storeFile = loader.getResource("classpath:exports/excels/jxls/" + fileName);
			return storeFile.getInputStream();
		} catch (IOException e) {
			log.error("An error occurred while loading the template file", e);
			return null;
		}
	}
}
