package com.emoldino.framework.exception.hadler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.exception.SysException;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.HttpUtils;

@RestControllerAdvice(basePackages = "com.emoldino.api")
public class RestExceptionHandler {

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<ErrorResponseBody> handler(Throwable t) {
		return handle(new SysException("UNHANDLED_ERROR", t));
	}

	@ExceptionHandler(BizException.class)
	public ResponseEntity<ErrorResponseBody> handler(BizException e) {
		return handle(e, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(LogicException.class)
	public ResponseEntity<ErrorResponseBody> handle(LogicException e) {
		return handle(e, HttpStatus.NOT_IMPLEMENTED);
	}

	@ExceptionHandler(SysException.class)
	public ResponseEntity<ErrorResponseBody> handle(SysException e) {
		return handle(e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ErrorResponseBody> handle(AbstractException e, HttpStatus defaultStatus) {
		if (e == null || defaultStatus == null) {
			return null;
		}
		HttpStatus status = HttpUtils.getStatus(e, defaultStatus);
		if (!(e instanceof BizException)) {
			LogUtils.saveErrorQuietly(e);
		}
		ErrorResponseBody body = new ErrorResponseBody(//
				status.value(), //
				status.getReasonPhrase(), //
				e.getCode(), //
				e.getCodeMessage(), //
				e.getId(), //
				ObjectUtils.isEmpty(e.getProperties()) ? null : e.getProperties(), //
				"developer".equals(ConfigUtils.getProfile()) ? e.getDetailedLog() : null//
		);
		return new ResponseEntity<>(body, status);
	}

}
