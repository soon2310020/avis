package com.emoldino.api.common.resource.composite.mdtadj.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.QCounter;
import saleson.model.QMold;
import saleson.model.QTerminal;
import saleson.model.Terminal;

@Service
@Transactional(propagation = Propagation.NEVER)
public class MdtAdjService {

	public void post() {
		JobUtils.runIfNotRunning("Mdt.adjust", new JobOptions().setClustered(true), () -> {
			{
				QMold table = Q.mold;
				MoldRepository repo = BeanUtils.get(MoldRepository.class);
				JPQLQuery<Mold> query = BeanUtils.get(JPAQueryFactory.class)//
						.selectFrom(table)//
						.where(table.location.isNotNull().and(table.companyId.ne(table.location.companyId)));
				QueryUtils.applySort(query, table.id.asc());
				DataUtils.runBatch(query, 100, false, (data) -> TranUtils.doNewTran(() -> {
					data.setCompanyId(data.getLocation().getCompanyId());
					repo.save(data);
				}));
			}

			{
				QTerminal table = Q.terminal;
				TerminalRepository repo = BeanUtils.get(TerminalRepository.class);
				JPQLQuery<Terminal> query = BeanUtils.get(JPAQueryFactory.class)//
						.selectFrom(table)//
						.where(table.location.isNotNull().and(table.companyId.ne(table.location.companyId)));
				QueryUtils.applySort(query, table.id.asc());
				DataUtils.runBatch(query, 100, false, (data) -> TranUtils.doNewTran(() -> {
					data.setCompanyId(data.getLocation().getCompanyId());
					repo.save(data);
				}));
			}

			{
				QCounter table = Q.counter;
				CounterRepository repo = BeanUtils.get(CounterRepository.class);
				JPQLQuery<Counter> query = BeanUtils.get(JPAQueryFactory.class)//
						.selectFrom(table)//
						.where(table.location.isNotNull().and(table.companyId.ne(table.location.companyId)));
				QueryUtils.applySort(query, table.id.asc());
				DataUtils.runBatch(query, 100, false, (data) -> TranUtils.doNewTran(() -> {
					data.setCompanyId(data.getLocation().getCompanyId());
					repo.save(data);
				}));
			}
		});
	}

}
