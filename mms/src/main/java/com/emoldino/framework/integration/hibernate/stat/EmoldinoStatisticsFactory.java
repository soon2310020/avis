package com.emoldino.framework.integration.hibernate.stat;

import org.hibernate.engine.spi.*;
import org.hibernate.stat.spi.*;

public class EmoldinoStatisticsFactory implements StatisticsFactory {

	@Override
	public StatisticsImplementor buildStatistics(SessionFactoryImplementor sessionFactory) {
		return new EmoldinoStatisticsImpl(sessionFactory);
	}

}
