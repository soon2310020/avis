package com.stg.service.dto.insurance;

import com.stg.service.dto.external.request.MbCallBackTransactionReqDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailDto {

    private InsurancePaymentDto insurancePayment;

    private MbCallBackTransactionReqDto paymentHubCallback;

}
