package com.emoldino.framework.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.emoldino.framework.repository.syncctrldata.QSyncCtrlData;
import com.emoldino.framework.repository.syncctrldata.SyncCtrlData;
import com.emoldino.framework.repository.syncctrldata.SyncCtrlDataRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncCtrlUtils {

	public static void lock(String name) {
		LogicUtils.assertNotEmpty(name, "name");
		SyncCtrlDataRepository repo = BeanUtils.get(SyncCtrlDataRepository.class);

		wrap("SyncCtrlUtils.lock." + name, () -> {
			SyncCtrlData lock = repo.findWithPessimisticLockByName(name).orElse(null);
			if (lock == null) {
				lock = new SyncCtrlData();
				lock.setName(name);
			}
			repo.save(lock);
		});
	}

	public static void cleanOldDataBatch() {
		QSyncCtrlData table = QSyncCtrlData.syncCtrlData;
		TranUtils.doTran(() -> BeanUtils.get(JPAQueryFactory.class)//
				.delete(table)//
				.where(new BooleanBuilder().and(table.updatedAt.lt(Instant.now().minus(Duration.ofDays(7)))))//
				.execute());
	}

	public static <V> V wrapWithLock(String name, Closure<V> closure) {
		return wrapWithLock(name, false, closure);
	}

	public static void wrapWithLock(String name, ClosureNoReturn closure) {
		wrapWithLock(name, false, closure);
	}

	public static <V> V wrapWithLock(String name, boolean newTran, Closure<V> closure) {
		if (newTran) {
			return TranUtils.doNewTran(() -> {
				lock(name);
				return closure.execute();
			});
		} else {
			return TranUtils.doTran(() -> {
				lock(name);
				return closure.execute();
			});
		}
	}

	public static void wrapWithLock(String name, boolean newTran, ClosureNoReturn closure) {
		wrapWithLock(name, newTran, () -> {
			closure.execute();
			return null;
		});
	}

	public static <V> V wrap(String name, Closure<V> closure) {
		synchronized (get(name)) {
			try {
				return closure.execute();
			} finally {
				release(name);
			}
		}
	}

	public static void wrap(String name, ClosureNoReturn closure) {
		synchronized (get(name)) {
			try {
				closure.execute();
			} finally {
				release(name);
			}
		}
	}

	public static <K, V> V wrap(final String name, final Map<K, V> cache, final K key, final Closure<V> closure) {
		LogicUtils.assertNotEmpty(name, "name");
		LogicUtils.assertNotNull(cache, "cache");

		final boolean debug = log.isDebugEnabled();

		if (key == null) {
			if (debug) {
				log.debug("key is null, so execute closure right away.");
			}
			return (V) wrap(name, closure);
		}

		if (cache.containsKey(key)) {
			if (debug) {
				log.debug("get data from map cache by key: " + key);
			}
			return cache.get(key);
		}

		return wrap(name, () -> {
			V value = CacheUtils.get(cache, key, closure);
			return value;
		});
	}

	public static <K, V> V wrapNullable(final String name, final Map<K, Optional<V>> cache, final K key, final Closure<V> closure) {
		LogicUtils.assertNotEmpty(name, "name");
		LogicUtils.assertNotNull(cache, "cache");

		final boolean debug = log.isDebugEnabled();

		if (key == null) {
			if (debug) {
				log.debug("key is null, so execute closure right away.");
			}
			return (V) wrap(name, closure);
		}

		if (cache.containsKey(key)) {
			if (debug) {
				log.debug("get data from map cache by key: " + key);
			}
			return cache.get(key).orElse(null);
		}

		return wrap(name, () -> {
			V value = CacheUtils.getNullable(cache, key, closure);
			return value;
		});
	}

	private static Map<String, Monitor> cache = new ConcurrentHashMap<>();

	private static Object get(String name) {
		LogicUtils.assertNotEmpty(name, "name");

		final boolean debug = log.isDebugEnabled();

		synchronized (cache) {
			Monitor monitor = null;
			try {
				if (cache.containsKey(name)) {
					monitor = cache.get(name);
					monitor.i++;
				} else {
					monitor = Monitor.newInstance();
					cache.put(name, monitor);
				}
			} finally {
				if (debug) {
					log.debug("get monitor name: " + name + ", index: " + (monitor == null ? 0 : monitor.i));
				}
			}
			return monitor;
		}
	}

	private static void release(String name) {
		LogicUtils.assertNotEmpty(name, "name");

		final boolean debug = log.isDebugEnabled();

		synchronized (cache) {
			if (!cache.containsKey(name)) {
				if (debug) {
					log.debug("the monitor was not released because there was not any monitor with name: " + name);
				}
				return;
			}

			Monitor monitor = cache.get(name);
			if (monitor.i > 0) {
				if (debug) {
					log.debug("release monitor name: " + name + ", index: " + monitor.i);
				}
				monitor.i--;
				return;
			}

			if (debug) {
				log.debug("remove monitor name: " + name + ", index: " + monitor.i);
			}
			cache.remove(name);
		}
	}

	private static class Monitor {
		static Monitor newInstance() {
			return new Monitor();
		}

		int i = 0;
	}
}
