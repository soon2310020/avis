package com.emoldino.api.common.resource.base.masterdata.service;

import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;

import saleson.api.company.CompanyRepository;
import saleson.api.counter.CounterRepository;
import saleson.api.machine.MachineRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.terminal.TerminalRepository;

@Service
public class MasterDataService {

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void adjustBatch() {
		JobUtils.runIfNotRunning("masterdata.adjustBatch", new JobOptions().setClustered(true), () -> {

			DataUtils.runBatch(CompanyRepository.class, //
					Q.company.companyCode.eq("EM - SO").and(Q.company.isEmoldino.isFalse()), //
					Sort.by("id"), 100, false, company -> {
						TranUtils.doNewTran(() -> {
							company.setEmoldino(true);
							BeanUtils.get(CompanyRepository.class).save(company);
						});
					});

			DataUtils.runBatch(TerminalRepository.class, //
					Q.terminal.location.company.isNotNull().and(Q.terminal.companyId.ne(Q.terminal.location.companyId)), //
					Sort.by("id"), 100, false, terminal -> {
						TranUtils.doNewTran(() -> {
							terminal.setCompanyId(terminal.getLocation().getCompanyId());
							BeanUtils.get(TerminalRepository.class).save(terminal);
						});
					});

			DataUtils.runBatch(CounterRepository.class, //
					Q.counter.location.company.isNotNull().and(Q.counter.companyId.ne(Q.counter.location.companyId)), //
					Sort.by("id"), 100, false, counter -> {
						TranUtils.doNewTran(() -> {
							counter.setCompanyId(counter.getLocation().getCompanyId());
							BeanUtils.get(CounterRepository.class).save(counter);
						});
					});

			DataUtils.runBatch(MoldRepository.class, //
					Q.mold.location.company.isNotNull().and(Q.mold.companyId.ne(Q.mold.location.companyId)), //
					Sort.by("id"), 100, false, mold -> {
						TranUtils.doNewTran(() -> {
							mold.setCompanyId(mold.getLocation().getCompanyId());
							BeanUtils.get(MoldRepository.class).save(mold);
						});
					});

			DataUtils.runBatch(MachineRepository.class, //
					Q.machine.location.company.isNotNull().and(Q.machine.companyId.ne(Q.machine.location.companyId)), //
					Sort.by("id"), 100, false, machine -> {
						TranUtils.doNewTran(() -> {
							machine.setCompany(machine.getLocation().getCompany());
							BeanUtils.get(MachineRepository.class).save(machine);
						});
					});

		});
	}

}
