package com.emoldino.framework.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.exception.LogicException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;

import saleson.common.util.SecurityUtils;

public class DataUtils {

	public static <T> void runEach(//
			Class<? extends QuerydslPredicateExecutor<T>> repoClass, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<T> closure//
	) {
		runEach(BeanUtils.get(repoClass), filter, sort, batchSize, pageUpRequired, closure);
	}

	public static <T> void runEach(//
			QuerydslPredicateExecutor<T> repo, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<T> closure//
	) {
		int counter = 0;
		Page<T> page;
		int[] i = { 0 };
		while (counter++ < 100000 && //
				!(page = repo.findAll(//
						(filter == null ? new BooleanBuilder() : filter), //
						PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize, sort)//
				)).isEmpty()//
		) {
			page.forEach(closure::execute);
		}
	}

	public static <T> void runBatch(//
			Class<? extends QuerydslPredicateExecutor<T>> repoClass, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<T> closure//
	) {
		runBatch(BeanUtils.get(repoClass), filter, sort, batchSize, pageUpRequired, closure);
	}

	public static <T> void runBatchWhenHasNext(//
			Class<? extends QuerydslPredicateExecutor<T>> repoClass, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1Param<T, Boolean> closure//
	) {
		runBatchWhenHasNext(BeanUtils.get(repoClass), filter, sort, batchSize, pageUpRequired, closure);
	}

	public static <T> void runBatch(//
			QuerydslPredicateExecutor<T> repo, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<T> closure//
	) {
		int counter = 0;
		Page<T> page;
		int[] i = { 0 };
		while (counter++ < 100000 && //
				!(page = TranUtils.doNewTran(() -> repo.findAll(//
						(filter == null ? new BooleanBuilder() : filter), //
						PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize, sort)//
				))).isEmpty()//
		) {
			page.forEach(closure::execute);
		}
	}

	public static <T> void runBatchWhenHasNext(//
			QuerydslPredicateExecutor<T> repo, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1Param<T, Boolean> closure//
	) {
		boolean stop = false;
		int counter = 0;
		Page<T> page;
		int[] i = { 0 };
		while (counter++ < 100000 && //
				!(page = TranUtils.doNewTran(() -> repo.findAll(//
						(filter == null ? new BooleanBuilder() : filter), //
						PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize, sort)//
				))).isEmpty()//
		) {
			for (T item : page.getContent()) {
				stop = !ValueUtils.toBoolean(closure.execute(item), true);
				if (stop) {
					return;
				}
			}
		}
	}

	public static <T> void runContentBatch(//
			Class<? extends QuerydslPredicateExecutor<T>> repoClass, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<List<T>> closure//
	) {
		runContentBatch(BeanUtils.get(repoClass), filter, sort, batchSize, pageUpRequired, closure);
	}

	public static <T> void runContentBatch(//
			QuerydslPredicateExecutor<T> repo, //
			Predicate filter, Sort sort, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<List<T>> closure//
	) {
		int counter = 0;
		Page<T> page;
		int[] i = { 0 };
		while (counter++ < 100000 && //
				!(page = TranUtils.doNewTran(() -> repo.findAll(//
						(filter == null ? new BooleanBuilder() : filter), //
						PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize, sort)//
				))).isEmpty()//
		) {
			closure.execute(page.getContent());
		}
	}

	public static <T> void runBatch(JPQLQuery<T> query, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<T> closure//
	) {
		int counter = 0;
		List<T> content;
		int[] i = { 0 };

		while (counter++ < 100000 && //
				!(content = TranUtils.doNewTran(() -> {
					QueryUtils.applyPagination(query, PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize));
					return query.fetch();
				})).isEmpty()//
		) {
			content.forEach(closure::execute);
		}
	}

	public static <T> void runBatchWhenHasNext(JPQLQuery<T> query, //
			int batchSize, boolean pageUpRequired, //
			Closure1Param<T, Boolean> closure//
	) {
		int counter = 0;
		List<T> content;
		int[] i = { 0 };

		while (counter++ < 100000 && //
				!(content = TranUtils.doNewTran(() -> {
					QueryUtils.applyPagination(query, PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize));
					return query.fetch();
				})).isEmpty()//
		) {
			boolean stop = false;
			for (T item : content) {
				stop = !ValueUtils.toBoolean(closure.execute(item), true);
				if (stop) {
					return;
				}
			}
		}
	}

	public static <T> void runContentBatch(JPQLQuery<T> query, //
			int batchSize, boolean pageUpRequired, //
			Closure1ParamNoReturn<List<T>> closure//
	) {
		int counter = 0;
		List<T> content;
		int[] i = { 0 };
		while (counter++ < 100000 && //
				!(content = TranUtils.doNewTran(() -> {
					QueryUtils.applyPagination(query, PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize));
					return query.fetch();
				})).isEmpty()//
		) {
			closure.execute(content);
		}
	}

	public static <T> void runContentBatchWhenHasNext(JPQLQuery<T> query, //
			int batchSize, boolean pageUpRequired, //
			Closure1Param<List<T>, Boolean> closure//
	) {
		int counter = 0;
		List<T> content;
		int[] i = { 0 };
		while (counter++ < 100000 && //
				!(content = TranUtils.doNewTran(() -> {
					QueryUtils.applyPagination(query, PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize));
					return query.fetch();
				})).isEmpty()//
		) {
			boolean stop = false;
			stop = !ValueUtils.toBoolean(closure.execute(content), true);
			if (stop) {
				return;
			}
		}
	}

	public static <T> void runBatch(//
			Sort sort, int batchSize, boolean pageUpRequired, //
			Closure1Param<Pageable, Page<T>> findAll, //
			Closure1ParamNoReturn<T> closure) {
		int counter = 0;
		Page<T> page;
		int[] i = { 0 };
		while (counter++ < 10000 //
				&& !(page = TranUtils.doNewTran(() -> findAll.execute(//
						PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize, sort)//
				))).isEmpty()) {
			page.forEach(closure::execute);
		}
	}

	public static <T> void runContentBatch(//
			Sort sort, int batchSize, boolean pageUpRequired, //
			Closure1Param<Pageable, Page<T>> findAll, //
			Closure1ParamNoReturn<List<T>> closure) {
		int counter = 0;
		Page<T> page;
		int[] i = { 0 };
		while (counter++ < 10000 //
				&& !(page = TranUtils.doNewTran(() -> findAll.execute(//
						PageRequest.of((pageUpRequired ? i[0]++ : 0), batchSize, sort)//
				))).isEmpty()) {
			closure.execute(page.getContent());
		}
	}

	private static class Meta {
		Method setCreatedBy;
		Method setCreatedAt;
		Method setUpdatedBy;
		Method setUpdatedAt;
		Method getEnabled;
	}

	private static final Map<Class<?>, Meta> META = new ConcurrentHashMap<>();

	private static Meta getMeta(Class<?> clazz) {
		if (clazz == null) {
			return null;
		}
		return CacheUtils.get(META, clazz, () -> {
			Meta meta = new Meta();
			meta.setCreatedBy = getSetter(clazz, "setCreatedBy", Long.class);
			meta.setCreatedAt = getSetter(clazz, "setCreatedAt", Instant.class);
			meta.setUpdatedBy = getSetter(clazz, "setUpdatedBy", Long.class);
			meta.setUpdatedAt = getSetter(clazz, "setUpdatedAt", Instant.class);
			meta.getEnabled = ReflectionUtils.getGetter(clazz, "enabled");
			return meta;
		});
	}

	// TODO Replace with ReflectionUtils.getSetter
	private static Method getSetter(Class<?> clazz, String name, Class<?> type) {
		try {
			return clazz.getMethod(name, type);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	@Deprecated
	public static void populate4Insert(Object data) {
		if (data == null) {
			return;
		}
		Class<?> clazz = data.getClass();
		Meta meta = getMeta(clazz);
		if (meta == null) {
			return;
		}

		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			userId = SecurityUtils.getSupportUserId();
		}
		Instant now = DateUtils2.newInstant();
		try {
			if (meta.setCreatedBy != null) {
				meta.setCreatedBy.invoke(data, userId);
			}
			if (meta.setCreatedAt != null) {
				meta.setCreatedAt.invoke(data, now);
			}
			if (meta.setUpdatedBy != null) {
				meta.setUpdatedBy.invoke(data, userId);
			}
			if (meta.setUpdatedAt != null) {
				meta.setUpdatedAt.invoke(data, now);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new LogicException("REFLECTION_BUG", e);
		}
	}

	@Deprecated
	public static void populate4Update(Object data) {
		if (data == null) {
			return;
		}
		Class<?> clazz = data.getClass();
		Meta meta = getMeta(clazz);
		if (meta == null) {
			return;
		}

		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			userId = SecurityUtils.getSupportUserId();
		}
		Instant now = DateUtils2.newInstant();
		try {
			if (meta.setUpdatedBy != null) {
				meta.setUpdatedBy.invoke(data, userId);
			}
			if (meta.setUpdatedAt != null) {
				meta.setUpdatedAt.invoke(data, now);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new LogicException("REFLECTION_BUG", e);
		}
	}

	public static void check(Object data, String field, Object value) {
		if (data == null) {
			return;
		}
		Class<?> clazz = data.getClass();
		Meta meta = getMeta(clazz);
		if (meta == null) {
			return;
		}

		try {
			if (meta.getEnabled != null) {
				Boolean enabled = (Boolean) meta.getEnabled.invoke(data);
				if (enabled != null && !enabled) {
					throw newDataDisabledException(clazz, field, value);
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new LogicException("REFLECTION_BUG", e);
		}
	}

	@Deprecated
	public static BizException newDataNotFoundException(Class<?> model, Object id) {
		return newDataNotFoundException(model, "id", id);
	}

	public static BizException newDataNotFoundException(Class<?> model, String field, Object value) {
		LogicUtils.assertNotNull(model, "model");
		return newDataNotFoundException(model.getSimpleName(), field, value);
	}

	public static BizException newDataNotFoundException(String model, String field, Object value) {
		LogicUtils.assertNotNull(model, "model");
		return new BizException("DATA_NOT_FOUND", model + " " + field + ": " + value + " is not found!!", //
				new Property("model", MessageUtils.get(model, null)), //
				new Property("field", MessageUtils.get(field, null)), //
				new Property("value", value));
	}

	public static BizException newDataDisabledException(Class<?> model, String field, Object value) {
		LogicUtils.assertNotNull(model, "model");
		return newDataDisabledException(model.getSimpleName(), field, value);
	}

	public static BizException newDataDisabledException(String model, String field, Object value) {
		return new BizException("DATA_DISABLED", model + " " + field + ": " + value + " is disabled!!", //
				new Property("model", MessageUtils.get(model, null)), //
				new Property("field", MessageUtils.get(field, null)), //
				new Property("value", value));
	}

	public static BizException newDataValueInvalidException(Class<?> model, String field, Object value) {
		LogicUtils.assertNotNull(model, "entity");
		return newDataValueInvalidException(model.getSimpleName(), field, value);
	}

	public static BizException newDataValueInvalidException(String model, String field, Object value) {
		LogicUtils.assertNotNull(model, "entity");
		return new BizException("DATA_VALUE_INVALID", model + " " + field + ": " + value + " is invalid!!", //
				new Property("model", MessageUtils.get(model, null)), //
				new Property("field", MessageUtils.get(field, null)), //
				new Property("value", value));
	}

}
