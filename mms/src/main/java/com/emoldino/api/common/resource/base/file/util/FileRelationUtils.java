package com.emoldino.api.common.resource.base.file.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.file.annotation.FileRelation;
import com.emoldino.api.common.resource.base.file.dto.FileDto;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.enumeration.FileStatus;
import com.emoldino.api.common.resource.base.file.enumeration.StorageType;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroup;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroupRepository;
import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItem;
import com.emoldino.api.common.resource.base.file.repository.fileitem.FileItemRepository;
import com.emoldino.api.common.resource.base.file.service.FileService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileRelationUtils {

	public static void move(Object[] args) {
		if (ObjectUtils.isEmpty(args)) {
			return;
		}

		for (Object arg : args) {
			if (arg == null || ValueUtils.isPrimitiveType(arg) || arg instanceof Map) {
				continue;
			}

			List<FileField> relations = getFileFields(arg.getClass());
			if (ObjectUtils.isEmpty(relations)) {
				continue;
			}

			FileRelationUtils.move(arg, relations);
		}
	}

	@SuppressWarnings("unchecked")
	private static void move(Object data, List<FileField> relations) {
		if (data == null || ValueUtils.isPrimitiveType(data)) {
			return;
		}

		if (data instanceof Map || data instanceof Collection) {
			return;
		}

		for (FileField relation : relations) {
			// 1. Get Version and File Refs
			String version;
			List<FileDto> fileRefs;
			try {
				version = ObjectUtils.isEmpty(relation.getVersionGetter()) ? "0" : ValueUtils.toString(relation.getVersionGetter().invoke(data));
				Method getter = relation.getGetter();
				Class<?> returnType = getter.getReturnType();
				Object value = relation.getGetter().invoke(data);
				if (List.class.equals(returnType)) {
					fileRefs = (List<FileDto>) value;
				} else if (value == null) {
					fileRefs = null;
				} else {
					fileRefs = Arrays.asList((FileDto) value);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				AbstractException ae = ValueUtils.toAe(e, null);
				throw ae;
			}
			if (fileRefs == null) {
				continue;
			}

			for (FileDto fileDto : fileRefs) {
				ValueUtils.assertNotEmpty(fileDto.getId(), "fileId");
			}

			// 2. Check File Group
			FileGroup fileGroup = TranUtils.doNewTran(() -> {
				FileGroup _fileGroup = populate(data, relation, new FileGroup());
				BooleanBuilder filter = new BooleanBuilder()//
						.and(Qs.fileGroup.fileGroupKey.eq(_fileGroup.getFileGroupKey()))//
						.and(Qs.fileGroup.paramName.eq(_fileGroup.getParamName()));
				if (BeanUtils.get(FileGroupRepository.class).exists(filter)) {
					Page<FileGroup> page = BeanUtils.get(FileGroupRepository.class).findAll(filter, PageRequest.of(0, 1, Direction.DESC, "id"));
					_fileGroup = page.getContent().get(0);
					if (!_fileGroup.isEnabled()) {
						throw new BizException("FILE_GROUP_DISABLED", new Property("fileGroupType", _fileGroup.getFileGroupType()),
								new Property("fileGroupKey", _fileGroup.getFileGroupKey()));
					}
				} else {
					_fileGroup.setVersion(version);
					_fileGroup.setFileGroupStatus(FileGroupStatus.UNMANAGED);
					_fileGroup.setFileGroupType(FileGroupType.UNMANAGED);
					_fileGroup.setFileGroupCode(_fileGroup.getFileGroupKey());
					BeanUtils.get(FileGroupRepository.class).save(_fileGroup);
				}
				return _fileGroup;
			});

			// 3. Move/Save/Remove File Items

			// 3.1 Extract Move/Save/Remove List
			int[] pos = { 1 };
			List<FileItem> moveList = new ArrayList<>();
			List<FileItem> saveList = new ArrayList<>();
			List<FileItem> removeList = new ArrayList<>();
			TranUtils.doNewTran(() -> {
				Map<Long, FileItem> map = new LinkedHashMap<>();
				BeanUtils.get(FileItemRepository.class)//
						.findAll(new BooleanBuilder()//
								.and(Qs.fileItem.fileGroupKey.eq(fileGroup.getFileGroupKey()))//
								.and(Qs.fileItem.version.eq(version))//
								.and(Qs.fileItem.paramName.eq(fileGroup.getParamName())), //
								Sort.by("position"))//
						.forEach(item -> map.put(item.getId(), item));

				if (map.isEmpty() && fileRefs.isEmpty()) {
					return;
				}

				for (FileDto fileDto : fileRefs) {
					if (NumberUtils.isCreatable(fileDto.getId())) {
						FileItem item;
						Long id = ValueUtils.toLong(fileDto.getId(), 0L);
						// Target for Insert
						if (map.containsKey(id)) {
							item = map.remove(id);
						}
						// Target for Update (different version)
						else {
							item = BeanUtils.get(FileItemRepository.class).findById(id).orElse(null);
							if (item == null || !fileGroup.getFileGroupKey().equals(item.getFileGroupKey()) || !fileGroup.getParamName().equals(item.getParamName())) {
								throw new BizException("UNKNOWN_FILE", new Property("fileId", fileDto.getId()));
							}
							item = ValueUtils.map(item, FileItem.class);
							item.setId(null);
							item.setVersion(version);
						}
						if (pos[0]++ != item.getPosition()) {
							item.setPosition(pos[0]++);
							saveList.add(item);
						}
					} else if (BeanUtils.get(FileService.class).existsTmp(fileDto.getId())) {
						FileItem item = new FileItem();
						item.setFileGroupKey(fileGroup.getFileGroupKey());
						item.setParamName(fileGroup.getParamName());
						item.setVersion(version);
						item.setPosition(pos[0]++);
						item.setFileStatus(FileStatus.CREATED);
						item.setFileType(fileGroup.getFileGroupType().name());
						item.setFileNo(ValueUtils.toString(fileDto.getFileNo(), fileGroup.getFileGroupCode()));
						item.setContentType(toContentType(fileDto));
						item.setFileSize(BeanUtils.get(FileService.class).getTmpSize(fileDto.getId()));
						item.setStorageType(StorageType.S3);
						item.setFileLocation(fileDto.getId());
						item.setFileName(ValueUtils.toString(fileDto.getFileName(), item.getFileNo()));
						moveList.add(item);
					} else {
						throw new BizException("UNKNOWN_FILE", new Property("fileId", fileDto.getId()));
					}
				}
				// Target for Remove
				map.values().forEach(item -> {
					item.setPosition(pos[0]++);
					removeList.add(item);
				});
				BeanUtils.get(FileItemRepository.class).saveAll(removeList);
			});

			// 3.2 Move
			moveList.forEach(item -> {
				String tmpId = item.getFileLocation();
				String fileLocation = BeanUtils.get(FileService.class).newFileLocation(tmpId);
				item.setFileLocation(fileLocation);

				TranUtils.doNewTran(() -> changeStatus(item, FileStatus.MOVING));
				BeanUtils.get(FileService.class).move(tmpId, fileLocation);
				TranUtils.doNewTran(() -> changeStatus(item, FileStatus.MOVED));
			});

			// 3.3 Save
			TranUtils.doNewTran(() -> BeanUtils.get(FileItemRepository.class).saveAll(saveList));
			ThreadUtils.setProp("FileRelationUtils.moveList", moveList);

			// 3.4 Remove
			removeList(removeList);

		}
	}

	private static String toContentType(FileDto fileDto) {
		return ValueUtils.toString(fileDto.getContentType(), "application/octet-stream");
	}

	public static void removeList(List<FileItem> list) {
		if (ObjectUtils.isEmpty(list)) {
			return;
		}

		list.forEach(item -> {
			TranUtils.doNewTran(() -> changeStatus(item, FileStatus.REMOVING));
			BooleanBuilder filter = new BooleanBuilder()//
					.and(Qs.fileItem.fileStatus.ne(FileStatus.REMOVING))//
					.and(Qs.fileItem.fileLocation.eq(item.getFileLocation()));
			if (!BeanUtils.get(FileItemRepository.class).exists(filter)) {
				try {
					BeanUtils.get(FileService.class).remove(item.getFileLocation());
				} catch (Exception e) {
					log.error("File:" + item.getFileLocation());
				}
			}
			TranUtils.doNewTran(() -> changeStatus(item, FileStatus.REMOVED));
		});
		ThreadUtils.setProp("FileRelationUtils.removeList", list);
	}

	private static void changeStatus(FileItem item, FileStatus status) {
		FileItem data = item.getId() == null ? item : BeanUtils.get(FileItemRepository.class).findById(item.getId()).orElse(item);
		data.setFileStatus(status);
		BeanUtils.get(FileItemRepository.class).save(data);
	}

	public static void save(Object[] args) {
		@SuppressWarnings("unchecked")
		List<FileItem> moveList = (List<FileItem>) ThreadUtils.getProp("FileRelationUtils.moveList");
		@SuppressWarnings("unchecked")
		List<FileItem> removeList = (List<FileItem>) ThreadUtils.getProp("FileRelationUtils.removeList");

		// Moved -> Save
		if (moveList != null) {
			moveList.forEach(item -> {
				TranUtils.doNewTran(() -> changeStatus(item, FileStatus.SAVED));
				BeanUtils.get(FileService.class).deleteTmp(item.getFileLocation());
			});
		}

		// Removed -> Deleted
		if (removeList != null) {
			removeList.forEach(item -> {
				TranUtils.doNewTran(() -> changeStatus(item, FileStatus.DELETED));
				TranUtils.doNewTran(() -> {
					BeanUtils.get(FileItemRepository.class).delete(item);
					BooleanBuilder filter = new BooleanBuilder()//
							.and(Qs.fileItem.fileStatus.ne(FileStatus.DELETED))//
							.and(Qs.fileItem.fileLocation.eq(item.getFileLocation()));
					if (!BeanUtils.get(FileItemRepository.class).exists(filter)) {
						try {
							BeanUtils.get(FileService.class).delete(item.getFileLocation());
						} catch (Exception e) {
							log.error("File:" + item.getFileLocation());
						}
					}
				});
			});
		}
	}

	public static boolean populate(Object data) {
		if (data == null || ValueUtils.isPrimitiveType(data) || data instanceof Map) {
			return false;
		}

		boolean found = false;

		if (data instanceof Page) {
			return populate(((Page<?>) data).getContent());
		} else if (data instanceof ListOut) {
			return populate(((ListOut<?>) data).getContent());
		} else if (data instanceof Collection) {
			boolean _found = false;
			for (Object item : (Collection<?>) data) {
				_found = populate(item);
				if (!_found) {
					break;
				}
			}
			found = found || _found;
			return found;
		}

		Class<?> clazz = data.getClass();

		List<FileField> relations = getFileFields(clazz);
		if (relations.isEmpty()) {
			return found;
		}
		for (FileField relation : relations) {
			try {
				Method getter = relation.getGetter();
				if (getter.invoke(data) != null) {
					continue;
				}
				Method setter = relation.getSetter();

				String fileGroupKey = getFileGroupKey(data, relation);
				String paramName = relation.getRelation().paramName();
				String version = relation.getVersionGetter() == null ? null : ValueUtils.toString(relation.getVersionGetter().invoke(data));
				List<FileDto> fileRefs = getFileRefs(fileGroupKey, paramName, version);

				Class<?> paramType = setter.getParameterTypes()[0];
				if (paramType.isAssignableFrom(List.class)) {
					setter.invoke(data, fileRefs);
				} else if (!ObjectUtils.isEmpty(fileRefs) && paramType.isAssignableFrom(FileDto.class)) {
					setter.invoke(data, fileRefs.get(0));
				}

				found = true;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.warn(e.getMessage(), e);
				continue;
			}
		}

		return found;
	}

	private static final String FILE_GRP_DELIMITER = "~";

	public static String toFileGroupKey(String prefix, String... values) {
		StringBuilder buf = new StringBuilder();
		buf.append(prefix);
		for (String str : values) {
			if (ObjectUtils.isEmpty(str)) {
				break;
			}
			buf.append(FILE_GRP_DELIMITER).append(str);
		}
		return buf.toString();
	}

	private static String getFileGroupKey(Object data, FileField fileField) {
		return populate(data, fileField, new FileGroup()).getFileGroupKey();
	}

	private static FileGroup populate(Object from, FileField fileField, FileGroup to) {
		LogicUtils.assertNotNull(to, "fileGroup");
		LogicUtils.assertNotNull(from, "data");
		LogicUtils.assertNotNull(fileField, "fileField");

		FileRelation relation = fileField.getRelation();
		StringBuilder buf = new StringBuilder();
		buf.append(relation.prefix());
		to.setPrefix(relation.prefix());
		int i = 0;
		for (String fieldName : relation.field()) {
			String value;
			if (fieldName.startsWith("value:")) {
				value = fieldName.substring(6);
			} else {
				value = ValueUtils.getString(from, fieldName);
			}
			LogicUtils.assertNotEmpty(value, fieldName);
			buf.append(FILE_GRP_DELIMITER).append(value);
			i++;
			if (i == 1) {
				to.setFileGroupKey1(value);
			} else if (i == 2) {
				to.setFileGroupKey2(value);
			} else if (i == 3) {
				to.setFileGroupKey3(value);
			} else if (i == 4) {
				to.setFileGroupKey4(value);
			} else if (i == 5) {
				to.setFileGroupKey5(value);
			}
		}
		to.setFileGroupKey(buf.toString());
		to.setParamName(relation.paramName());
		return to;
	}

	private static List<FileDto> getFileRefs(String fileGroupKey, String paramName, String version) {
		List<FileItem> fileItems = //
				ObjectUtils.isEmpty(version) ? //
						BeanUtils.get(FileItemRepository.class).findAllByFileGroupKeyAndParamNameOrderByPosition(fileGroupKey, paramName)//
						: BeanUtils.get(FileItemRepository.class).findAllByFileGroupKeyAndParamNameAndVersionOrderByPosition(fileGroupKey, paramName, version);
		List<FileDto> fileDtos = fileItems.stream().map(fileItem -> FileDto.fromModel(fileItem)).collect(Collectors.toList());
		return fileDtos;
	}

	private static final Map<Class<?>, List<FileField>> FILE_REL = new ConcurrentHashMap<>();

	private static List<FileField> getFileFields(Class<?> clazz) {
		List<FileField> list = SyncCtrlUtils.wrap("RelationUtils.FILE_REL." + clazz.getSimpleName(), FILE_REL, clazz, () -> {
			List<FileField> _list = new ArrayList<>();
			for (Field field : ReflectionUtils.getFieldList(clazz, true)) {
				FileRelation ann = field.getAnnotation(FileRelation.class);
				if (ann == null) {
					continue;
				}
				Method getter = ReflectionUtils.getGetter(clazz, field.getName());
				if (getter == null) {
					continue;
				}
				Method setter = ReflectionUtils.getSetter(clazz, field.getName());
				if (setter == null) {
					continue;
				}
				Method versionGetter;
				if (ObjectUtils.isEmpty(ann.versionField())) {
					versionGetter = null;
				} else {
					versionGetter = ReflectionUtils.getGetter(clazz, ann.versionField());
				}

				FileField fileField = new FileField();
				fileField.setGetter(getter);
				fileField.setSetter(setter);
				fileField.setRelation(ann);
				fileField.setVersionGetter(versionGetter);
				_list.add(fileField);
			}
			return _list;
		});
		return list;
	}

	@Data
	private static class FileField {
		private Method getter;
		private Method setter;
		private Method versionGetter;
		private FileRelation relation;
	}

}
