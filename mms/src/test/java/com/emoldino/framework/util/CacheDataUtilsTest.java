package com.emoldino.framework.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import saleson.MmsApplication;

@SpringBootTest(classes = MmsApplication.class)
@ActiveProfiles("test")
@Slf4j
public class CacheDataUtilsTest {
	@AllArgsConstructor
	private static class CacheNameKeyPair {
		String name;
		String key;
	}

	private static List<CacheNameKeyPair> LIST = Arrays.asList(new CacheNameKeyPair("TEST", "get1"));

	@BeforeAll
	static void beforeClass() throws Exception {
		LIST.forEach(item -> {
			try {
				TranUtils.doNewTran(() -> CacheDataUtils.delete(item.name, item.key));
			} catch (Exception e) {
				// Skip
				log.warn(e.getMessage());
			}
		});
	}

	@AfterAll
	static void afterClass() throws Exception {
		LIST.forEach(item -> {
			try {
				TranUtils.doNewTran(() -> CacheDataUtils.delete(item.name, item.key));
			} catch (Exception e) {
				// Skip
				log.warn(e.getMessage());
			}
		});
	}

	@Test
	public void get1() {
		CacheNameKeyPair namekey = new CacheNameKeyPair("TEST", "get1");

		Assertions.assertNull(CacheDataUtils.get(namekey.name, namekey.key, Map.class));

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("a", "x");
		data.put("b", "y");
		CacheDataUtils.save(namekey.name, namekey.key, data);

		@SuppressWarnings("unchecked")
		Map<String, Object> cache = CacheDataUtils.get(namekey.name, namekey.key, Map.class);
		Assertions.assertNotNull(CacheDataUtils.get(namekey.name, namekey.key, Map.class));

		Assertions.assertEquals(cache.get("a"), "x");
		Assertions.assertEquals(cache.get("b"), "y");

	}

}
