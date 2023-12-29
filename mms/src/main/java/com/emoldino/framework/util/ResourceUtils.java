package com.emoldino.framework.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.emoldino.framework.exception.AbstractException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUtils {

	public static InputStream getInputStream(String path) {
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource resource = loader.getResource(path.trim());
		if (!resource.exists()) {
			return null;
		}

		InputStream is = null;
		try {
			is = resource.getInputStream();
			return is;
		} catch (IOException e) {
			AbstractException ae = ValueUtils.toAe(e, null);
			throw ae;
		}
	}

	public static <T> T toRequiredType(String path, Class<T> requiredType) {
		// 1. Get String from Resource
		String str = null;
		{
			InputStream is = getInputStream(path);
			if (is == null) {
				return null;
			}
			try {
				str = IOUtils.toString(is, "UTF-8");
			} catch (IOException e) {
				AbstractException ae = ValueUtils.toAe(e, null);
				throw ae;
			} finally {
				ValueUtils.closeQuietly(is);
			}
		}

		// 2. String -> T
		T data = ValueUtils.toRequiredType(str, requiredType);
		return data;
	}

}
