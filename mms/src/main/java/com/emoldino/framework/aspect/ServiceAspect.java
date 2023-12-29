package com.emoldino.framework.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

@Component
@Aspect
public class ServiceAspect {

	@Around("execution(* com.emoldino.api.*.resource.*.*.service.*Service.*(..))"//
			+ " or execution(* com.emoldino.api.*.resource.*.*.service.*.*Service.*(..))")
	public Object doAround(final ProceedingJoinPoint point) throws Throwable {
		return ThreadUtils.doScope("RestControllerAspect.doAround", () -> {
			Object output = null;

			// 0. Preset
			String name = ReflectionUtils.getShortName(point);

			Throwable t = null;
			try {
				// 1. Before

				// 2. Logic
				output = point.proceed();

				// 3. After

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

			}
		});
	}

}
