package com.emoldino.framework.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.enumeration.RenewalType;
import com.emoldino.framework.repository.cachedata.CacheData;
import com.emoldino.framework.repository.cachedata.CacheDataRepository;
import com.emoldino.framework.repository.cachedata.QCacheData;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.Data;
import lombok.experimental.Accessors;
import saleson.common.util.SecurityUtils;

public class CacheDataUtils {

	public static String toKey(Object... keys) {
		StringBuilder buf = new StringBuilder();
		int i = 0;
		for (Object key : keys) {
			buf.append(i++ == 0 ? "" : "&").append(ObjectUtils.isEmpty(key) ? "n" : ValueUtils.toString(key));
		}
		return buf.toString();
	}

	@Data
	public static class CacheDataOptions {
		@Accessors(chain = true)
		RenewalType renewalType;
		@Accessors(chain = true)
		private boolean keyCompanyDisabled;
		@Accessors(chain = true)
		private boolean keyUserIdDisabled;
	}

	private static final CacheDataOptions DEFAULT_OPTIONS = new CacheDataOptions();

	/**
	 * companyId & userId & renewalType & [k,value]
	 * 
	 * @param additionalKeys
	 * @param options
	 * @return
	 */
	private static String getKey(Object[] additionalKeys, CacheDataOptions options) {
		HttpServletRequest req = HttpUtils.getRequest();
		if (req == null) {
			return null;
		}

		if (options == null) {
			options = DEFAULT_OPTIONS;
		}

		StringBuilder buf = new StringBuilder();
		buf.append(options.keyCompanyDisabled ? "n" : SecurityUtils.getCompanyId());
		buf.append("&").append(options.keyUserIdDisabled ? "n" : SecurityUtils.getUserId());
		buf.append("&").append(options.renewalType == null ? "n" : options.renewalType.name().charAt(0));

		Map<String, String[]> paramMap = req.getParameterMap();
		if (paramMap == null || paramMap.isEmpty()) {
			return buf.toString();
		}

		paramMap.forEach((k, v) -> {
			buf.append("&").append(k.charAt(0));
			if (ObjectUtils.isEmpty(v)) {
				return;
			}
			int j = 0;
			for (String str : v) {
				if (ObjectUtils.isEmpty(str)) {
					continue;
				}
				buf.append(j++ == 0 ? "" : ",").append(str);
			}
		});
		if (!ObjectUtils.isEmpty(additionalKeys)) {
			for (Object key : additionalKeys) {
				buf.append("&").append(key);
			}
		}
		return buf.toString();
	}

//	public static <T> T get(String name, Class<T> returnType, Object[] additionalKeys, Closure<T> closure, Closure<Instant> closureLastUpdatedAt) {
//		return get(name, returnType, null, additionalKeys, closure, closureLastUpdatedAt);
//	}

	public static <T> T get(String name, Class<T> returnType, CacheDataOptions options, Object[] additionalKeys, Closure<T> closure, Closure<Instant> closureLastUpdatedAt) {
		return _get(name, returnType, options, getKey(additionalKeys, options), closure, closureLastUpdatedAt);
	}

	private static final ObjectMapper MAPPER;
	static {
		MAPPER = new ObjectMapper();
		MAPPER.setSerializationInclusion(Include.NON_NULL);
//		MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
//		MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
//		MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		MAPPER.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
		MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		// MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		MAPPER.registerModule(new JavaTimeModule());
	}

	private static <T> T _get(String name, Class<T> returnType, CacheDataOptions options, String key, Closure<T> closure, Closure<Instant> getLastUpdatedAt) {
		if (key == null) {
			return null;
		}

		LogicUtils.assertNotEmpty(name, "name");
		LogicUtils.assertNotNull(closure, "closure");

		if (key.length() > 300) {
			return closure.execute();
		}

		Instant now = DateUtils2.newInstant();

		// 1.1 Get Version
		Instant dataLastUpdatedAt = getLastUpdatedAt == null ? null : getLastUpdatedAt.execute();
		// Minus 30 seconds
		if (dataLastUpdatedAt != null) {
			dataLastUpdatedAt = dataLastUpdatedAt.minus(Duration.ofSeconds(30L));
		}

		// 1.2 Get Cache
		CacheData cache = getValid(name, key, options, dataLastUpdatedAt, null);
		if (cache != null) {
			String content = cache.getContent();
			if (content == null) {
				return null;
			}

			try {
				T data = MAPPER.readValue(content, returnType);
				return data;
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "CACHE_DATA_UNDESERIALIZABLE", HttpStatus.SERVICE_UNAVAILABLE,
						"CacheData(name:" + name + ", key:" + key + ", id:" + cache.getId() + ").content is undeserializable to this Class(" + returnType.getName() + ")", e);
			}
		}

