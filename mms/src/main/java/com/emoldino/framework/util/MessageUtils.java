package com.emoldino.framework.util;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.MessageSource;
import org.springframework.util.ObjectUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.common.security.UserPrincipal;
import saleson.common.util.SecurityUtils;

@Slf4j
public class MessageUtils {

	public static String get(String code, Properties properties) {
		return get(code, code, properties);
	}

	private static final VelocityEngine VE;
	static {
		VE = new VelocityEngine();
		VE.init();
	}

	public static String get(String code, String defaultValue, Properties properties) {
		if (code == null) {
			return defaultValue;
		}
		Locale locale;
		try {
			UserPrincipal user = SecurityUtils.getUserPrincipal();
			locale = user == null || ObjectUtils.isEmpty(user.getLanguage()) ? Locale.ENGLISH : Locale.forLanguageTag(ValueUtils.toString(user.getLanguage()).toLowerCase());
		} catch (Exception e) {
			locale = Locale.ENGLISH;
		}
		String message;
		try {
			message = BeanUtils.get(MessageSource.class).getMessage(code, new String[] {}, locale);
		} catch (Exception e) {
			message = null;
		}
		if (message == null || message.equals(code)) {
			String codeLower = code.toLowerCase();
			if (!codeLower.equals(code)) {
				try {
					message = BeanUtils.get(MessageSource.class).getMessage(codeLower, new String[] {}, locale);
				} catch (Exception e) {
					message = null;
				}
			}
		}

		// Assign Properties
		try {
			if (message != null && properties != null && !properties.isEmpty() && (message.contains("$") || message.contains("#"))) {
				StringWriter writer = new StringWriter();
				Map<String, Object> map = new HashMap<>();
				properties.forEach((k, v) -> map.put((String) k, v));
				VelocityContext context = new VelocityContext(map);
				VE.evaluate(context, writer, message, message);
				return writer.toString();
			}
		} catch (Exception e) {
			// DO Nothing
			log.warn(e.getMessage(), e);
		}
		return message == null ? defaultValue : message;
	}

}
