package com.emoldino.framework.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigUtils {
	@Value("${spring.profiles.active:prod}")
	private String profile;

	@Value("${customer.server.name}")
	private String serverName;

	@Value("${app.license.type:t000}")
	private String licenseType;

	@Value("${host.url:http://localhost:8080}")
	private String hostUrl;

	@Value("${http.port:0}")
	private int httpPort;

	@Value("${app.mode:dev}")
	private String mode;

	@Value("${app.central.url:https://central.emoldino.com}")
	private String centralUrl;

	@Value("${app.log.access-summary.enabled:false}")
	private boolean accessSummaryLogEnabled;

	@Value("${app.log.long-elapsed-time-millis.threshold:-1}")
	private long longElapsedTimeMillisLogThreshold;

	@Value("${app.log.sql-long-elapsed-time-millis.threshold:-1}")
	private long sqlLongElapsedTimeMillisLogThreshold;

	@Value("${app.log.sql-long-elapsed-time.trace:false}")
	private boolean sqlLongElapsedTimeLogTrace;

	@Value("${app.account.password-max-failed-attemps:3}")
	private int accountPasswordMaxFailedAttempts;
	@Value("${app.account.password-expiration-duration-days:-1}")
	private int accountPasswordExpirationDurationDays;

	private static ConfigUtils instance;

	public ConfigUtils() {
		instance = this;
	}

	public static String getProfile() {
		return instance.profile;
	}

	public static String getServerName() {
		return instance.serverName;
	}

	public static String getLicenseType() {
		return instance.licenseType;
	}

	public static String getHostUrl() {
		return instance.hostUrl;
	}

	public static int getHttpPort() {
		return instance.httpPort;
	}

	public static boolean isProdMode() {
		return "prod".equalsIgnoreCase(instance.mode);
	}

	public static String getCentralUrl() {
		return instance.centralUrl;
	}

	public static boolean isAccessSummaryLogEnabled() {
		return instance.accessSummaryLogEnabled;
	}

	public static long getLongElapsedTimeMillisLogThreshold() {
		return instance.longElapsedTimeMillisLogThreshold;
	}

	public static long getSqlLongElapsedTimeMillisLogThreshold() {
		return instance.sqlLongElapsedTimeMillisLogThreshold;
	}

	public static boolean isSqlLongElapsedTimeLogTrace() {
		return instance.sqlLongElapsedTimeLogTrace;
	}

	public static int getAccountPasswordMaxFailedAttempts() {
		return instance.accountPasswordMaxFailedAttempts;
	}

	public static int getAccountPasswordExpirationDurationDays() {
		return instance.accountPasswordExpirationDurationDays;
	}

}
