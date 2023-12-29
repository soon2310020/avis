package com.emoldino.api.common.resource.composite.flemng.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.file.dto.FileGroupGetIn;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroup;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroupRepository;
import com.emoldino.api.common.resource.base.file.repository.filegroup.QFileGroup;
import com.emoldino.api.common.resource.base.file.service.group.FileGroupService;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngGetIn;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngGroup;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngPutVersionIn;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

@Service
public class FleMngService {
	@Autowired
	private FileGroupService service;

	public Page<FleMngGroup> get(FleMngGetIn input, Pageable pageable) {
		FileGroupGetIn reqin = ValueUtils.map(input, FileGroupGetIn.class);
		reqin.setManagedTypeOnly(true);
		Page<FileGroup> page = service.get(reqin, pageable);
		List<FleMngGroup> fleMngGroupList = page.getContent().stream()//
				.map(fileGroup -> fromModel(fileGroup))//
				.collect(Collectors.toList());
		return new PageImpl<>(fleMngGroupList, pageable, page.getTotalElements());
	}

	public FleMngGroup get(Long id) {
		FileGroup fileGroup = BeanUtils.get(FileGroupRepository.class).findById(id).orElse(null);
		FleMngGroup data = fromModel(fileGroup);
		return data;
	}

	private static FleMngGroup fromModel(FileGroup fileGroup) {
		FleMngGroup data = ValueUtils.map(fileGroup, FleMngGroup.class);
		if (FileGroupStatus.RELEASED.equals(data.getFileGroupStatus())) {
			data.setReleasedVersion(data.getVersion());
		} else {
			BooleanBuilder filter = new BooleanBuilder()//
					.and(Qs.fileGroup.fileGroupType.eq(data.getFileGroupType()))//
					.and(Qs.fileGroup.fileGroupCode.eq(data.getFileGroupCode()))//
					.and(Qs.fileGroup.fileGroupStatus.eq(FileGroupStatus.RELEASED));
			Page<FileGroup> lastReleased = BeanUtils.get(FileGroupRepository.class).findAll(filter, PageRequest.of(0, 1, Direction.DESC, "versionNo"));
			if (!lastReleased.isEmpty()) {
				data.setReleasedVersion(lastReleased.getContent().get(0).getVersion());
			}
		}
		return data;
	}

	public Long post(FleMngGroup data) {
		return service.post(data.toModel());
	}

	public void put(Long id, FleMngGroup data) {
		service.put(id, data.toModel());
	}

	public void delete(Long id) {
		service.delete(id);
	}

	public void disable(List<Long> id) {
		service.disable(id);
	}

	public void enable(List<Long> id) {
		service.enable(id);
	}

	public void release(Long id, FleMngGroup data) {
		put(id, data);
		service.release(id);
	}

	public void unrelease(Long id, FleMngGroup data) {
		service.unrelease(id);
		put(id, data);
	}

	public void putVersion(Long id, FleMngPutVersionIn data) {
		service.putVersion(id, data.getVersion());
	}

	public List<FleMngGroup> getVersions(Long id) {
		List<FileGroup> fileGroups = service.getVersions(id);
		List<FleMngGroup> fleMngGroupList = fileGroups.stream()//
				.map(fileGroup -> {
					FleMngGroup item = fromModel(fileGroup);
					return item;
				})//
				.collect(Collectors.toList());
		return fleMngGroupList;
	}

}
