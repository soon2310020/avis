package com.emoldino.api.common.resource.base.file.util;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.file.dto.FileGroupGetIn;
import com.emoldino.api.common.resource.base.file.dto.FilePostTmpOut;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroup;
import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItem;
import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItemRepository;
import com.emoldino.api.common.resource.base.file.service.FileService;
import com.emoldino.api.common.resource.base.file.service.group.FileGroupService;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

	public static File getReleasedByGroupTypeAndCode(FileGroupType fileGroupType, String fileGroupCode) {
		return TranUtils.doTran(() -> {
			Page<FileGroup> page = BeanUtils.get(FileGroupService.class).get(//
					FileGroupGetIn.builder()//
							.fileGroupType(fileGroupType)//
							.fileGroupCode(fileGroupCode)//
							.fileGroupStatus(List.of(FileGroupStatus.RELEASED))//
							.groupByCode(false)//
							.enabled(true)//
							.build(), //
					PageRequest.of(0, 10, Direction.DESC, "versionNo"));

			Iterable<FileItem> items = null;
			for (FileGroup fileGroup : page.getContent()) {
				if (fileGroup.getVersionNo() == null) {
					break;
				} else if (!ValueUtils.toString(fileGroup.getVersionNo()).endsWith("99")) {
					continue;
				}
				items = BeanUtils.get(FileItemRepository.class).findAll(//
						new BooleanBuilder()//
								.and(Qs.fileItem.fileGroupKey.eq(fileGroup.getFileGroupKey()))//
								.and(Qs.fileItem.paramName.eq(fileGroup.getParamName()))//
								.and(Qs.fileItem.version.eq(fileGroup.getVersion())), //
						Sort.by("position")//
				);
				break;
			}

			if (ObjectUtils.isEmpty(items)) {
				return null;
			}

			File file = null;
			for (FileItem item : items) {
				file = BeanUtils.get(FileService.class).getByFileLocation(item.getFileLocation());
				break;
			}
			return file;
		});
	}

	public static FilePostTmpOut postTmp(InputStream is) {
		return BeanUtils.get(FileService.class).postTmp(is);
	}

	public static void post(Object... args) {
		if (ObjectUtils.isEmpty(args)) {
			return;
		}
		boolean allNull = true;
		for (Object arg : args) {
			if (arg != null) {
				allNull = false;
				break;
			}
		}
		if (allNull) {
			return;
		}

		FileRelationUtils.move(args);
		FileRelationUtils.save(args);
	}

}
