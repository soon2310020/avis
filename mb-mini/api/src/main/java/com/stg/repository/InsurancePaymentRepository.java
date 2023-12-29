package com.stg.repository;

import com.stg.entity.InsurancePayment;
import com.stg.service.dto.insurance.InsurancePaymentVo;
import com.stg.service.dto.insurance.MicPaymentVo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InsurancePaymentRepository extends JpaRepository<InsurancePayment, Long> {
    String FILTER_PAYMENT = "join customer c on c.id = ipay.customer_id " +
            "where (:query = '' OR (ipay.root_account ILIKE CONCAT('%', :query, '%')) " +
            "or (c.full_name ILIKE CONCAT('%', :query, '%')) " +
            "or (c.mb_id ILIKE CONCAT('%', :query, '%'))" +
            "or (ipay.rm_code ILIKE CONCAT('%', :query, '%'))) " +
            "and (:controlState = '' OR ipay.control_state ILIKE CONCAT('%', :controlState, '%')) " +
            "and ((:dateFrom = '' and :dateTo = '') or (payment_time >= date(:dateFrom) and payment_time < (date(:dateTo) + interval '1 day'))) " +
            "and ipay.transaction_id is not null ";

    String SELECT_FOR_INSURANCE_PAYMENT_VO = "SELECT ip.id, c.mb_id as mbId, c.full_name as fullName, ip.tran_status as tranStatus, ic.transaction_id as mbTransactionId, " +
            "ic.mb_reference_number as mbFt, " +
            "bpob.amount as micInsuranceFee, " +
            "case " +
                "when (bpob.mic_contract_num is not null) then bpob.mic_contract_num " +
                "when (ic.mic_contract_num is not null) then ic.mic_contract_num " +
                "else  ip.mic_contract_num end as micContractNum, " +
            "bpob.ft_number as micFtNumber, bpob.status as baasMicStatus, " +
            "ip.mbal_insurance_fee as mbalInsuranceFee, ic.mbal_app_no as mbalAppNo, bpob2.ft_number as mbalFtNumber, " +
            "case " +
            "when (bpob2.status = 'FAIL' or bpob2.mbal_hook_status is false) then 'FAIL' " +
            "else 'SUCCESS' end as baasMbalStatus, " +
            "ip.control_state as controlState, " +
            "bpob.\"type\" as micType, bpob2.\"type\" as mbalType, ip.rm_code as rmCode, ip.rm_name as rmName, ip.rm_email as rmEmail, ip.rm_phone_number as rmPhoneNumber, " +
            "ip.payment_time as paymentTime, ip.total_fee as totalFee, ip.periodic_fee_payment as periodicFee, " +
            "ip.ic_name as icName, ip.ic_code as icCode, ip.support_name as supportName, ip.support_code as supportCode, ip.support_phone_number as supportPhoneNumber, ip.support_email as supportEmail, " +
            "ip.branch_code as branchCode, ip.branch_name as branchName, ip.department_name as departmentName, ip.department_code as departmentCode," +
            "ic.mbal_fee_payment_time as mbalFeePaymentTime, " +
            "ip.normal, " +
            "ip.auto_pay as autoPay, " +
            "ip.installment_status as installmentStatus, " +
            "ip.\"period\", " +
            "ip.fees_pay_able as feesPayable, " +
            "ip.periodic_conversion_fee as periodicConversionFee, " +
            "ip.funding_source as fundingSource," +
            "ic.package_type as packageType," +
            "adpt.registered as autoPayRegistered," +
            "adpt.status_message as autoPayStatus," +
            "ip.insurance_request_id as insuranceRequestId, " +
            "ip.installment_error_code as installmentErrorCode," +
            "c.last_updated as customerLastUpdated ," +
            "c.managing_unit as managingUnit," +
            "c.mb_id as cif ";

    String COUNT_FOR_INSURANCE_PAYMENT_VO = "SELECT count(distinct ip.id) ";

    String FROM = "from insurance_payment ip " +
            "join insurance_contract ic on ic.transaction_id = ip.transaction_id " +
            "join customer c on c.id = ip.customer_id " +
            "left join baas_pay_on_behalf bpob on bpob.mb_transaction_id = ic.transaction_id and bpob.\"type\" ='MIC' " +
            "left join baas_pay_on_behalf bpob2 on bpob2.mb_transaction_id = ic.transaction_id and bpob2.\"type\" ='MBAL' " +
            "left join insurance_package ipk on ipk.id = ic.insurance_package_id  " +
            "left join auto_debit_payment adpt on ip.auto_debit_payment_id = adpt.id  ";

    String WHERE_FILTER_PAYMENT = "where (:query = '' OR (c.full_name ILIKE CONCAT('%', :query, '%')) " +
            "or (c.mb_id ILIKE CONCAT('%', :query, '%'))" +
            "or (ic.mbal_app_no ILIKE CONCAT('%', :query, '%'))" +
            "or (ip.rm_code ILIKE CONCAT('%', :query, '%')) " +
            "or (ic.transaction_id ILIKE CONCAT('%', :query, '%')) " +
            "or (ic.mb_reference_number ILIKE CONCAT('%', :query, '%')) " +
            "or (ic.mic_contract_num ILIKE CONCAT('%', :query, '%')) " +
            "or (ip.mic_contract_num ILIKE CONCAT('%', :query, '%')) " +
            "or (bpob.mic_contract_num ILIKE CONCAT('%', :query, '%')) ) " +
            "and (:controlState = '' OR ip.control_state ILIKE CONCAT('%', :controlState, '%')) " +
            "and (:status = '' OR ip.tran_status =:status) " +
            "and (:micStatus = '' OR bpob.status =:micStatus) " +
            "and (bpob2.mbal_hook_status in (:mbalStatus)) " +
            "and ((:dateFrom = '' and :dateTo = '') or (ip.payment_time >= date(:dateFrom) and ip.payment_time < (date(:dateTo) + interval '1 day'))) " +
            "and ip.transaction_id is not null " +
            "and (:category = '' or category = :category) " +
            "and (:packageName = '' or ipk.package_name = :packageName) " +
            //"and (:mbalAppNo = '' or ic.mbal_app_no = :mbalAppNo) " +
            "and case " +
            "   when :installmentStatus = '' then 1=1 " +
            "   when :installmentStatus = 'true' then ip.installment_status = 'INSTALLMENT_SUCCESS' " +
            "   else (ip.installment_status != 'INSTALLMENT_SUCCESS' and ip.installment_status is not null) " +
            "end " +
            "and case " +
            "   when :paymentType = '' then 1=1 " +
            "   when :paymentType = 'ACCOUNT' then ip.normal = true and (ip.funding_source = 'ACCOUNT' or ip.funding_source is null) " +
            "   when :paymentType = 'CARD' then ip.normal = true and ip.funding_source = 'CARD' " +
            "   when :paymentType = 'INSTALLMENT' then ip.installment = true and  ip.normal = false " +
            "   when :paymentType = 'AUTO_PAY_ACCOUNT' then ip.auto_pay = true and (ip.funding_source = 'ACCOUNT' or ip.funding_source is null) " +
            "   when :paymentType = 'AUTO_PAY_CARD' then ip.auto_pay = true and ip.funding_source = 'CARD' " +
            "end " +
            "and case " +
            "   when :autoPayStatus = '' then 1=1 " +
            "   when :autoPayStatus = 'true' then adpt.status_code = '000' " +
            "   when :autoPayStatus = 'false' then adpt.status_code != '000' " +
            "end ";

    String WHERE_FILTER_PENDING_AND_WAITING_PAYMENT = "WHERE (ip.tran_status = 'WAITING' OR ip.tran_status = 'PENDING') " +
            "AND (:query = '' or (ip.transaction_id ILIKE CONCAT('%', :query, '%')) " +
            "or (c.full_name ILIKE CONCAT('%', :query, '%')) " +
            "or (ip.mbal_app_no ILIKE CONCAT('%', :query, '%')) " +
            "or (c.identification ILIKE CONCAT('%', :query, '%')) " +
            "or (c.mb_id ILIKE CONCAT('%', :query, '%')) " +
            " ) " +
            "AND (:status = '' or (ip.tran_status = :status)) " +
            "AND ((:dateFrom = '' and :dateTo = '') or (ip.payment_time >= date(:dateFrom) and ip.payment_time < (date(:dateTo) + interval '1 day'))) ";

    String ORDER_BY_ID = "ORDER BY ip.id DESC ";
    String WHERE_BY_ID = "WHERE ip.id =:id";
    String ORDER_BY_TRAN_STATUS_AND_ID = "ORDER BY ip.tran_status DESC, ip.id DESC";

    @Query(value = SELECT_FOR_INSURANCE_PAYMENT_VO + FROM + WHERE_FILTER_PAYMENT + ORDER_BY_ID, nativeQuery = true)
    List<InsurancePaymentVo> listInsurancePayments(@Param("query") String query,
                                                   @Param("dateFrom") String dateFrom,
                                                   @Param("dateTo") String dateTo,
                                                   @Param("status") String status,
                                                   @Param("micStatus") String micStatus,
                                                   @Param("mbalStatus") List<Boolean> mbalStatus,
                                                   @Param("controlState") String controlState,
                                                   @Param("category") String category,
                                                   @Param("packageName") String packageName,
                                                   @Param("installmentStatus") String installmentStatus,
                                                   @Param("paymentType") String paymentType,
                                                   //@Param("mbalAppNo") String mbalAppNo,
                                                   @Param("autoPayStatus") String autoPayStatus,
                                                   Pageable pageable);

    @Query(value = SELECT_FOR_INSURANCE_PAYMENT_VO + FROM + WHERE_BY_ID,  nativeQuery = true)
    List<InsurancePaymentVo> detailPayment(@Param("id") Long id);

    @Query(value = COUNT_FOR_INSURANCE_PAYMENT_VO + FROM + WHERE_FILTER_PAYMENT, nativeQuery = true)
    long totalPayments(@Param("query") String query,
                       @Param("dateFrom") String dateFrom,
                       @Param("dateTo") String dateTo,
                       @Param("status") String status,
                       @Param("micStatus") String micStatus,
                       @Param("mbalStatus") List<Boolean> mbalStatus,
                       @Param("controlState") String controlState,
                       @Param("category") String category,
                       @Param("packageName") String packageName,
                       @Param("installmentStatus") String installmentStatus,
                       @Param("paymentType") String paymentType,
                       @Param("autoPayStatus") String autoPayStatus);

    @Query(value = "SELECT ipay.* FROM insurance_payment ipay " + FILTER_PAYMENT +
            "ORDER BY ipay.id ASC ", nativeQuery = true)
    List<InsurancePayment> listPaymentsNoPaging(@Param("query") String query,
                                                @Param("dateFrom") String dateFrom,
                                                @Param("dateTo") String dateTo,
                                                @Param("controlState") String controlState);

    @Query(value = SELECT_FOR_INSURANCE_PAYMENT_VO + FROM + WHERE_FILTER_PAYMENT, nativeQuery = true)
    List<InsurancePaymentVo> listPaymentVosNoPaging(@Param("query") String query,
                                                    @Param("dateFrom") String dateFrom,
                                                    @Param("dateTo") String dateTo,
                                                    @Param("status") String status,
                                                    @Param("micStatus") String micStatus,
                                                    @Param("mbalStatus") List<Boolean> mbalStatus,
                                                    @Param("controlState") String controlState,
                                                    @Param("category") String category,
                                                    @Param("packageName") String packageName,
                                                    @Param("installmentStatus") String installmentStatus,
                                                    @Param("paymentType") String paymentType,
                                                    @Param("autoPayStatus") String autoPayStatus);

    @Query(value = "SELECT count(*) FROM insurance_payment ip " +
            "WHERE date_trunc('week',payment_time) = date_trunc('week',CURRENT_TIMESTAMP) " +
            "and tran_status = :status ", nativeQuery = true)
    long totalPaymentCurrentWeekByStatus(@Param("status") String status);

    @Query(value = "SELECT count(*) FROM insurance_payment ip " +
            "WHERE date_trunc('week',payment_time) = date_trunc('week',CURRENT_TIMESTAMP) ", nativeQuery = true)
    long totalPaymentCurrentWeek();

    InsurancePayment findByTransactionId(String transactionId);

    @Query(value = "SELECT * FROM insurance_payment ip " +
            "WHERE  ip.installment = true " +
            "and (ip.installment_status = 'INSTALLMENT_FAILED' or ip.installment_status = 'INSTALLMENT_UNQUALIFIED') " +
            "and way4_docs_id is not null " +
            "and ip.version < 10 ", nativeQuery = true)
    List<InsurancePayment> collectInstallmentPayment();

    @Query(value = "SELECT * FROM insurance_payment ip " +
            "INNER JOIN auto_debit_payment adpt on adpt.id = ip.auto_debit_payment_id " +
            "WHERE ip.auto_pay = true " +
            "and adpt.status_code != :status " +
            "and adpt.version < :maxRetry + 1 ", nativeQuery = true)
    List<InsurancePayment> collectPaymentForAutoDebitWithoutStatus(@Param("status") String status, @Param("maxRetry") int maxRetry);

    @Query(value = "SELECT * FROM insurance_payment ip where ip.insurance_request_id =:insuranceRequestId ", nativeQuery = true)
    InsurancePayment findByInsuranceRequest(@Param("insuranceRequestId") Long insuranceRequestId);

    @Query(value = "SELECT ip.* FROM insurance_payment ip " +
            "join customer c on c.id = ip.customer_id " +
            "WHERE  ip.installment = true " +
            "and c.mb_id = :mbId " +
            "and ip.installment_popup  = 'SHOW' " +
            "and ip.payment_time < now() - interval '2 hours' " +
            "and way4_docs_id is not null " +
            "union " +
            "SELECT ip.* FROM insurance_payment ip " +
            "join customer c on c.id = ip.customer_id " +
            "WHERE ip.installment = false " +
            "and c.mb_id = :mbId " +
            "and ip.installment_popup = 'SHOW' " +
            "and installment_status = 'INSTALLMENT_UNQUALIFIED_CANCEL' " +
            "order by payment_time limit 1", nativeQuery = true)
    InsurancePayment retrieveInstallmentShowPopup(@Param("mbId") String mbId);

    @Query(value = "SELECT ip.* FROM insurance_payment ip " +
            "join customer c on c.id = ip.customer_id " +
            "join insurance_contract ic on ic.transaction_id = ip.transaction_id " +
            "WHERE ic.id  = :contractId " +
            "and ip.installment_popup  = 'SHOW' " +
            "and c.mb_id = :mbId ", nativeQuery = true)
    InsurancePayment getInstalmentPayment(@Param("contractId") Long contractId,
                                          @Param("mbId") String mbId);

    @Query(value = "SELECT * FROM insurance_payment ip " +
            "WHERE ip.tran_status = 'PENDING' " +
            "AND (ip.payment_time >= date(:startTime) and ip.payment_time < date(:endTime)) ", nativeQuery = true)
    List<InsurancePayment> retrievedInsurancePaymentPendingInDay(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Query(value = "SELECT count(*) FROM insurance_payment ip join customer c on c.id = ip.customer_id " + WHERE_FILTER_PENDING_AND_WAITING_PAYMENT, nativeQuery = true)
    long countInsurancePaymentWaiting(@Param("query") String query,
                                      @Param("dateFrom") String dateFrom,
                                      @Param("dateTo") String dateTo,
                                      @Param("status") String status);

    @Query(value = "SELECT ip.*, c.managing_unit as managingUnit FROM insurance_payment ip join customer c on c.id = ip.customer_id " + WHERE_FILTER_PENDING_AND_WAITING_PAYMENT + ORDER_BY_TRAN_STATUS_AND_ID, nativeQuery = true)
    List<InsurancePayment> retrievedInsurancePaymentWaiting(@Param("query") String query,
                                                            @Param("dateFrom") String dateFrom,
                                                            @Param("dateTo") String dateTo,
                                                            @Param("status") String status,
                                                            Pageable pageable);

    @Query(value = "SELECT * FROM insurance_payment ip join customer c on c.id = ip.customer_id " + WHERE_FILTER_PENDING_AND_WAITING_PAYMENT, nativeQuery = true)
    List<InsurancePayment> retrievedInsurancePaymentWaitingExport(@Param("query") String query,
                                                                  @Param("dateFrom") String dateFrom,
                                                                  @Param("dateTo") String dateTo,
                                                                  @Param("status") String status);

    @Query(value = "select ip.transaction_id as mbTransactionId, sum(ap.mic_fee) as flexibleMicFee, ip.mic_insurance_fee as comboMicFee " +
            "from insurance_payment ip " +
            "left join additional_product ap on ap.insurance_request_id = ip.insurance_request_id and ap.\"type\" = 'MIC' " +
            "where ip.transaction_id in (:transactionIds) " +
            "group by ip.transaction_id , ip.mic_insurance_fee", nativeQuery = true)
    List<MicPaymentVo> retrieveMicFee(@Param("transactionIds") List<String> transactionIds);

}
