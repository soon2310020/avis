package vn.com.twendie.avis.locale.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class Translator {
	private static ResourceBundleMessageSource messageSource;

	@Autowired
	Translator(ResourceBundleMessageSource messageSource) {
		Translator.messageSource = messageSource;
	}

	public static String toLocale(String msgCode) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(msgCode, null, locale);
	}

	public static String toLocale(String msgCode, String defaultMsg) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(msgCode, null, defaultMsg, locale);
	}

	public static String toLocale(String msgCode, Object[] args) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(msgCode, args, locale);
	}
}
