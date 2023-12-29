package com.stg.repository;

import com.stg.entity.InsuranceContract;
import com.stg.service.dto.insurance.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface InsuranceContractRepository extends JpaRepository<InsuranceContract, Long> {
    String SELECT_INSURANCE_CONTRACT = "select ic.id, " +
            "c.mb_id as mbId,  " +
            "c.full_name as fullName,   " +
            "ic.transaction_id as mbTransactionId,  " +
            "ic.mb_reference_number as mbFt,  " +
            "bpob.ft_number as micFtNumber,  " +
            "case " +
                "when (bpob.mic_contract_num is not null) then bpob.mic_contract_num " +
                "else ic.mic_contract_num end as micContractNum, " +
            "bpob2.ft_number as mbalFtNumber,  " +
            "ic.mbal_policy_number as mbalPolicyNumber,  " +
            "ip.package_name as packageName, " +
            "ic.str_insurance_fee as strInsuranceFee,  " +
            "ic.package_type as packageType,  " +
            "bpob.\"type\" as micType, bpob2.\"type\" as mbalType, ic.mbal_app_no as mbalAppNo, s.gain as segment, " +
            "ic.mbal_fee_payment_time as mbalFeePaymentTime," +
            "ipm.insurance_request_id as insuranceRequestId ";
    String FILTER_INSURANCE_CONTRACT = "FROM insurance_contract ic  " +
            "left join insurance_package ip on ip.id = ic.insurance_package_id  " +
            "join customer c on c.id = ic.customer_id  " +
            "left join segment s on c.segment_id = s.id  " +
            "left join baas_pay_on_behalf bpob on bpob.mb_transaction_id = ic.transaction_id and bpob.\"type\" ='MIC'  " +
            "left join baas_pay_on_behalf bpob2 on bpob2.mb_transaction_id = ic.transaction_id and bpob2.\"type\" ='MBAL'  " +
            "left join insurance_payment ipm on ic.transaction_id = ipm.transaction_id  " +
            "where package_type in ('FIX_COMBO', 'FREE_STYLE', 'FLEXIBLE')  " +
            "and (:query = '' " +
            "    or (c.full_name ILIKE CONCAT('%', :query, '%'))  " +
            "    or (ic.transaction_id ILIKE CONCAT('%', :query, '%'))  " +
            "    or (ic.mb_reference_number ILIKE CONCAT('%', :query, '%'))  " +
            "    or (bpob.ft_number ILIKE CONCAT('%', :query, '%'))  " +
            "    or (ic.mic_contract_num ILIKE CONCAT('%', :query, '%'))  " +
            "    or (bpob2.ft_number ILIKE CONCAT('%', :query, '%'))  " +
            "    or (ic.mbal_app_no ILIKE CONCAT('%', :query, '%'))  " +
            ")  " +
            "and (:segment = '' OR (s.gain ILIKE CONCAT('%', :segment, '%'))) " +
            "and ic.transaction_id is not null " +
            "and case " +
            "   when :filterUrStyle = 'Yes' then (ic.insurance_package_id in (:filterIdPackage) or ic.insurance_package_id is null) " +
            "   when :filterUrStyle = 'No' then (ic.insurance_package_id in (:filterIdPackage)) " +
            "   when :filterUrStyle = '' then 1=1 " +
            "end " +
            "and (:category = '' or category = :category) " ;

    String SELECT_INSURANCE_CONTRACT_THIRD_PARTY = "select " +
            "ic.id, " +
            "c.mb_id as mbId,  " +
            "c.full_name as fullName,   " +
            "ic.mbal_app_no as mbalAppNo, " +
            "ic.mbal_policy_number as mbalPolicyNumber,  " +
            "ic.mic_contract_num as micContractNum, " +
            "pm.product_name as productName, " +
            "ic.mbal_amount as strInsuranceFee, " +
            "ic.mbal_fee_payment_time as mbalFeePaymentTime, " +
            "ic.status, " +
            "ic.\"source\" ";

    String FROM_WHERE_INSURANCE_CONTRACT_THIRD_PARTY = " FROM insurance_contract ic  " +
            "join customer c on c.id = ic.customer_id " +
            "join product_mbal pm on pm.insurance_contract_id = ic.id and pm.main = true  " +
            "where (:query = '' " +
            "    or (c.full_name ILIKE CONCAT('%', :query, '%'))  " +
            "    or (ic.mbal_app_no ILIKE CONCAT('%', :query, '%'))    " +
            "    or (ic.mbal_policy_number ILIKE CONCAT('%', :query, '%'))    " +
            "    or (ic.mic_contract_num ILIKE CONCAT('%', :query, '%'))    " +
            "    or (pm.product_name ILIKE CONCAT('%', :query, '%'))    " +
            ")  " +
            "and ic.source in (:sources)  " ;

    @Query(value = SELECT_INSURANCE_CONTRACT + FILTER_INSURANCE_CONTRACT +
            "ORDER BY ic.id DESC OFFSET (:page - 1) * :size LIMIT :size", nativeQuery = true)
    List<InsuranceContractVo> listInsuranceContract(@Param("page") int page,
                                                  @Param("size") int size,
                                                  @Param("query") String query,
                                                  @Param("segment") String segment,
                                                  @Param("filterIdPackage") Set<Integer> filterIdPackage,
                                                  @Param("filterUrStyle") String filterUrStyle,
                                                  @Param("category") String category);

    @Query(value = "Select count (ic.id) " + FILTER_INSURANCE_CONTRACT, nativeQuery = true)
    long totalInsuranceContract(@Param("query") String query,
                                @Param("segment") String segment,
                                @Param("filterIdPackage") Set<Integer> filterIdPackage,
                                @Param("filterUrStyle") String filterUrStyle,
                                @Param("category") String category);

    String SELECT_CONTRACT_BY_MB_ID = "SELECT ic.* FROM insurance_contract ic " +
            "left join insurance_package ip on ip.id = ic.insurance_package_id " +
            "join customer c on c.id = ic.customer_id " +
            "where (c.mb_id =:mbId and (:lastId IS NULL OR ic.id > :lastId)) " ;
    String WHERE_CLAUSE_SELECT_CONTRACT_BY_MB_ID = " and (:query = '' " +
            "    or (ic.mbal_policy_number ILIKE CONCAT('%', :query, '%'))    " +
            "    or (ic.mbal_app_no ILIKE CONCAT('%', :query, '%'))    " +
            "    or (ic.mic_contract_num ILIKE CONCAT('%', :query, '%'))    " +
            "    or case " +
            "       when ic.package_type = 'FIX_COMBO' or ic.package_type = 'FREE_STYLE' or ic.package_type = 'FLEXIBLE' then ip.package_name ILIKE CONCAT('%', :query, '%') " +
            "       when ic.package_type = 'MBAL' then 'Bảo hiểm MB Ageas Life' ILIKE CONCAT('%', :query, '%') " +
            "       when ic.package_type = 'ONEID' then 'Bảo hiểm MIC' ILIKE CONCAT('%', :query, '%') " +
            "    end    " +
            ")  " +
            " order by ic.id desc";

    @Query(value = SELECT_CONTRACT_BY_MB_ID +
            "and (:packageType = '' or ic.package_type = :packageType) " +
            WHERE_CLAUSE_SELECT_CONTRACT_BY_MB_ID, nativeQuery = true)
    List<InsuranceContract> insuranceContractByMbId(@Param("mbId") String mbId,
                                                    @Param("lastId") Long lastId,
                                                    @Param("packageType") String packageType,
                                                    @Param("query") String query,
                                                    Pageable pageable);

    @Query(value =
            "SELECT " +
                "ic.* " +
            "FROM insurance_contract ic " +
            "left join insurance_package ip on ip.id = ic.insurance_package_id " +
            "join customer c on c.id = ic.customer_id " +
                "where c.mb_id = :#{#filterParam.mbId} " +
                "and (:#{#filterParam.status.getText()} = '' or (ic.status is null and 'PENDING' = :#{#filterParam.status.getText()}) or ic.status = :#{#filterParam.status.getText()}) " +
                " and (:#{#filterParam.query} = '' " +
                "    or (ic.mbal_policy_number ILIKE CONCAT('%', :#{#filterParam.query}, '%'))    " +
                "    or (ic.mbal_app_no ILIKE CONCAT('%', :#{#filterParam.query}, '%'))    " +
                "    or (ic.mic_contract_num ILIKE CONCAT('%', :#{#filterParam.query}, '%'))    " +
                "    or case " +
                "       when ic.package_type = 'FIX_COMBO' or ic.package_type = 'FREE_STYLE' or ic.package_type = 'FLEXIBLE' then ip.package_name ILIKE CONCAT('%', :#{#filterParam.query}, '%') " +
                "       when ic.package_type = 'MBAL' then 'Bảo hiểm MB Ageas Life' ILIKE CONCAT('%', :#{#filterParam.query}, '%') " +
                "       when ic.package_type = 'ONEID' then 'Bảo hiểm MIC' ILIKE CONCAT('%', :#{#filterParam.query}, '%') " +
                "    end    " +
                ")  order by ic.id desc", nativeQuery = true)
    List<InsuranceContract> insuranceContractByMbIdAndCombo(@Param("filterParam") FilterContractParam filterParam,
                                                            Pageable pageable);

    @Query(value = SELECT_INSURANCE_CONTRACT + FILTER_INSURANCE_CONTRACT +
            "ORDER BY ic.id ASC", nativeQuery = true)
    List<InsuranceContractVo> listInsuranceContractNoPaging(@Param("query") String query,
                                                          @Param("segment") String segment,
                                                          @Param("filterIdPackage") Set<Integer> filterIdPackage,
                                                          @Param("filterUrStyle") String filterUrStyle,
                                                          @Param("category") String category);

    @Query(value = "SELECT count(*) FROM insurance_contract ic " +
            "WHERE date_trunc('week',mic_issue_date) = date_trunc('week',CURRENT_TIMESTAMP); ", nativeQuery = true)
    long totalInsuranceContractCurrentWeek();

    @Query(value = "SELECT count(*) FROM insurance_contract " +
            "WHERE date_trunc('week',mic_issue_date) = date_trunc('week',CURRENT_TIMESTAMP) " +
            "AND extract(isodow from mic_issue_date) = :dayNum ", nativeQuery = true)
    Long totalContractCurrentWeekByDayOfWeek(@Param("dayNum") long dayNum);

    @Query(value = "SELECT * FROM insurance_contract ic " +
            "join insurance_package ip on ic.insurance_package_id = ip.id " +
            "WHERE date_trunc('week',mic_issue_date) = date_trunc('week',CURRENT_TIMESTAMP) " +
            "and package_type = :packageType " +
            "AND (:category = '' OR ip.category = :category)", nativeQuery = true)
    List<InsuranceContract> listContractCurrentWeekByPackage(@Param("packageType") String packageType,
                                                             @Param("category") String category);

    @Query(value = "SELECT ic.* FROM insurance_contract ic " +
            "where package_type IN (:packageType) " +
            "order by mic_issue_date desc LIMIT 10", nativeQuery = true)
    List<InsuranceContract> listContractOrderByCreationTime(@Param("packageType") List<String> packageType);

    InsuranceContract findByTransactionId(String transactionId);

    @Query(value = "select ic.transaction_id as transactionId, ic.mb_reference_number as mbReferenceNumber, bpobMic.ft_number as micFtNumber, bpobMb.ft_number as mbalFtNumber " +
            "from insurance_contract ic " +
            "left join baas_pay_on_behalf bpobMic on bpobMic.mb_transaction_id = ic.transaction_id and bpobMic.\"type\" ='MIC' and ic.transaction_id is not null " +
            "left join baas_pay_on_behalf bpobMb on bpobMb.mb_transaction_id = ic.transaction_id and bpobMb.\"type\" ='MBAL' and ic.transaction_id is not null " +
            "where ic.transaction_id in (:transactionIds) ", nativeQuery = true)
    List<TransactionContractDto> listTransactionContract(@Param("transactionIds") Set<String> transactionIds);

    @Query(value = "select ic.id as contractId, ic.transaction_id as transactionId, ip.insurance_request_id as insuranceRequestId, ir.illustration_number as illustrationNumber " +
            "from insurance_contract ic " +
            "left join insurance_payment ip on ic.transaction_id = ip.transaction_id " +
            "left join insurance_request ir on ip.insurance_request_id = ir.id " +
            "where ic.id in (:contractIds)", nativeQuery = true)
    List<ContractIllustrationNumberVo> listContractIllustrationNumber(@Param("contractIds") List<Long> contractIds);

    @Query(value = "SELECT * FROM insurance_contract ic " +
            "join insurance_package ip on ic.insurance_package_id = ip.id " +
            "WHERE date_trunc('week',mic_issue_date) = date_trunc('week',CURRENT_TIMESTAMP) " +
            "AND (:category = '' OR ip.category = :category)", nativeQuery = true)
    List<InsuranceContract> listContractCurrentWeekByCategory(@Param("category") String category);

    @Query(value = "select * from insurance_contract ic " +
            "where ic.mbal_app_no = :mbalAppNo " +
            "order by id desc " +
            "limit 1 offset 0 ", nativeQuery = true)
    InsuranceContract findByMbalAppNo(String mbalAppNo);

    @Query(value = SELECT_INSURANCE_CONTRACT_THIRD_PARTY + FROM_WHERE_INSURANCE_CONTRACT_THIRD_PARTY +
            "ORDER BY ic.id DESC OFFSET (:page - 1) * :size LIMIT :size", nativeQuery = true)
    List<InsuranceContractThirdPartyVo> listInsuranceContractThirdParty(@Param("page") int page,
                                                                        @Param("size") int size,
                                                                        @Param("query") String query,
                                                                        @Param("sources") List<String> sources);

    @Query(value = "SELECT count(*) " + FROM_WHERE_INSURANCE_CONTRACT_THIRD_PARTY, nativeQuery = true)
    long totalInsuranceContractThirdParty(@Param("query") String query,
                                          @Param("sources") List<String> sources);

    @Query(value = SELECT_INSURANCE_CONTRACT_THIRD_PARTY + FROM_WHERE_INSURANCE_CONTRACT_THIRD_PARTY +
            "ORDER BY ic.id DESC", nativeQuery = true)
    List<InsuranceContractThirdPartyVo> listInsuranceContractThirdPartyNoPaging(@Param("query") String query,
                                                                                @Param("sources") List<String> sources);

    @Query(value = "select ir.id  " +
            "from insurance_contract ic  " +
            "left join insurance_payment ip on ic.transaction_id = ip.transaction_id  " +
            "left join insurance_request ir on ip.insurance_request_id = ir.id  " +
            "where ic.id in (:contractId) ", nativeQuery = true)
    Long findInsuranceRequestIdByContractId(@Param("contractId") Long contractId);

    @Query(value = "select * from insurance_contract ic " +
            "where ic.mbal_policy_number = :mbalPolicyNumber " +
            "order by id desc " +
            "limit 1 offset 0 ", nativeQuery = true)
    InsuranceContract findByMbalPolicyNumber(String mbalPolicyNumber);
}
