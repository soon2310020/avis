package com.emoldino.api.common.resource.base.accesscontrol.service.dataleak;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.api.common.resource.base.accesscontrol.exception.DataLeakDetectedException;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ClosureNoReturn;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.BooleanBuilder;

import lombok.extern.slf4j.Slf4j;
import saleson.api.company.CompanyRepository;
import saleson.api.counter.CounterRepository;
import saleson.api.location.LocationRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.api.user.UserRepository;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;
import saleson.model.Counter;
import saleson.model.Location;
import saleson.model.Mold;
import saleson.model.QCompany;
import saleson.model.QCounter;
import saleson.model.QLocation;
import saleson.model.QMold;
import saleson.model.QTerminal;
import saleson.model.QUser;
import saleson.model.Terminal;
import saleson.model.User;

@Service
@Slf4j
public class DataLeakAccessControlService {

	@Value("${app.access-control.data-leak.prevention-strategy:ERROR}")
	private String dataLeakAccessControlPreventionStrategy;

	// Field Names for check. need to check in the future "partId", "categoryId", "toolMakerCompanyId", "toolMakerId", "toolMaker"
	private static final Stream<String> FIELD_NAME_STREAM = Stream.of("companyId", "companyCode", "supplierCompanyId", "supplierId", "supplier", "userId", "locationId", "moldId",
			"terminalId", "counterId", "ci");

	private static final Map<String, String> FIELD_NAME_AND_METHOD_NAME_MAP;
	private static final Map<String, String> METHOD_NAME_AND_FIELD_NAME_MAP;
	static {
		FIELD_NAME_AND_METHOD_NAME_MAP = FIELD_NAME_STREAM.collect(Collectors.toMap(name -> name, name -> ReflectionUtils.toGetterName(name)));
		METHOD_NAME_AND_FIELD_NAME_MAP = MapUtils.invertMap(FIELD_NAME_AND_METHOD_NAME_MAP);
	}

	// Defence StackOverFlow
	private static final int MAX_DEPTH = 6;

	@Transactional
	public void check(Object data) {
		check(data, null, null, 0, new HashSet<>(), new HashSet<>());

		DataLeakDetectedException dlde = (DataLeakDetectedException) ThreadUtils.getProp("DataLeakService.lastDlde");
		if (dlde != null) {
			LogUtils.saveErrorQuietly(dlde);
		}

		ThreadUtils.setProp("DataLeakService.rlist", null);
	}

