package com.stg.errors;

import com.stg.errors.dto.ErrorDto;
import org.springframework.http.HttpStatus;

public class ErrorUtil {

	private ErrorUtil() {
	}

	static final String DEFAULT_MESSAGE = "Forbidden";

	public static ErrorDto getDefaultErrorDto() {
		ErrorDto errorDto = new ErrorDto();
		errorDto.setHttpStatus(HttpStatus.FORBIDDEN.value());
		errorDto.setMessage(DEFAULT_MESSAGE);

		return errorDto;
	}

}
