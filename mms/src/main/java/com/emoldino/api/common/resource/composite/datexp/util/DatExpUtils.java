package com.emoldino.api.common.resource.composite.datexp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.util.FileUtils;
import com.emoldino.framework.dto.LargeListOut;
import com.emoldino.framework.util.Closure1Param;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ResourceUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatExpUtils {

	/**
	 * Export Excel by Jxls Template.
	 * @param <T>
	 * @param groupCode: Same as groupCode in Excel Template Name
	 * @param get
	 * @param pageSize
	 * @param sort
	 * @param fileNamePrefix: Exported File Name Prefix
	 * @param response
	 */
	public static <T> void exportByJxls(//
			String groupCode, //
			Closure1Param<Pageable, Page<T>> get, //
			int pageSize, Sort sort, String fileNamePrefix, //
			HttpServletResponse response//
	) {
		LogicUtils.assertNotNull(response, "response");
		doByJxls(groupCode, get, pageSize, sort, fileNamePrefix, null, response);
	}

	public static <T> void appendByJxls(//
			String groupCode, //
			Closure1Param<Pageable, Page<T>> get, //
			int pageSize, Sort sort, String fileNamePrefix, //
			ZipOutputStream zos) {
		LogicUtils.assertNotNull(zos, "zos");
		doByJxls(groupCode, get, pageSize, sort, fileNamePrefix, zos, null);
	}

	public static <T> void exportByJxls(//
			String groupCode, //
			Context context, //
			String fileNamePrefix, //
			HttpServletResponse response//
	) {
		LogicUtils.assertNotNull(response, "response");
		doByJxls(groupCode, context, fileNamePrefix, null, response);
	}

	private static <T> void doByJxls(//
			String groupCode, //
			Closure1Param<Pageable, Page<T>> get, //
			int pageSize, Sort sort, String fileNamePrefix, //
			ZipOutputStream zos, //
			HttpServletResponse response//
	) {
		LogicUtils.assertNotEmpty(groupCode, "groupCode");
		LogicUtils.assertNotNull(get, "getMethod");
		LogicUtils.assertNotNull(sort, "sort");
		LogicUtils.assertNotEmpty(fileNamePrefix, "fileNamePrefix");

		Context context = new Context();
		TranUtils.doTran(() -> {
			context.putVar("output", get.execute(PageRequest.of(0, 1, sort)));
			context.putVar("content", new LargeListOut<T>(get, pageSize, sort).getContent());
		});

		doByJxls(groupCode, context, fileNamePrefix, zos, response);
	}

	private static <T> void doByJxls(//
			String groupCode, //
			Context context, //
			String fileNamePrefix, //
			ZipOutputStream zos, //
			HttpServletResponse response//
	) {
		LogicUtils.assertNotEmpty(groupCode, "groupCode");
		LogicUtils.assertNotEmpty(context, "context");
		LogicUtils.assertNotEmpty(fileNamePrefix, "fileNamePrefix");

		context.putVar("em", new DatExpJxlsEm());

		InputStream is = null;
		try {
			File file = FileUtils.getReleasedByGroupTypeAndCode(FileGroupType.DATA_EXPORT_TEMPLATE, groupCode);
			if (file != null && file.exists()) {
				is = new FileSystemResource(file).getInputStream();
			} else {
				is = ResourceUtils.getInputStream("/file/dataexport/" + groupCode + ".xlsx");
				LogicUtils.assertNotNull(is, "defaultTemplate");
			}
			String fileName = fileNamePrefix + " (" + new Date().getTime() + ")";

			if (zos != null) {
				try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
					JxlsHelper.getInstance()//
//							.setUseFastFormulaProcessor(true)//
							.processTemplate(is, bos, context);
					ZipEntry zipEntry = new ZipEntry(fileName + ".xlsx");
					zos.putNextEntry(zipEntry);
					bos.writeTo(zos);
					zos.flush();
				}
			} else {
				try (OutputStream os = response.getOutputStream()) {
					response.setContentType("application/msexcel");
					response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
					JxlsHelper.getInstance()//
//							.setUseFastFormulaProcessor(true)//
							.processTemplate(is, os, context);
				}
			}

		} catch (Exception e) {
			throw ValueUtils.toAe(e, "FILE_DOWN_FAIL");
		} finally {
			ValueUtils.closeQuietly(is);
		}
	}

}
