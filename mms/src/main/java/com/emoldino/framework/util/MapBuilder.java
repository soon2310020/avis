package com.emoldino.framework.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapBuilder<K, V> {
	private Map<K, V> map;
	private boolean immutable;

	public MapBuilder() {
		this(false);
	}

	public MapBuilder(boolean immutable) {
		this.immutable = immutable;
		this.map = new LinkedHashMap<>();
	}

	public MapBuilder<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}

	public Map<K, V> build() {
		if (immutable) {
			return Collections.unmodifiableMap(map);
		}
		return map;
	}
}
