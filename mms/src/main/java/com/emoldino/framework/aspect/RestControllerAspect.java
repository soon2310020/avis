package com.emoldino.framework.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.file.util.FileRelationUtils;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.annotation.ApiOptions;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.exception.SuccessException;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class RestControllerAspect {

	@Value("${app.access-control.data-leak.disabled:false}")
	private boolean dataLeakAccessControlDisabled;

	@Value("${app.log.access-summary.enabled:false}")
	private boolean accessSummaryLogEnabled;

	@Value("${app.log.long-elapsed-time-millis.threshold:-1}")
	private long longElapsedTimeMillisThreshold;

	@Around("execution(* com.emoldino.api.*.resource.*.*.*ControllerImpl.*(..))"//
			+ " or execution(* saleson.service.transfer.TransferController.*(..))"//
			+ " or execution(* saleson.api.*.*Controller.*(..))"//
			+ " or execution(* saleson.api.*.*.*Controller.*(..))")
	public Object doAround(final ProceedingJoinPoint point) throws Throwable {
		return ThreadUtils.doScope("RestControllerAspect.doAround", () -> {
			Object output = null;

			// 0. Preset
			String name = ReflectionUtils.getShortName(point);
			boolean firstStack = false;
			Long startTimeMillis = null;
			Integer depth = null;
			if (ThreadUtils.getPropApiName() == null) {
				firstStack = true;
				ThreadUtils.procGapTime();
				ThreadUtils.setPropApiName(name);
			} else {
				startTimeMillis = System.currentTimeMillis();
				depth = (Integer) ThreadUtils.getProp("RestControllerAspect.depth");
				depth = (depth == null ? 0 : depth) + 1;
				ThreadUtils.setProp("RestControllerAspect.depth", depth);
			}

			Method method = null;
			Throwable t = null;
			try {
				method = ReflectionUtils.getMethod(point);
				final Object[] args = point.getArgs();

				// 1. Before

				// 1.1 Move/Remove Temporal Files to Storage
				if (firstStack) {
					populateArgs(method, args);
					FileRelationUtils.move(args);
				}

				// 2. Logic
				try {
					output = point.proceed();
				} catch (SuccessException e) {
					output = e.getOutput();
				}

				// 3. After

				if (firstStack) {
					// 3.1 DataLeak AccessControl
					if (!dataLeakAccessControlDisabled) {
						AccessControlUtils.checkDataLeak(method, output);
					}

					// 3.2 Populate FileDto
					FileRelationUtils.populate(output);

					// TODO 3.3 Save/Delete Files -> will be moved to "after transaction" scope
					FileRelationUtils.save(args);
				}

				return output;

			} catch (Throwable th) {
				// 4. Throws

				// 4.1 Unwrap
				t = ValueUtils.unwrap(th);

				// 4.2 Handler Error
				AbstractException ae = null;
				if (t instanceof Error) {
					ae = LogUtils.saveErrorQuietly(ErrorType.LOGIC, "ERROR", HttpStatus.NOT_IMPLEMENTED, "Throwable Error happened at " + name, t);
				} else if (t instanceof BizException) {
					ae = (BizException) t;
				} else {
					ae = ValueUtils.toAe(t, null);
					LogUtils.saveErrorQuietly(ae);
				}

				throw ae;

			} finally {
				// 5. Finally

				// 5.1 ElapsedTime Log
				long elapsedTimeMillis = 0L;
				if (firstStack) {
					elapsedTimeMillis = ThreadUtils.procGapTime().getGapTimeMillis();
				} else if (startTimeMillis != null) {
					elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
				}
				if (longElapsedTimeMillisThreshold > 0L && longElapsedTimeMillisThreshold < elapsedTimeMillis) {
					if (method != null) {
						ApiOptions options = method.getAnnotation(ApiOptions.class);
						if (options == null || options.longElapsedTimeMillisThreshold() == 0
								|| (options.longElapsedTimeMillisThreshold() > 0L && options.longElapsedTimeMillisThreshold() < elapsedTimeMillis)) {
							String logStr = LogUtils.toElapsedTimeMillisStr(elapsedTimeMillis, name);
							log.warn("LONG_ELAPSED_TIME!! " + logStr);
							LogUtils.saveErrorQuietly(ErrorType.SYS, "LONG_ELAPSED_TIME", HttpStatus.SERVICE_UNAVAILABLE, logStr);
						}
					}
				}
				if (log.isDebugEnabled()) {
					log.debug(LogUtils.toElapsedTimeMillisStr(elapsedTimeMillis, name));
				}

				// 5.2 Access Summary Log
				if (accessSummaryLogEnabled) {
					String apiName;
					if (firstStack) {
						apiName = name;
					} else {
						apiName = name + "[" + depth + "]";
					}
					LogUtils.putAccessSummaryQuietly(apiName, elapsedTimeMillis, t);
				}
			}
		});
	}

	private void populateArgs(Method method, Object[] args) {
		if (ObjectUtils.isEmpty(method.getParameters()) || ObjectUtils.isEmpty(args)) {
			return;
		}

		// Firstly, we let the id param can be set to input dto automatically, if the id field is in the dto.
		Map<String, Object> map = null;
		int i = 0;
		for (Parameter param : method.getParameters()) {
			if (args.length <= i) {
				break;
			}
			if (ValueUtils.isPrimitiveType(param.getType())) {
				if (ObjectUtils.isEmpty(param.getName()) || !param.getName().equals("id") || args[i] == null) {
					break;
				}
				Object id = args[i];
				if (map == null) {
					map = new LinkedHashMap<>();
				}
				map.put(param.getName(), id);
			} else if (map == null || map.isEmpty()) {
				break;
			} else {
				Object dto = args[i];
				if (dto == null || //
						dto instanceof TimeSetting || dto instanceof BatchIn || dto instanceof Pageable || //
						dto instanceof Collection || dto instanceof Map || dto.getClass().isArray()) {
					continue;
				}
				map.forEach((fieldName, value) -> {
					Method setter = ReflectionUtils.getSetter(dto, fieldName);
					if (setter == null) {
						return;
					}
					try {
						setter.invoke(dto, value);
					} catch (Exception e) {
						// DO NOTHING
					}
				});
			}
			i++;
		}
	}

}
