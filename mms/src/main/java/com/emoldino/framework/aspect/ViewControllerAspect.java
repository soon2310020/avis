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
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

@Component
@Aspect
public class ViewControllerAspect {

	@Around("execution(* com.emoldino.view.*Controller.*(..))"//
			+ " or execution(* com.emoldino.view.*.*Controller.*(..))"//
	)
	public Object doAround(final ProceedingJoinPoint point) throws Throwable {
		return ThreadUtils.doScope("ViewControllerAspect.doAround", () -> {
			Object output = null;

			Throwable t = null;
			try {
				output = point.proceed();
				return output;

			} catch (Throwable th) {
				// 4. Throws

				// 4.1 Unwrap
				t = ValueUtils.unwrap(th);

				// 4.2 Handler Error
				AbstractException ae = null;
				if (t instanceof Error) {
					ae = LogUtils.saveErrorQuietly(ErrorType.LOGIC, "ERROR", HttpStatus.NOT_IMPLEMENTED, "Throwable Error happened at " + HttpUtils.getRequestStr(), t);
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
