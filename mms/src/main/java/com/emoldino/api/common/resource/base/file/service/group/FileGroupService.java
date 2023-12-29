package com.emoldino.api.common.resource.base.file.service.group;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.file.dto.FileGroupGetIn;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroup;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroupRepository;
import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItem;
import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItemRepository;
import com.emoldino.api.common.resource.base.file.util.FileRelationUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

@Service
public class FileGroupService {
	@Autowired
	FileGroupRepository repo;

	@Transactional
	public Page<FileGroup> get(FileGroupGetIn input, Pageable pageable) {
		// Make Filter
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Qs.fileGroup.enabled, input.getEnabled());
		if (input.isManagedTypeOnly()) {
			filter.and(Qs.fileGroup.fileGroupType.ne(FileGroupType.UNMANAGED));
		}
		QueryUtils.eq(filter, Qs.fileGroup.fileGroupType, input.getFileGroupType());
		QueryUtils.eq(filter, Qs.fileGroup.fileGroupCode, input.getFileGroupCode());
		QueryUtils.in(filter, Qs.fileGroup.fileGroupStatus, input.getFileGroupStatus());
		if (!StringUtils.isEmpty(input.getQuery())) {
			String query = StringUtils.trimWhitespace(input.getQuery());
			filter.and(//
					Qs.fileGroup.fileGroupKey.containsIgnoreCase(query)//
							.or(Qs.fileGroup.version.containsIgnoreCase(query))//
							.or(Qs.fileGroup.fileGroupCode.containsIgnoreCase(query))//
							.or(Qs.fileGroup.fileGroupName.containsIgnoreCase(query))//
							.or(Qs.fileGroup.description.containsIgnoreCase(query))//
			);
		}

		QueryUtils.applySortDefault(pageable, Direction.DESC, "createdAt");

		Page<FileGroup> page;
		if (!input.isManagedTypeOnly()) {
			page = repo.findAll(filter, pageable);
		} else {
			page = repo.findAllDistinctFileGroupTypeAndFileGroupCode(filter, pageable);
		}
		return page;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Long post(FileGroup data) {
		LogicUtils.assertNotNull(data, "data");
		LogicUtils.assertNotNull(data.getFileGroupType(), "fileGroupType");
		LogicUtils.assertNotNull(data.getFileGroupCode(), "fileGroupCode");

		if (data.getId() != null && repo.existsById(data.getId())) {
			throw new BizException("DATA_ALREADY_EXIST", "AppVersion id:" + data.getId() + " is not found!!", new Property("model", "AppVersion"),
					new Property("id", data.getId()));
		}

		FileGroup fileGroup;
		{
			String fileGroupKey = FileRelationUtils.toFileGroupKey(//
					"MNG", //
					data.getFileGroupType() == null ? null : data.getFileGroupType().name(), //
					data.getFileGroupCode());
			Page<FileGroup> page = BeanUtils.get(FileGroupRepository.class).findAll(//
					new BooleanBuilder().and(Qs.fileGroup.fileGroupKey.eq(fileGroupKey)), //
					PageRequest.of(0, 1, Direction.DESC, "id"));
			if (page.isEmpty()) {
				throw DataUtils.newDataNotFoundException(FileGroup.class, "file_group_key", fileGroupKey);
			}
			fileGroup = page.getContent().get(0);
			LogicUtils.assertNotNull(fileGroup, "fileGroup");
		}

		if (!FileGroupStatus.UNMANAGED.equals(fileGroup.getFileGroupStatus())//
				|| !FileGroupType.UNMANAGED.equals(fileGroup.getFileGroupType())//
				|| !fileGroup.isEnabled()) {
			throw new BizException("DATA_ALREADY_EXIST", "AppVersion id:" + data.getId() + " is not found!!", new Property("model", "AppVersion"),
					new Property("id", data.getId()));
		}

		fileGroup.setFileGroupStatus(FileGroupStatus.UNRELEASED);
		fileGroup.setFileGroupType(data.getFileGroupType());
		fileGroup.setFileGroupCode(data.getFileGroupCode());
		fileGroup.setFileGroupName(data.getFileGroupName());
		fileGroup.setDescription(data.getDescription());
		repo.save(fileGroup);

		findAllItems(fileGroup).forEach(item -> {
			boolean changed = false;
			if (!ValueUtils.equals(fileGroup.getFileGroupCode(), fileGroup.getFileGroupKey())) {
				if (ValueUtils.equals(item.getFileNo(), fileGroup.getFileGroupKey())) {
					item.setFileNo(data.getFileGroupCode());
					changed = true;
				}
				if (item.getFileName().equals(fileGroup.getFileGroupKey())) {
					item.setFileName(data.getFileGroupCode());
					changed = true;
				}
				if (!item.getFileType().equals(fileGroup.getFileGroupType())) {
					item.setFileType(data.getFileGroupType().name());
					changed = true;
				}
			}
			if (changed) {
				BeanUtils.get(FileItemRepository.class).save(item);
			}
		});

		return fileGroup.getId();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void put(Long id, FileGroup data) {
		Assert.notNull(id, "id is required!!");
		Assert.notNull(data, "data is required!!");

		FileGroup fileGroup = findById(id);
		if (!ValueUtils.equals(fileGroup.getFileGroupType(), data.getFileGroupType())) {
			new BizException("UNCHANGEABLE_FIELD", new Property("field", "fileGroupType"), new Property("value", data.getFileGroupType()));
		} else if (!ValueUtils.equals(fileGroup.getFileGroupCode(), data.getFileGroupCode())) {
			new BizException("UNCHANGEABLE_FIELD", new Property("field", "fileGroupCode"), new Property("value", data.getFileGroupCode()));
		} else if (!ValueUtils.equals(fileGroup.getVersion(), data.getVersion())) {
			new BizException("UNCHANGEABLE_FIELD", new Property("field", "version"), new Property("value", data.getVersion()));
		}

		fileGroup.setFileGroupName(data.getFileGroupName());
		fileGroup.setDescription(data.getDescription());
		repo.save(fileGroup);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void delete(Long id) {
		Assert.notNull(id, "id is required!!");

		FileGroup fileGroup = findById(id);
		if (FileGroupStatus.RELEASED.equals(fileGroup.getFileGroupStatus())) {
			throw new BizException("FILE_GROUP_RELEASED");
		}

		List<FileItem> items = TranUtils.doNewTran(() -> findAllItems(fileGroup));

		FileRelationUtils.removeList(items);

		repo.delete(fileGroup);
	}

	@Transactional
	public void disable(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}
		for (Long id : ids) {
			List<FileGroup> list = getVersions(id);
			list.forEach(item -> item.setEnabled(false));
			repo.saveAll(list);
		}
	}

	@Transactional
	public void enable(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}
		for (Long id : ids) {
			List<FileGroup> list = getVersions(id);
			list.forEach(item -> item.setEnabled(true));
			repo.saveAll(list);
		}
	}