	private void check(Object data, Class<?> fromClass, Method fromMethod, int fromDepth, Set<Object> checked, Set<Long> companyIds) {
		if (data == null || ValueUtils.isPrimitiveType(data)) {
			return;
		}
		if (fromDepth >= MAX_DEPTH) {
			return;
		}

		// 1. Logic by DataType
		// 1.1 ResponseEntity
		if (data instanceof ResponseEntity) {
			check(((ResponseEntity<?>) data).getBody(), fromClass, fromMethod, fromDepth, checked, companyIds);
		}
		// 1.2 Slice
		else if (data instanceof Slice) {
			Slice<?> slice = (Slice<?>) data;
			List<?> content = slice.getContent();
			if (ObjectUtils.isEmpty(content)) {
				return;
			}

			checkMethod("getContent", () -> check(content, fromClass, fromMethod, fromDepth, checked, companyIds));

			@SuppressWarnings("unchecked")
			List<Object> rlist = (List<Object>) ThreadUtils.getProp("DataLeakService.rlist");
			if (ObjectUtils.isEmpty(rlist)) {
				return;
			}

			try {
				Field field = ReflectionUtils.getFieldAndSetAccessible(slice, "content");
				if (field == null) {
					DataLeakDetectedException dlde = getDlde();
					throw dlde;
				}
				List<?> newContent = new ArrayList<>(content);
				newContent.removeAll(rlist);
				try {
					field.set(slice, newContent);
				} catch (Exception e) {
					DataLeakDetectedException dlde = getDlde();
					throw dlde;
				}
			} finally {
				ThreadUtils.setProp("DataLeakService.rlist", null);
			}
		}
		// 1.3 Optional
		else if (data instanceof Optional) {
			((Optional<?>) data).ifPresent(item -> check(item, fromClass, fromMethod, fromDepth, checked, companyIds));
		}
		// 1.4 Collection
		else if (data instanceof Collection) {
			ThreadUtils.setProp("DataLeakService.rlist", null);
			Collection<?> collection = (Collection<?>) data;
			List<Object> rlist = new ArrayList<>();
			Set<DataLeakDetectedException> dlde = new HashSet<>();
			int i[] = { 0 };
			collection.stream().filter(item -> item != null).forEach(item -> {
				try {
					checkMethod(i[0]++ + "", () -> check(item, fromClass, fromMethod, fromDepth, checked, companyIds));
				} catch (DataLeakDetectedException e) {
					if (dlde.isEmpty()) {
						dlde.add(e);
					}
					if (StringUtils.equalsIgnoreCase("REMOVE", dataLeakAccessControlPreventionStrategy)) {
						rlist.add(item);
					} else {
						throw e;
					}
				}
			});
			if (rlist.isEmpty()) {
				return;
			}

			if (!dlde.isEmpty()) {
				ThreadUtils.setProp("DataLeakService.lastDlde", dlde.iterator().next());
			}
			try {
				collection.removeAll(rlist);
			} catch (UnsupportedOperationException e) {
				ThreadUtils.setProp("DataLeakService.rlist", rlist);
			}
		}
		// 1.5 Array
		else if (data.getClass().isArray()) {
			int i[] = { 0 };
			Arrays.asList((Object[]) data).stream().filter(item -> item != null).forEach(item -> {
				checkMethod(i[0]++ + "", () -> check(item, fromClass, fromMethod, fromDepth, checked, companyIds));
			});
		}
		// 1.6 DTO Data
		else {
			int currentDepth = fromDepth + 1;

			// 1.6.1 Map DataType Case is Not Provided(Skipped) right now
			if (data instanceof Map) {
//				Map<?, ?> map = (Map<?, ?>) data;
//				if (map.isEmpty() || !(map.keySet().iterator().next() instanceof String)) {
//					return;
//				}
//				((Map<String, ?>) data).forEach((k, v) -> {
//					if (isPrimitive(v)) {
//						checkPermission((String) k, v, companyIds);
//					} else {
//						checkPermission(v, companyIds);
//					}
//				});
			}
			// 1.6.2 ByteBuddyInterceptor Case is Not Provided(Skipped) right now
			else if (data instanceof ByteBuddyInterceptor) {

			}
			// 1.6.3 DTO Model
			else {
				Class<?> clazz = data.getClass();
				String className = clazz.getName();

				// 1.7.1 Validation
				if (!className.startsWith("com.emoldino.") && !className.startsWith("saleson")) {
					saveErrorDataLeakUnconsideredQuietly(clazz, fromClass, fromMethod);
//					log.warn("Check whether we need to consider this DataType: " + className + " for data leack prevention or not");
					return;
				}

				// Defence StackOverFlow
				if (checked.contains(data)) {
					return;
				}
				checked.add(data);
				try {
					// 1.7.2 Get Methods for Check
					List<Method> methods = getMethods(clazz, fromClass, fromMethod);

					// 1.7.3 Check DataLeak by Method & MethodValue
					for (Method method : methods) {
						Object value;
						try {
							value = method.invoke(data);
						} catch (Exception e) {
							LogUtils.saveErrorQuietly(ErrorType.LOGIC, "GETTER_METHOD_INVOKE_ERROR", HttpStatus.NOT_IMPLEMENTED,
									ReflectionUtils.toShortName(clazz, method.getName()) + ": " + e.getMessage());
							log.warn(e.getMessage(), e);
							continue;
						}

						if (ObjectUtils.isEmpty(value)) {
							continue;
						}

						checkMethod(method.getName(), () -> {
							if (!ValueUtils.isPrimitiveType(value)) {
								check(value, clazz, method, currentDepth, checked, companyIds);
							} else {
								String fieldName = METHOD_NAME_AND_FIELD_NAME_MAP.get(method.getName());
								check(fieldName, value, companyIds);
							}
						});
					}
				} finally {
					checked.remove(data);
				}
			}
		}
	}

