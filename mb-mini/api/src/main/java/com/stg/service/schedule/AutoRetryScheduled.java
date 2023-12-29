package com.stg.service.schedule;

import com.stg.entity.InsurancePayment;
import com.stg.entity.customer.Customer;
import com.stg.repository.CustomerRepository;
import com.stg.repository.InsurancePaymentRepository;
import com.stg.service.AutoDebitPaymentService;
import com.stg.service3rd.baas.dto.code.ErrorCodeRegisterAutoDebit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AutoRetryScheduled {
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final CustomerRepository customerRepository;
    private final AutoDebitPaymentService autoDebitPaymentService;

    /**
     * Retry 10 lần, mỗi 10 phút
     */
    @Scheduled(cron = "0 */10 * * * ?")
    @SchedulerLock(name = "Payment_AutoRetryRegisterAutoDebit")
    public void scheduleAutoRetryRegisterAutoDebit() {
        log.info("[REGISTER-AUTO-DEBIT-SCHEDULE]--[START] : Retry auto register Auto-Debit");
        List<InsurancePayment> insurancePayments = insurancePaymentRepository.collectPaymentForAutoDebitWithoutStatus(ErrorCodeRegisterAutoDebit.OK.getCode(), 10);

        int numOfSuccess = 0;
        for (InsurancePayment insurancePayment : insurancePayments) {
            try {
                Customer customer = customerRepository.findByInsurancePaymentId(insurancePayment.getId());
                autoDebitPaymentService.createAutoDebitNonOTP(customer, insurancePayment, null);
                numOfSuccess++;
            } catch (Exception ex) {
                log.error("[REGISTER-AUTO-DEBIT-SCHEDULE]--[ERROR] : Error while retry auto register auto-debit", ex);
            }
        }
        log.info("[REGISTER-AUTO-DEBIT-SCHEDULE]--[END  ] : totalRecord={}, totalSuccess={}", insurancePayments.size(), numOfSuccess);
    }
}
