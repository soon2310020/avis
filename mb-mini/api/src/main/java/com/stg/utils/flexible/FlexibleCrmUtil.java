package com.stg.utils.flexible;

import com.stg.entity.customer.Customer;
import com.stg.service.dto.external.responseFlexible.AdditionalAssuredOutput;
import com.stg.service3rd.mbal.dto.resp.FlexibleCheckTSAResp;
import com.stg.service3rd.toolcrm.dto.resp.quote.QuotationAssuredDto;
import com.stg.utils.DateUtil;
import com.stg.utils.FlexibleCommon;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlexibleCrmUtil {
    public static final int QUOTATION_DATE_EXPIRED_DAYS = 30;
    public static final int QUOTATION_DATE_MUST_CHECK_TSA_HOURS = 24;

    /***/
    public static boolean isMatchedCustomer(Customer mbCustomer, QuotationAssuredDto customer) {
        String bodStr = DateUtil.localDateTimeToString(DateUtil.DATE_YYYY_MM_DD, mbCustomer.getBirthday());
        return (
                //mbCustomer.getFullName() != null && mbCustomer.getFullName().equalsIgnoreCase(customer.getFullName())) &&
                Objects.equals(bodStr, customer.getDob().toString()) &&
                Objects.equals(mbCustomer.getPhone(), customer.getPhoneNumber()) &&
                Objects.equals(mbCustomer.getGender(), customer.getGender().toString())
                //Objects.equals(mbCustomer.getIdentification(), customer.getIdentificationId())
                //&& Objects.equals(mbCustomer.getIdCardType(), convertIdentificationTypeMbalToMiniApp(customer.getIdentificationType())
        );
    }

    /***/
    public static boolean isMatchedIdentify(FlexibleCommon.CommonInfo customerInfo, FlexibleCommon.CommonInfo assured) {
        if (customerInfo == null || assured == null) return false;
        return Objects.equals(customerInfo.getIdentificationNumber(), assured.getIdentificationNumber()) &&
                Objects.equals(customerInfo.getIdentificationType(), assured.getIdentificationType());
    }

    public static boolean isMatchedIdentify(FlexibleCommon.PolicyHolderAndLifeAssured assured, FlexibleCheckTSAResp.Assured assuredTsa) {
        if (assured == null || assuredTsa == null) return false;
        return Objects.equals(assured.getIdentificationNumber(), assuredTsa.getIdentificationNumber()) &&
                Objects.equals(assured.getIdentificationType(), assuredTsa.getIdentificationType());
    }

    public static boolean isMatchedIdentify(AdditionalAssuredOutput additionAssured, FlexibleCheckTSAResp.Assured assuredTsa) {
        if (additionAssured == null || assuredTsa == null) return false;
        return Objects.equals(additionAssured.getAssured().getIdentificationNumber(), assuredTsa.getIdentificationNumber()) &&
                Objects.equals(additionAssured.getAssured().getIdentificationType(), assuredTsa.getIdentificationType());
    }


    /***/
    public static boolean hasMustCheckTSA(LocalDateTime quotationDate) {
        if (quotationDate == null) return true;
        return LocalDateTime.now().isAfter(quotationDate.plusHours(QUOTATION_DATE_MUST_CHECK_TSA_HOURS));
    }

    /***/
    public static boolean hasExpiredQuotationData(LocalDateTime quotationDate) {
        if (quotationDate == null) return true;
        return LocalDateTime.now().isAfter(quotationDate.plusDays(QUOTATION_DATE_EXPIRED_DAYS));
    }
}
