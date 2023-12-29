package com.emoldino.framework.integration.hibernate.stat;

import java.util.Set;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.stat.internal.StatisticsImpl;
import org.springframework.http.HttpStatus;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class EmoldinoStatisticsImpl extends StatisticsImpl {
	public EmoldinoStatisticsImpl() {
		super();
	}

	public EmoldinoStatisticsImpl(SessionFactoryImplementor sessionFactory) {
		super(sessionFactory);
	}

	private static final Set<String> EXC = ValueUtils.asSet(EmoldinoStatisticsImpl.class.getName());

	@Override
	public void queryExecuted(String hql, int rows, long time) {
		super.queryExecuted(hql, rows, time);
		long threshold = ConfigUtils.getSqlLongElapsedTimeMillisLogThreshold();
		if (threshold > 0L && threshold < time) {
			String logStr = LogUtils.toElapsedTimeMillisStr(time, ThreadUtils.getPropApiName() + ".sql");
			StringBuilder buf = new StringBuilder(hql);
			buf.append(System.lineSeparator());
			ValueUtils.appendTrace(buf, true, null, Thread.currentThread().getStackTrace(), EXC);
			LogUtils.saveErrorQuietly(ErrorType.SYS, "SQL_LONG_ELAPSED_TIME", HttpStatus.SERVICE_UNAVAILABLE, logStr, buf.toString());
			if (ConfigUtils.isSqlLongElapsedTimeLogTrace()) {
				log.info(buf.toString());
			}
		}
	}

}
