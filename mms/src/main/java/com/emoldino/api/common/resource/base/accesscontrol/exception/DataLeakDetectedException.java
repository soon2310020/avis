package com.emoldino.api.common.resource.base.accesscontrol.exception;

import java.util.Properties;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.Property;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
@SuppressWarnings("serial")
public class DataLeakDetectedException extends LogicException {
	private static final String SYS_CODE = "DATA_LEAK_DETECTED";

	public DataLeakDetectedException() {
		super(SYS_CODE);
	}

	public DataLeakDetectedException(String message) {
		super(SYS_CODE, message);
	}

	public DataLeakDetectedException(Throwable cause) {
		super(SYS_CODE, cause);
	}

	public DataLeakDetectedException(String message, Throwable cause) {
		super(SYS_CODE, message, cause);
	}

	public DataLeakDetectedException(Property... property) {
		super(SYS_CODE, property);
	}

	public DataLeakDetectedException(String message, Property... property) {
		super(SYS_CODE, message, property);
	}

	public DataLeakDetectedException(String message, Properties properties) {
		super(SYS_CODE, message, properties);
	}

	public DataLeakDetectedException(Throwable cause, Property... property) {
		super(SYS_CODE, cause, property);
	}

	public DataLeakDetectedException(String message, Throwable cause, Property... property) {
		super(SYS_CODE, message, cause, property);
	}

}