		// 2. Get Data
		T data = closure.execute();
		if (dataLastUpdatedAt != null && !dataLastUpdatedAt.isBefore(now)) {
			return data;
		}

		// 3. Leave New Cache

		// 3.1 Serialize to JSON
		String content;
		try {
			content = data == null ? null : MAPPER.writeValueAsString(data);
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "CACHE_DATA_UNSERIALIZABLE", HttpStatus.SERVICE_UNAVAILABLE,
					"CacheData(name:" + name + ", key:" + key + ") is unserializable to JSON", e);
			return data;
		}

		// 3.2 Save Cache
		try {
			Instant expiredAt = Instant.now().plus(Duration.ofDays(30));
			cache = new CacheData();
			cache.setCacheName(name);
			cache.setCacheKey(key);
			cache.setContent(content);
			cache.setLastUsedAt(now);
			cache.setExpiredAt(expiredAt);
			cache.setCreatedAt(now);
			BeanUtils.get(CacheDataRepository.class).save(cache);
		} catch (Exception e) {
			QCacheData table = QCacheData.cacheData;
			if (!BeanUtils.get(CacheDataRepository.class).exists(new BooleanBuilder().and(table.cacheName.eq(name)).and(table.cacheKey.eq(key)))) {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "CACHE_DATA_INSERT_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "Failed to save CacheData(name:" + name + ", key:" + key + ")",
						e);
			}
			return data;
		}

		return data;
	}

	public static boolean exists(String name) {
		QCacheData table = QCacheData.cacheData;
		return BeanUtils.get(CacheDataRepository.class).exists(new BooleanBuilder().and(table.cacheName.eq(name)));
	}

	public static boolean exists(String name, String key) {
		QCacheData table = QCacheData.cacheData;
		return BeanUtils.get(CacheDataRepository.class).exists(new BooleanBuilder().and(table.cacheName.eq(name)).and(table.cacheKey.eq(key)));
	}

	public static void update(String name, String key) {
		getValid(name, key, null, null, null);
	}

	public static <T> T get(String name, String key, Class<T> returnType) {
		LogicUtils.assertNotEmpty(returnType, "returnType");

		CacheData cache = getValid(name, key, null, null, null);

		String content = cache == null ? null : cache.getContent();
		if (content == null) {
			return null;
		}

		try {
			T data = ValueUtils.isPrimitiveType(returnType) ? //
					ValueUtils.toRequiredType(content, returnType) //
					: MAPPER.readValue(content, returnType);
			return data;
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "CACHE_DATA_UNDESERIALIZABLE", HttpStatus.SERVICE_UNAVAILABLE,
					"CacheData(name:" + name + ", key:" + key + ", id:" + cache.getId() + ").content is undeserializable to this Class(" + returnType.getName() + ")", e);
			return null;
		}
	}

	public static void create(String name, String key, String content) {
		Instant now = DateUtils2.newInstant();
		CacheData cache = new CacheData();
		cache.setCacheName(name);
		cache.setCacheKey(key);
		cache.setContent(content);
		cache.setLastUsedAt(now);
		cache.setExpiredAt(null);
		cache.setCreatedAt(now);
		BeanUtils.get(CacheDataRepository.class).save(cache);
	}

	public static void save(String name, String key, Object data) {
		// 1. Serialize to JSON
		String content;
		try {
			if (data == null) {
				content = null;
			} else if (ValueUtils.isPrimitiveType(data)) {
				content = ValueUtils.toString(data);
			} else {
				content = data == null ? null : MAPPER.writeValueAsString(data);
			}
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "CACHE_DATA_UNSERIALIZABLE", HttpStatus.SERVICE_UNAVAILABLE,
					"CacheData(name:" + name + ", key:" + key + ") is unserializable to JSON", e);
			return;
		}

		DateUtils2.newInstant();

		// 2. Get Cache and if exists valid one, Update 
		CacheData cache = getValid(name, key, null, null, content);
		if (cache != null) {
			return;
		}

		// 3. Create if not exists
		create(name, key, content);

		// Remove Old Logic
