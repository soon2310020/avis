package com.stg.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.stg.entity.potentialcustomer.DirectSubmitStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.stg.entity.potentialcustomer.DirectSubmitStatus;
import com.stg.entity.quotation.QuotationCustomer;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.repository.QuotationCustomerRepository;
import com.stg.repository.QuotationHeaderRepository;
import com.stg.repository.RefreshTokenRepository;
import com.stg.service.PotentialCustomerDirectService;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationScheduled {
	private final QuotationHeaderRepository quotationHeaderRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final QuotationCustomerRepository quotationCustomerRepository;

	private final PotentialCustomerDirectService potentialCustomerDirectService;

	//NOTE: remove => because: Không còn trạng thái raw nữa
	//@Scheduled(fixedDelay = 3600 * 1000)
    //@SchedulerLock(name = "Tool_Scheduler_clearQuotation", lockAtLeastFor = "PT5M", lockAtMostFor = "PT15M")
	public void clearQuotation() {
		List<QuotationHeader> quotations = quotationHeaderRepository
				.findByExpiredDate(LocalDateTime.now().minusDays(1));
		for (QuotationHeader q : quotations) {
			QuotationCustomer customer = q.getCustomer();
			if (customer.getQuotationHeader() != null) {
				customer.setQuotationHeader(null);
				quotationCustomerRepository.save(customer);
			}
		}
		quotationHeaderRepository.deleteAll(quotations);
	}

	@Scheduled(fixedDelay = 3600 * 1000)
	@SchedulerLock(name = "Tool_Scheduler_clearRefreshToken", lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")
	public void clearRefreshToken() {
		refreshTokenRepository.deleteExpired(Instant.now().minus(1, ChronoUnit.DAYS));
	}

	@Scheduled(cron = "${schedule.direct-submit-retry}")
	@SchedulerLock(name = "Tool_Scheduler_directSubmitRetry", lockAtLeastFor = "PT1M", lockAtMostFor = "PT5M")
    public void scheduleDirectSubmitRetry() {
	    log.info("[DIRECT-SUBMIT-RETRY-SCHEDULE]--[START] :: Retry submit direct");
        int total = 0;
        int page = 0;
	    try {
            while (true) {
                List<SubmitPotentialCustomerReq> submitRequests = potentialCustomerDirectService.getNextRetry(page++ );
                if (submitRequests.isEmpty()) {
                    break;
                }
                total += submitRequests.size();
                List<Future<DirectSubmitStatus>> futures = submitRequests.stream().map(potentialCustomerDirectService::submit)
                        .collect(Collectors.toList());
                for (Future<DirectSubmitStatus> future : futures) {
					try { //NOSONAR
						future.get();
					} catch (Exception e) { //NOSONAR
						log.error("[DIRECT-SUBMIT-RETRY-SCHEDULE]--[SUB-THREAD]", e);
					}
                }
            }
        } catch (Exception e) { //NOSONAR
			log.error("[DIRECT-SUBMIT-RETRY-SCHEDULE]--[ERROR]", e);
        }
        log.info("[DIRECT-SUBMIT-RETRY-SCHEDULE]--[END] :: totalRecord={}", total);
    }

}