	@Transactional
	public void release(Long id) {
		Assert.notNull(id, "id is required!!");
		FileGroup fileGroup = findById(id);
		if (!FileGroupStatus.UNRELEASED.equals(fileGroup.getFileGroupStatus())) {
			throw new BizException("INVALID_STATUS", new Property("status", fileGroup.getFileGroupStatus()));
		}
		fileGroup.setReleasedAt(Instant.now());
		fileGroup.setFileGroupStatus(FileGroupStatus.RELEASED);
		repo.save(fileGroup);
	}

	@Transactional
	public void unrelease(Long id) {
		Assert.notNull(id, "id is required!!");
		FileGroup fileGroup = findById(id);
		if (!FileGroupStatus.RELEASED.equals(fileGroup.getFileGroupStatus())) {
			throw new BizException("INVALID_STATUS", new Property("status", fileGroup.getFileGroupStatus()));
		}
		fileGroup.setFileGroupStatus(FileGroupStatus.UNRELEASED);
		repo.save(fileGroup);
	}

	@Transactional
	public void putVersion(Long id, String version) {
		LogicUtils.assertNotEmpty(version, "version");
		FileGroup fileGroup = findById(id);
		FileGroup lastVersion = repo.findFirstByFileGroupTypeAndFileGroupCodeOrderByVersionNoDesc(fileGroup.getFileGroupType(), fileGroup.getFileGroupCode()).orElse(null);
		if (!fileGroup.getId().equals(lastVersion.getId())) {
			throw new BizException("DATA_NOT_LAST_VERSION", new Property("version", fileGroup.getVersion()));
		} else if (fileGroup.getVersionNo() > ValueUtils.toVersionNo(version)) {
			throw new BizException("VERSION_DOWN_UNAVAILABLE", new Property("version", fileGroup.getVersion()));
		}

		FileGroup newVersion = ValueUtils.map(fileGroup, FileGroup.class);
		newVersion.setId(null);
		newVersion.setVersion(version);
		newVersion.setFileGroupStatus(FileGroupStatus.UNRELEASED);

		List<FileItem> items = new ArrayList<>();
		findAllItems(fileGroup).forEach(item -> {
			FileItem newItem = ValueUtils.map(item, FileItem.class);
			newItem.setId(null);
			newItem.setVersion(version);
			items.add(newItem);
		});

		repo.save(newVersion);
		BeanUtils.get(FileItemRepository.class).saveAll(items);
	}

	@Transactional
	public List<FileGroup> getVersions(Long id) {
		if (!repo.existsById(id)) {
			return Collections.emptyList();
		}
		FileGroup fileGroup = findById(id);
		return repo.findAllByFileGroupKeyOrderByVersionNoDesc(fileGroup.getFileGroupKey());
	}

	private FileGroup findById(Long id) {
		return repo.findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(FileGroup.class, "file_group_id", id));
	}

	private List<FileItem> findAllItems(FileGroup fileGroup) {
		List<FileItem> items = new ArrayList<>();
		BeanUtils.get(FileItemRepository.class).findAll(new BooleanBuilder()//
				.and(Qs.fileItem.fileGroupKey.eq(fileGroup.getFileGroupKey()))//
				.and(Qs.fileItem.paramName.eq(fileGroup.getParamName()))//
				.and(Qs.fileItem.version.eq(fileGroup.getVersion()))//
		).forEach(item -> items.add(item));
		return items;
	}
}
