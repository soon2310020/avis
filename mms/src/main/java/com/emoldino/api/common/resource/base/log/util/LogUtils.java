package com.emoldino.api.common.resource.base.log.util;

import java.util.Properties;

import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.repository.errorlog.ErrorLog;
import com.emoldino.api.common.resource.base.log.service.error.ErrorLogService;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.exception.SysException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtils {

	public static void putAccessSummaryQuietly(String apiName, long elapsedTime, Throwable t) {
//		try {
//			BeanUtils.get(AccessLogService.class).put(apiName, elapsedTime, t);
//		} catch (Exception e) {
//			log.warn(e.getMessage(), e);
//		}
	}

	public static String toElapsedTimeMillisStr(long elapsedTimeMillis, String causedAt) {
		StringBuilder buf = new StringBuilder();
		buf.append("Elapsed Time Millis: ").append(elapsedTimeMillis);
		buf.append(" at ").append(causedAt);
		return buf.toString();
	}

	public static ErrorType getErrorType(Throwable t) {
		// TODO fill more cases
		if (t == null) {
			return ErrorType.SYS;
		} else if (t instanceof LogicException) {
			return ErrorType.LOGIC;
		} else {
			return ErrorType.SYS;
		}
	}

	public static void saveErrorQuietly(ErrorType errorType, String errorCode, HttpStatus errorStatus, String errorMessage) {
		saveErrorQuietly(errorType, errorCode, errorStatus, errorMessage, (String) null);
	}

	public static void saveErrorQuietly(ErrorType errorType, String errorCode, HttpStatus errorStatus, String errorMessage, String detailedLog) {
		ErrorLog data = new ErrorLog(errorType, errorCode, errorStatus, errorMessage, detailedLog);
		LogUtils.saveErrorQuietly(data);
	}

	public static Long saveErrorQuietly(Throwable th) {
		Throwable t1 = ValueUtils.unwrap(th);

		AbstractException ae = t1 instanceof AbstractException ? ((AbstractException) t1) : new SysException("UNKNOWN", th);

		if (ae.getId() != null && ae.getId() > 0L) {
			return ae.getId();
		}

		ErrorType errorType = getErrorType(t1);
		HttpStatus errorStatus = HttpUtils.getStatus(t1, null);
		saveErrorQuietly(errorType, ae.getSysCode(), errorStatus, ae.getMessage(), ae);
		return ae.getId();
	}

	public static AbstractException saveErrorQuietly(ErrorType errorType, String errorCode, HttpStatus errorStatus, String errorMessage, Throwable t) {
		ErrorLog data = new ErrorLog(errorType, errorCode, errorStatus, errorMessage);

		if (t != null) {
			try {
				Throwable t1 = t;
				int i = 0;
				while (t1.getCause() != null) {
					if (i++ > 5) {
						break;
					}
					t1 = t1.getCause();
				}
				StringBuilder buf = new StringBuilder();
				String cause = appendAndGetCause(buf, data.getErrorType(), data.getErrorCode(), data.getErrorStatus(), data.getErrorMessage(), t1);
				data.setErrorCause(ValueUtils.abbreviate(cause, 125));
				data.setDetailedLog(buf.toString());
			} catch (Exception e1) {
				// Skip
				log.warn(e1.getMessage(), e1);
			}
		}

		AbstractException ae = (t instanceof AbstractException) ? ((AbstractException) t) : new SysException("UNKNOWN", t);
		if (ae.getId() != null && ae.getId() > 0L) {
			return ae;
		}

		Long errorId = LogUtils.saveErrorQuietly(data);
		ae.setId(errorId);
		if ("developer".equals(ConfigUtils.getProfile())) {
			ae.setDetailedLog(data.getErrorMessage());
		}
		return ae;
	}

	private static Long saveErrorQuietly(ErrorLog data) {
		if (data == null) {
			return null;
		}

		try {
			TranUtils.doNewTran(() -> BeanUtils.get(ErrorLogService.class).save(data));
		} catch (Exception e1) {
			// Skip
			log.warn(e1.getMessage(), e1);
		}

		return data.getId();
	}

	private static String appendAndGetCause(StringBuilder buf, ErrorType errorType, String errorCode, HttpStatus errorStatus, String errorMessage, Throwable t) {
		buf.append("Error:");
		buf.append(System.lineSeparator()).append("\ttype: ").append(errorType);
		buf.append(System.lineSeparator()).append("\tcode: ").append(errorCode);
		buf.append(System.lineSeparator()).append("\tstatus: ").append(errorStatus);
		buf.append(System.lineSeparator()).append("\tmessage: ").append(errorMessage);
		if (t == null) {
			return null;
		}

		if (t instanceof AbstractException) {
			AbstractException ae = (AbstractException) t;
			if (!ObjectUtils.isEmpty(ae.getProperties())) {
				Properties props = ae.getProperties();
				buf.append(System.lineSeparator()).append("\tproperties:");
				for (Object key : props.keySet()) {
					String value = props.getProperty(key.toString());
					buf.append(System.lineSeparator()).append("\t\t").append(key).append("=").append(value);
				}
			}
		}

		String cause = appendDetailLogAndGetCause(buf, t);
		return cause;
	}

	private static String appendDetailLogAndGetCause(StringBuilder buf, Throwable t) {
		if (t == null) {
			return null;
		}
		boolean selfThrown = t.getCause() == null;

		buf.append(System.lineSeparator()).append(t.getClass().getName()).append(": ").append(t.getMessage());
		String prevFirstLine = null;

		Throwable th = t;
		String cause = null;
		do {
			cause = th.getClass().getName();
			prevFirstLine = ValueUtils.appendTrace(buf, selfThrown, prevFirstLine, th.getStackTrace(), null);
		} while ((th = th.getCause()) != null);

		return cause;
	}

}
