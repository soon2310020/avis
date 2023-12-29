package com.stg.service.impl;

import com.google.gson.Gson;
import com.stg.entity.AutoDebitPayment;
import com.stg.entity.InsurancePayment;
import com.stg.entity.customer.Customer;
import com.stg.errors.AutoDebitException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.AutoDebitPaymentRepository;
import com.stg.repository.InsurancePaymentRepository;
import com.stg.service.AutoDebitPaymentService;
import com.stg.service.dto.baas.InsurancePaymentRetryReq;
import com.stg.service.dto.external.request.MbCallBackTransactionReqDto;
import com.stg.service.dto.insurance.RegisterAutoDebitStatusDto;
import com.stg.service.lock.PaymentLockService;
import com.stg.service3rd.baas.dto.code.ErrorCodeRegisterAutoDebit;
import com.stg.service3rd.baas.dto.req.RegisterAutoDebitReq;
import com.stg.service3rd.baas.dto.resp.RegisterAutoDebitResp;
import com.stg.service3rd.baas.thuho.CollectBulk3rdService;
import com.stg.service3rd.common.dto.enumcode.IEnumCode;
import com.stg.service3rd.common.dto.error.ErrorDesc;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AutoDebitPaymentServiceImpl implements AutoDebitPaymentService {
    private final AutoDebitPaymentRepository autoDebitPaymentRepository;
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final CollectBulk3rdService collectBulk3rdService;
    private final PaymentLockService paymentLockService;
    private final Gson gson;

    private static final String SERVICE_CODE = "THU_HO";
    private static final String CUS_TYPE = "CA_NHAN";

    /**
     * NOTE: mbCallBackData is null <=> retry-call
     */
    @Override
    @Async
    public RegisterAutoDebitStatusDto createAutoDebitNonOTP(Customer customer,
                                                            InsurancePayment insurancePayment,
                                                            @Nullable MbCallBackTransactionReqDto mbCallBackData) {
        // 1.check customer
        if (customer == null) {
            throw new AutoDebitException("Không tìm thấy thông tin khách hàng");
        }

        String mbTransactionId = insurancePayment.getTransactionId();
        return paymentLockService.lockRegisterAutoDebit(mbTransactionId, () -> {
            log.info("[REGISTER-AUTO-DEBIT]--Starting... mbTransactionId={}", mbTransactionId);
            boolean isFirstTime = mbCallBackData != null; // NOTE: isRetry = !isFirstTime <=> mbCallBackData == null
            RegisterAutoDebitStatusDto response = new RegisterAutoDebitStatusDto();

            // 2.get autoDebitEntity
            AutoDebitPayment autoDebitEntity;
            if (isFirstTime) {
                autoDebitEntity = autoDebitPaymentRepository.findBySourceNumberAndSourceType(mbCallBackData.getMbFundingAccount(), mbCallBackData.getFundingSource());
                if (autoDebitEntity == null) {
                    autoDebitEntity = new AutoDebitPayment(); /*TẠO MỚI*/

                    autoDebitEntity.setCustomerId(customer.getId());
                    autoDebitEntity.setSourceType(mbCallBackData.getFundingSource());
                    autoDebitEntity.setSourceNumber(mbCallBackData.getMbFundingAccount()); // converted! => không cần convert nữa
                    autoDebitEntity.setSourceName(customer.getFullNameT24());
                }
            } else {
                autoDebitEntity = autoDebitPaymentRepository.findByInsurancePaymentId(insurancePayment.getId());
                if (autoDebitEntity == null) {
                    log.warn("[REGISTER-AUTO-DEBIT]--Something wrong! mbTransactionId={}", mbTransactionId);
                    return response.setSuccess(false).setMessage("Chưa đủ điều kiện đăng ký thanh toán tự động");
                }
                autoDebitEntity.setSourceName(customer.getFullNameT24()); // todo... remove sau khi retry manual
            }

            /*
             * - Từ bước này sẽ luôn save update autoDebitEntity
             * - Và isFirstTime == true thì luôn cần: Gán AutoDebit vào InsurancePayment
             * */
            try {
                // 3.check-spam!
                if (autoDebitEntity.isRegistered() || ErrorCodeRegisterAutoDebit.OK.equalCode(autoDebitEntity.getStatusCode())) {
                    return response.setSuccess(false).setMessage("Đã đăng ký, vui lòng không spam!");
                }

                if (ErrorCodeRegisterAutoDebit.SOURCE_NUMBER_STILL_ACTIVE.equalCode(autoDebitEntity.getStatusCode())) {
                    return response.setSuccess(false).setMessage("Tài khoản đã được đăng ký trên hệ thống khác!");
                }

                // 4.callApi: register auto-debit
                RegisterAutoDebitReq registerReq = new RegisterAutoDebitReq();
                registerReq.setServiceCode(SERVICE_CODE);
                registerReq.setCusType(CUS_TYPE);
                registerReq.setNationalId(customer.getIdentification());
                registerReq.setSourceType(autoDebitEntity.getSourceType());
                registerReq.setSourceNumber(autoDebitEntity.getSourceNumber());
                registerReq.setSourceName(autoDebitEntity.getSourceName());
                registerReq.setPhoneNumber(customer.getPhone());
                registerReq.setEmail(customer.getEmail());

                RegisterAutoDebitResp registerResp = collectBulk3rdService.registerAutoDebitNonOTP(registerReq);
                autoDebitEntity.setStatusCode(registerResp.getErrorCode());

                IEnumCode enumMessage = ErrorCodeRegisterAutoDebit.findByCode(registerResp.getErrorCode(), registerResp.getErrorMessage());
                if (ErrorCodeRegisterAutoDebit.OK.equals(enumMessage)) {
                    if (registerResp.getData() != null) {
                        autoDebitEntity.setRegistered(true);
                        autoDebitEntity.setSourceId(registerResp.getData().getSourceId());
                    } else {
                        log.error("[REGISTER-AUTO-DEBIT]--Wrong response! mbTransactionId={}", mbTransactionId);
                    }
                }
                autoDebitEntity.setStatusMessage(enumMessage.getMessageVn());

                log.info("[REGISTER-AUTO-DEBIT]-- mbTransactionId={}, registerResp={}", mbTransactionId, gson.toJson(registerResp));
                return response.setSuccess(true).setMessage(enumMessage.getMessageVn());
            } catch (Exception ex) {
                log.error("[REGISTER-AUTO-DEBIT]--Có lỗi xảy ra khi register auto-debit, mbTransactionId={}, detail={}", mbTransactionId, ex.getMessage());

                IErrorObject errorObject = ApiUtil.parseErrorMessage(ex, ErrorDesc.class);
                IEnumCode enumMessage = ErrorCodeRegisterAutoDebit.findByCode(errorObject.getErrorCode(), errorObject.getErrorMessage());

                autoDebitEntity.setStatusCode(errorObject.getErrorCode());
                autoDebitEntity.setStatusMessage(enumMessage.getMessageVn());
                return response.setSuccess(false).setMessage(enumMessage.getMessageVn());
            } finally {
                autoDebitEntity.setVersion(autoDebitEntity.getVersion() + 1); // next version
                autoDebitEntity = autoDebitPaymentRepository.save(autoDebitEntity); /*createOrUpdate*/

                if (isFirstTime) {
                    insurancePayment.setAutoDebitPayment(autoDebitEntity);
                    insurancePaymentRepository.save(insurancePayment); /*Gán AutoDebit vào InsurancePayment*/
                }
                log.info("[REGISTER-AUTO-DEBIT]--End... mbTransactionId={}, success={}, autoDebitEntity={}", mbTransactionId, response.isSuccess(), gson.toJson(autoDebitEntity));
            }
        });
    }


    @Override
    public void retryRegisterAutoDebitManual(InsurancePaymentRetryReq reqDto) {
        String mbTransactionId = reqDto.getMbTransactionId();
        InsurancePayment insurancePayment = insurancePaymentRepository.findByTransactionId(mbTransactionId);
        if (!insurancePayment.isAutoPay()) {
            String errorMsg = "Không phải giao dịch thanh toán tự động. Không được phép đăng ký";
            log.error("[REGISTER-AUTO-DEBIT]--{} {}", errorMsg, mbTransactionId);
            throw new AutoDebitException(errorMsg, new ErrorDto(HttpStatus.BAD_REQUEST, errorMsg));
        }

        RegisterAutoDebitStatusDto registerStatus = this.createAutoDebitNonOTP(insurancePayment.getCustomer(), insurancePayment, null);
        if (!registerStatus.isSuccess()) {
            throw new AutoDebitException(registerStatus.getMessage(), new ErrorDto(HttpStatus.BAD_REQUEST, registerStatus.getMessage()));
        }
    }


    /***/
    /*void convertAndSetSourceNumber(@NonNull AutoDebitPayment autoDebitPayment, @Nullable MbCallBackTransactionReqDto mbCallBackData) throws Exception {
        boolean isFirstTime = mbCallBackData != null; // isRetry = !isFirstTime
        boolean isMustConvert = isFirstTime ? CARD.getLabel().equals(mbCallBackData.getFundingSource()) : !StringUtils.hasText(autoDebitPayment.getSourceNumber());

        // 1. set data lần đầu tiên (isFirstTime)
        if (isFirstTime) {
            if (isMustConvert) {
                autoDebitPayment.setCardNumber(mbCallBackData.getMbFundingAccount());
            } else {
                autoDebitPayment.setSourceNumber(mbCallBackData.getMbFundingAccount());
                return;
            }
        }

        // 2. convert card-number
        if (isMustConvert) {
            CardNumbToCardIDReq convertCardReq = new CardNumbToCardIDReq();
            convertCardReq.setRequestID(UUID.randomUUID().toString());
            convertCardReq.setCardNumber(autoDebitPayment.getCardNumber());
            CardNumbToCardIDResp convertCardResp = mbCardApi3rdService.convertCardNumbToCardID(convertCardReq);

            if (ErrorCodeConvertCard.SUCCESS.getCode().equals(convertCardResp.getErrorInfo().getCode())) {
                autoDebitPayment.setSourceNumber(convertCardResp.getCardID());
            } else {
                throw new BaasApi3rdException("Có lỗi xảy ra khi convert số thẻ sang ID thẻ", new ErrorDto(HttpStatus.BAD_REQUEST, convertCardResp.getErrorInfo().getMessage()));
            }
        }
    }*/
}
