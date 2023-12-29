package com.emoldino.api.analysis.resource.composite.datcol.service.accelerationdata;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Service
public class DatColFillDataService {

	public void fillMoldIdAtDataAcceleration() {
		JobUtils.runIfNotRunning("DatColFillDataService.fillMoldId", new JobOptions().setClustered(true), () -> {
			fillMoldId();
		});
	}

	public void fillMoldId() {
		QDataAcceleration daTable = QDataAcceleration.dataAcceleration;
		BooleanBuilder filter = new BooleanBuilder() //
				.and(daTable.counterId.startsWith("EMA")) //
				.and(daTable.moldId.isNull());

		DataUtils.runBatch(DataAccelerationRepository.class, filter, Sort.by("updatedAt"), 100, true, (dataAcceleration) -> {
			TranUtils.doNewTran(() -> {
				Long moldId = BeanUtils.get(JPAQueryFactory.class)//
						.select(Q.statistics.moldId) //
						.from(Q.statistics) //
						.leftJoin(Q.cdata).on(Q.cdata.id.eq(Q.statistics.cdataId)) //
						.where(Q.statistics.ci.startsWith("EMA") //
								.and(Q.statistics.moldId.isNotNull()) //
								.and(Q.cdata.sn.eq(dataAcceleration.getDataId().intValue()))) //
						.fetchFirst();

				if (moldId != null) {
					dataAcceleration.setMoldId(moldId);
					BeanUtils.get(DataAccelerationRepository.class).saveAndFlush(dataAcceleration);
				}
			});
		});
	}
}