//		Instant now = Instant.now();
//
//		QCacheData table = QCacheData.cacheData;
//		CacheData cache = BeanUtils.get(CacheDataRepository.class).findOne(new BooleanBuilder().and(table.cacheName.eq(name)).and(table.cacheKey.eq(key))).orElse(null);
//		if (cache == null) {
//			_create(name, key, content, now);
//			return;
//		}
//
//		cache.setContent(content);
//		cache.setLastUsedAt(now);
//		BeanUtils.get(CacheDataRepository.class).save(cache);
	}

	public static int delete(String name, String key) {
		return BeanUtils.get(CacheDataRepository.class).deleteByCacheNameAndCacheKey(name, key);
	}

	private static <T> CacheData getValid(String name, String key, CacheDataOptions options, Instant dataLastUpdatedAt, String newContent) {
		QCacheData table = QCacheData.cacheData;
		CacheData cache = BeanUtils.get(CacheDataRepository.class).findOne(new BooleanBuilder().and(table.cacheName.eq(name)).and(table.cacheKey.eq(key))).orElse(null);

		if (cache == null) {
			return null;
		}

		Instant now = DateUtils2.getInstant();

		// Check Expired Time
		if (cache.getExpiredAt() != null && cache.getExpiredAt().isBefore(now)) {
			BeanUtils.get(CacheDataRepository.class).delete(cache);
			return null;
		}

		if (options == null) {
			options = DEFAULT_OPTIONS;
		}

		// Check Renewal Type
		if (options.renewalType != null) {
			boolean valid = true;

			DateTimeFormatter formatter;
			switch (options.renewalType) {
			case HOURLY:
				formatter = DateTimeFormatter.ofPattern("yyyyMMddHH").withZone(ZoneId.systemDefault());
				valid = formatter.format(now).equals(formatter.format(cache.getCreatedAt()));
				break;
			case DAILY:
				formatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());
				valid = formatter.format(now).equals(formatter.format(cache.getCreatedAt()));
				break;
			case MONTHLY:
				formatter = DateTimeFormatter.ofPattern("yyyyMM").withZone(ZoneId.systemDefault());
				valid = formatter.format(now).equals(formatter.format(cache.getCreatedAt()));
				break;
			case WEEKLY:
				Calendar cal1 = Calendar.getInstance();
				cal1.setTimeInMillis(now.toEpochMilli());
				Calendar cal2 = Calendar.getInstance();
				cal2.setTimeInMillis(cache.getCreatedAt().toEpochMilli());
				valid = cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
				break;
			case YEARLY:
				formatter = DateTimeFormatter.ofPattern("yyyy").withZone(ZoneId.systemDefault());
				valid = formatter.format(now).equals(formatter.format(cache.getCreatedAt()));
				break;
			}

			if (!valid) {
				BeanUtils.get(CacheDataRepository.class).delete(cache);
				return null;
			}
		}

		// Check Last Updated Data Time
		if (dataLastUpdatedAt != null && dataLastUpdatedAt.isAfter(cache.getCreatedAt())) {
			BeanUtils.get(CacheDataRepository.class).delete(cache);
			return null;
		}

		// Return Cache
		try {
			cache.setLastUsedAt(now);
			if (newContent != null) {
				cache.setContent(newContent);
			}
			BeanUtils.get(CacheDataRepository.class).save(cache);
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(ErrorType.SYS, "CACHE_DATA_UPDATE_ERROR", HttpStatus.SERVICE_UNAVAILABLE, "Failed to save CacheData(name:" + name + ", key:" + key + ")", e);
		}
		return cache;
	}

	public static void cleanOldBatch() {
		QCacheData table = QCacheData.cacheData;
		TranUtils.doTran(() -> BeanUtils.get(JPAQueryFactory.class).delete(table).where(new BooleanBuilder().and(table.expiredAt.lt(Instant.now()))).execute());
	}

}
