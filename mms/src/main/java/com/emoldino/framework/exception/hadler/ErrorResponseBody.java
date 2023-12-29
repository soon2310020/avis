package com.emoldino.framework.exception.hadler;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ErrorResponseBody {
	private int status;
	private String error;
	private String code;
	private String message;
	private Long id;
	private Properties properties;
	private String detailedLog;
}
