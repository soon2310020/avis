package com.emoldino.framework.util;

import java.util.Iterator;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import com.emoldino.framework.repository.draftdata.DraftData;
import com.emoldino.framework.repository.draftdata.DraftDataRepository;
import com.emoldino.framework.repository.draftdata.QDraftData;
import com.querydsl.core.BooleanBuilder;

import saleson.common.util.SecurityUtils;

public class DraftUtils {

	/**
	 * Checks existence of Any Draft of this Resource
	 * @param resourceType
	 * @param resourceId
	 * @return
	 */
	public static boolean existsAny(String resourceType, Object resourceId) {
		BooleanBuilder filter = newFitler(resourceType, resourceId, null);
		boolean exists = BeanUtils.get(DraftDataRepository.class).exists(filter);
		return exists;
	}

	/**
	 * Gets Any of the Most Recent Draft of this Resource
	 * @param <T>
	 * @param resourceType
	 * @param resourceId
	 * @param requiredType
	 * @return
	 */
	public static <T> T getAny(String resourceType, Object resourceId, Class<T> requiredType) {
		DraftData draft = findOne(resourceType, resourceId, null);
		return draft == null ? null : ValueUtils.fromJsonStr(draft.getContent(), requiredType);
	}

	/**
	 * Gets My Draft of this Resource
	 * @param <T>
	 * @param resourceType
	 * @param resourceId
	 * @param requiredType
	 * @return
	 */
	public static <T> T get(String resourceType, Object resourceId, Class<T> requiredType) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return null;
		}
		DraftData draft = findOne(resourceType, resourceId, userId);
		return draft == null ? null : ValueUtils.fromJsonStr(draft.getContent(), requiredType);
	}

	/**
	 * Saves My Draft of this Resource
	 * @param resourceType
	 * @param resourceId
	 * @param data
	 */
	public static void save(String resourceType, Object resourceId, Object data) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}

		if (data == null) {
			delete(resourceType, resourceId);
			return;
		}

		LogicUtils.assertNotEmpty(resourceType, "resourceType");
		LogicUtils.assertNotEmpty(resourceId, "resourceId");
		String content = ValueUtils.isPrimitiveType(data) ? ValueUtils.toString(data) : ValueUtils.toJsonStr(data);

		DraftData draft = findOne(resourceType, resourceId, userId);
		if (draft == null) {
			draft = new DraftData();
			draft.setResourceType(resourceType);
			draft.setResourceId(ValueUtils.toString(resourceId));
			draft.setUserId(userId);
		}
		draft.setContent(content);
		BeanUtils.get(DraftDataRepository.class).save(draft);
	}

	/**
	 * Deletes My Draft of this Resource
	 * @param resourceType
	 * @param resourceId
	 * @param data
	 */
	public static void delete(String resourceType, Object resourceId) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}
		deleteAll(resourceType, resourceId, userId);
	}

	/**
	 * Deletes All (Users') Draft of this Resource
	 * @param resourceType
	 * @param resourceId
	 */
	public static void deleteAll(String resourceType, Object resourceId) {
		deleteAll(resourceType, resourceId, null);
	}

	private static void deleteAll(String resourceType, Object resourceId, Long userId) {
		Iterable<DraftData> drafts = findAll(resourceType, resourceId, userId, null);
		BeanUtils.get(DraftDataRepository.class).deleteAll(drafts);
	}

	private static DraftData findOne(String resourceType, Object resourceId, Long userId) {
		Iterable<DraftData> drafts = findAll(resourceType, resourceId, userId, PageRequest.of(0, 1, Direction.DESC, "id"));
		Iterator<DraftData> itr = drafts.iterator();
		DraftData draft = itr.hasNext() ? itr.next() : null;
		return draft;
	}

	private static Iterable<DraftData> findAll(String resourceType, Object resourceId, Long userId, Pageable pageable) {
		BooleanBuilder filter = newFitler(resourceType, resourceId, userId);
		Iterable<DraftData> drafts = pageable == null ? //
				BeanUtils.get(DraftDataRepository.class).findAll(filter)//
				: BeanUtils.get(DraftDataRepository.class).findAll(filter, pageable);
		return drafts;
	}

	private static BooleanBuilder newFitler(String resourceType, Object resourceId, Long userId) {
		LogicUtils.assertNotEmpty(resourceType, "resourceType");
		LogicUtils.assertNotEmpty(resourceId, "resourceId");
		QDraftData table = QDraftData.draftData;
		BooleanBuilder filter = new BooleanBuilder()//
				.and(table.resourceType.eq(resourceType))//
				.and(table.resourceId.eq(ValueUtils.toString(resourceId)));//
		if (userId != null) {
			filter.and(table.userId.eq(userId));
		}
		return filter;
	}

}