	private static DataLeakDetectedException getDlde() {
		DataLeakDetectedException dlde = (DataLeakDetectedException) ThreadUtils.getProp("DataLeakService.lastDlde");
		if (dlde != null) {
			return dlde;
		}

		throw new DataLeakDetectedException("Unclear");
	}

	private static final Map<Class<?>, List<Method>> CHECKLIST_CACHE = new ConcurrentHashMap<>();

	private static List<Method> getMethods(Class<?> clazz, Class<?> fromClass, Method fromMethod) {
		List<Method> methods = SyncCtrlUtils.wrap("DataLeakService.getMethods." + clazz.getName(), CHECKLIST_CACHE, clazz, () -> _getMethods(clazz, fromClass, fromMethod));
		return methods;
	}

	private static List<Method> _getMethods(Class<?> clazz, Class<?> parentClass, Method currentMethod) {
		String className = clazz.getName();
		if (!className.startsWith("com.emoldino.") && !className.startsWith("saleson")) {
			saveErrorDataLeakUnconsideredQuietly(clazz, parentClass, currentMethod);
			return Collections.emptyList();
		}

		List<Method> list = new ArrayList<>();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();

			// check method disabled
			if (!methodName.startsWith("get") || methodName.equals("getClass") || isDisabled(method)) {
				continue;
			}

			Class<?> returnType = method.getReturnType();

			// check field disabled
			String fieldName = ReflectionUtils.toFieldName(methodName);
			Field field = ReflectionUtils.getField(clazz, fieldName);
			if (field != null && isDisabled(field)) {
				continue;
			}

			// primitive type
			if (ValueUtils.isPrimitiveType(returnType)) {
				if (METHOD_NAME_AND_FIELD_NAME_MAP.containsKey(methodName)) {
					list.add(method);
				}
				continue;
			}

			try {
				// Generic Type DTO Model
				if (Slice.class.isAssignableFrom(returnType) || Optional.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType)) {
					Type genericType = method.getGenericReturnType();
					if (genericType == null || method.getReturnType().equals(genericType)) {
						continue;
					}

					List<Type> types = ReflectionUtils.getActualTypeList(genericType);
					for (Type type : types) {
						if (!(type instanceof Class)) {
							continue;
						}
						Class<?> classType = (Class<?>) type;
						if (Object.class.equals(classType)) {
							LogUtils.saveErrorQuietly(ErrorType.LOGIC, "GENERIC_TYPE_UNDECLARED", HttpStatus.NOT_IMPLEMENTED,
									"Generic Type is undeclared at returnType of " + className + "." + methodName);
						} else if (ValueUtils.isPrimitiveType(classType)) {
							continue;
						}
						list.add(method);
					}

				}
				// Map DataType Case is Not Provided(Skipped) right now
				else if (Map.class.isAssignableFrom(returnType)) {

				}
				// DTO Model
				else {
					list.add(method);
				}
			} catch (Exception e) {
				// Skip
				log.warn(e.getMessage(), e);
			}
		}
		return list.isEmpty() ? Collections.emptyList() : list;
	}

	private static void saveErrorDataLeakUnconsideredQuietly(Class<?> dataType, Class<?> fromClass, Method fromMethod) {
		StringBuilder buf = new StringBuilder();
		buf.append("This DataType is unconsidered for detecting data leak: ").append(dataType.getName());
		if (fromClass != null) {
			buf.append(" at ").append(fromClass.getName());
			if (fromMethod != null) {
				buf.append(".").append(fromMethod.getName());
			}
		}
		LogUtils.saveErrorQuietly(ErrorType.LOGIC, "DATA_LEAK_UNCONSIDERED", HttpStatus.NOT_IMPLEMENTED, buf.toString());
	}

	private static boolean isDisabled(Method target) {
		LogicUtils.assertNotNull(target, "method");

		JsonIgnore jsonIgnore = target.getAnnotation(JsonIgnore.class);
		if (jsonIgnore != null && jsonIgnore.value()) {
			return true;
		}

		DataLeakDetector detector = target.getAnnotation(DataLeakDetector.class);
		if (detector != null) {
			return detector.disabled();
		}

		return false;
	}

	private static boolean isDisabled(Field target) {
		LogicUtils.assertNotNull(target, "field");

		JsonIgnore jsonIgnore = target.getAnnotation(JsonIgnore.class);
		if (jsonIgnore != null && jsonIgnore.value()) {
			return true;
		}

		DataLeakDetector detector = target.getAnnotation(DataLeakDetector.class);
		if (detector != null) {
			return detector.disabled();
		}

		return false;
	}

	private void check(String fieldName, Object value, Set<Long> companyIds) {
		if (value == null || !FIELD_NAME_AND_METHOD_NAME_MAP.containsKey(fieldName)) {
			return;
		}

		Long companyId = SecurityUtils.getCompanyId();

		if (value instanceof Long) {
			if ("companyId".equals(fieldName) || "supplierCompanyId".equals(fieldName) || "supplierId".equals(fieldName) || "supplier".equals(fieldName)) {
				if (value.equals(companyId)) {
					return;
				}

				if (companyIds.isEmpty()) {
					companyIds.addAll(AccessControlUtils.getAllAccessibleCompanyIds(companyId));
				}

				if (!companyIds.contains(value)) {
					throw new DataLeakDetectedException(toMessage("Company", fieldName, null, null, (Long) value, companyId));
				}

			} else {
				if (companyIds.isEmpty()) {
					companyIds.addAll(AccessControlUtils.getAllAccessibleCompanyIds(companyId));
				}

				if ("userId".equals(fieldName)) {
					QUser table = QUser.user;
					BooleanBuilder builder = new BooleanBuilder();
					builder.and(table.id.eq((Long) value)).and(table.companyId.in(companyIds));
					if (!BeanUtils.get(UserRepository.class).exists(builder)) {
						User data = BeanUtils.get(UserRepository.class).findById((Long) value).orElse(new User());
						throw new DataLeakDetectedException(toMessage("User", fieldName, value, data.getName(), data.getCompanyId(), companyId));
					}
				} else if ("locationId".equals(fieldName)) {
					QLocation table = QLocation.location;
					BooleanBuilder builder = new BooleanBuilder();
					builder.and(table.id.eq((Long) value)).and(table.companyId.in(companyIds));
					if (!BeanUtils.get(LocationRepository.class).exists(builder)) {
						Location data = BeanUtils.get(LocationRepository.class).findById((Long) value).orElse(new Location());
						throw new DataLeakDetectedException(toMessage("Location", fieldName, value, data.getName(), data.getCompanyId(), companyId));
					}
				} else if ("moldId".equals(fieldName)) {
					QMold table = QMold.mold;
					BooleanBuilder builder = new BooleanBuilder();
					builder.and(table.id.eq((Long) value)).and(table.companyId.in(companyIds));
					if (!BeanUtils.get(MoldRepository.class).exists(builder)) {
						Mold data = BeanUtils.get(MoldRepository.class).findById((Long) value).orElse(new Mold());
						throw new DataLeakDetectedException(toMessage("Mold", fieldName, value, data.getToolDescription(), data.getCompanyId(), companyId));
					}
				} else if ("terminalId".equals(fieldName)) {
					QTerminal table = QTerminal.terminal;
					BooleanBuilder builder = new BooleanBuilder();
					builder.and(table.id.eq((Long) value)).and(table.companyId.in(companyIds));
					if (!BeanUtils.get(TerminalRepository.class).exists(builder)) {
						Terminal data = BeanUtils.get(TerminalRepository.class).findById((Long) value).orElse(new Terminal());
						throw new DataLeakDetectedException(toMessage("Terminal", fieldName, value, data.getEquipmentCode(), data.getCompanyId(), companyId));
					}
				} else if ("counterId".equals(fieldName)) {
					QCounter table = QCounter.counter;
					BooleanBuilder builder = new BooleanBuilder();
					builder.and(table.id.eq((Long) value)).and(table.companyId.in(companyIds));
					if (!BeanUtils.get(CounterRepository.class).exists(builder)) {
						Counter data = BeanUtils.get(CounterRepository.class).findById((Long) value).orElse(new Counter());
						throw new DataLeakDetectedException(toMessage("Counter", fieldName, value, data.getEquipmentCode(), data.getCompanyId(), companyId));
					}
//				} else if ("partId".equals(fieldName)) {
//					QPart table = QPart.part;
//					BooleanBuilder builder = new BooleanBuilder();
//					builder.and(table.id.eq((Long) value)).and(table.);
//					if (BeanUtils.get(PartRepository.class).exists(builder)) {
//						throw new RuntimeException("Not permitted data!");
//					}
				}
			}
		} else if (value instanceof String) {
			if (companyIds.isEmpty()) {
				companyIds.addAll(AccessControlUtils.getAllAccessibleCompanyIds(companyId));
			}

			if ("companyCode".equals(fieldName)) {
				QCompany table = QCompany.company;
				BooleanBuilder builder = new BooleanBuilder();
				builder.and(table.companyCode.eq((String) value)).and(table.id.in(companyIds));
				if (!BeanUtils.get(CompanyRepository.class).exists(builder)) {
					Company data = BeanUtils.get(CompanyRepository.class).findByCompanyCode((String) value).orElse(new Company());
					throw new DataLeakDetectedException(toMessage("Company", fieldName, value, data.getCompanyCode(), data.getId(), companyId));
				}
			} else if ("counterId".equals(fieldName) || "ci".equals(fieldName)) {
				QCounter table = QCounter.counter;
				BooleanBuilder builder = new BooleanBuilder();
				builder.and(table.equipmentCode.eq((String) value)).and(table.companyId.in(companyIds));
				if (!BeanUtils.get(CounterRepository.class).exists(builder)) {
					Counter data = BeanUtils.get(CounterRepository.class).findByEquipmentCode((String) value).orElse(new Counter());
					throw new DataLeakDetectedException(toMessage("Counter", fieldName, value, data.getEquipmentCode(), data.getCompanyId(), companyId));
				}
			}
		}
	}

	private static void checkMethod(String methodName, ClosureNoReturn closure) {
		getMethodStacks().push(methodName);
		try {
			closure.execute();
		} finally {
			getMethodStacks().pop();
		}
	}

	private static Stack<String> getMethodStacks() {
		Stack<String> stack = ThreadUtils.getProp("DataLeakAccessControlService.check.methods", () -> new Stack<String>());
		return stack;
	}

	private String toMessage(String resourceName, String fieldName, Object value, String name, Long dataCompanyId, Long companyId) {
		StringBuilder buf = new StringBuilder();
		buf.append("Data Leak is detected!! ").append(toCompanyStr(dataCompanyId));
		if (value != null) {
			String label = toLabel(value, name);
			buf.append(".").append(resourceName).append("(").append(label).append(")");
		}
		buf.append(" data (by ").append(fieldName).append(" field) is unpermitted to this ").append(toCompanyStr(companyId));
		buf.append(", Path: ");
		getMethodStacks().forEach(method -> {
			if (ValueUtils.isNumber(method)) {
				buf.append("[").append(method).append("]");
			} else {
				buf.append("/").append(method);
			}
		});
		return buf.toString();
	}

	private String toCompanyStr(Long companyId) {
		if (companyId == null) {
			return null;
		}
		try {
			String name = BeanUtils.get(CompanyRepository.class).findById(companyId).orElse(new Company()).getName();
			return "Company(" + toLabel(companyId, name) + ")";
		} catch (Exception e) {
			return companyId.toString();
		}
	}

	private String toLabel(Object id, String name) {
		return ObjectUtils.isEmpty(name) ? id.toString() : name + " - " + id;
	}

}
