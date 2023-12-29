package com.emoldino.api.common.resource.composite.fletmp.service;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItem;
import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItemRepository;
import com.emoldino.api.common.resource.base.file.service.FileService;
import com.emoldino.api.common.resource.composite.fletmp.dto.FleTmpPostOut;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.ValueUtils;

@Service
public class FleTmpService {

	public void get(String id, HttpServletResponse response) {
//		File file = null;
		InputStream is = null;
		String fileName = null;
		String contentType = null;
		try {
			if (NumberUtils.isCreatable(id)) {
				Long idVal = ValueUtils.toLong(id, 0L);
				FileItem item = BeanUtils.get(FileItemRepository.class).findById(idVal).orElse(null);
				if (item == null || StringUtils.isBlank(item.getFileLocation())) {
					return;
				}

				fileName = item.getFileName();
				contentType = item.getContentType();

				is = BeanUtils.get(FileService.class).getInputStreamByFileLocation(item.getFileLocation());
//				file = BeanUtils.get(FileService.class).getByFileLocation(item.getFileLocation());
			} else if (BeanUtils.get(FileService.class).existsTmp(id)) {
				// 1. Get Tmp File
				is = BeanUtils.get(FileService.class).getTmpInputStream(id);
//				file = BeanUtils.get(FileService.class).getTmp(id);
			}
//			if (file == null || !file.exists()) {
//				return;
//			}
//			is = new FileInputStream(file);
		} catch (Exception e) {
			ValueUtils.closeQuietly(is);
			AbstractException ae = ValueUtils.toAe(e, "FILE_DOWN_FAIL");
			throw ae;
		}

		fileName = ValueUtils.toString(fileName, id);
		HttpUtils.respondFile(is, fileName, contentType, response);
	}

	public FleTmpPostOut post(MultipartFile file) throws IOException {
		String id = BeanUtils.get(FileService.class).postTmp(file).getId();
		return new FleTmpPostOut(id);
	}

}
