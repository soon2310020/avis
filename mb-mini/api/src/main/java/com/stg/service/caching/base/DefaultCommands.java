package com.stg.service.caching.base;

import com.stg.config.jackson.Jackson;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DefaultCommands {
    Jackson JACKSON = Jackson.newInstance();
    String EXEC_RESPONSE = "OK";
    long TTL_DEFAULT = 21600; // 6h


    /***/
    String get(String key);

    default <T> T get(String key, Class<T> clazz) {
        String value = this.get(key);
        if (value == null) return null;

        return JACKSON.fromJson(value, clazz);
    }

    default <T> List<T> getList(String key, Class<T> clazz) {
        String value = this.get(key);
        if (value == null) return Collections.emptyList();

        return JACKSON.fromJsonList(value, clazz);
    }

    /***/
    boolean set(String key, String value);

    default boolean set(String key, Object value) {
        return set(key, value == null ? null : JACKSON.toJson(value));
    }

    /***/
    boolean set(String key, String value, long ttlSecond);

    default boolean set(String key, Object value, long ttlSecond) {
        return set(key, value == null ? null : JACKSON.toJson(value), ttlSecond);
    }

    /***/
    boolean delKeyPattern(String keyPattern);

    /***/
    boolean existByPattern(String keyPattern);

    /***/
    boolean mset(@NonNull Map<String, String> valueMap);

    default boolean mset2(@NonNull Map<String, Object> valueMap) {
        Map<String, String> data = new HashMap<>();
        valueMap.forEach((k, value) -> data.put(k, value == null ? null : JACKSON.toJson(value)));
        return mset(data);
    }

    default boolean mset(@NonNull List<? extends ValueKey> valueKeys) {
        Map<String, String> data = new HashMap<>();
        valueKeys.forEach(el -> data.put(el.key(), JACKSON.toJson(el)));
        return mset(data);
    }
}
