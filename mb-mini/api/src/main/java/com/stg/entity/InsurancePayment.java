package com.stg.entity;

import com.stg.entity.customer.Customer;
import com.stg.service.dto.external.ControlStateEnum;
import com.stg.utils.FundingSource;
import com.stg.utils.InstallmentPopup;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "insurance_payment")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class InsurancePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Column(name = "total_fee")
    private String totalFee;  //tổng phí bảo hiểm, format x.xxx.xxx VND
    @Column(name = "mbal_insurance_fee")
    private String mbalInsuranceFee; //format x.xxx.xxx VND
    @Column(name = "mic_insurance_fee")
    private String micInsuranceFee; //format x.xxx.xxx VND
    //thời gian đóng phí
    @Column(name = "fee_payment_time")
    private String feePaymentTime;
    //định kỳ đóng phí
    @Column(name = "periodic_fee_payment")
    private String periodicFee;
    @Column(name = "auto_pay")
    private boolean autoPay;
    @Column(name = "installment")
    private boolean installment;
    @Column(name = "rm_name")
    private String rmName;
    @Column(name = "rm_code")
    private String rmCode;
    @Column(name = "rm_email")
    private String rmEmail;
    @Column(name = "rm_phone_number")
    private String rmPhoneNumber;

    @Column(name = "presenter_phone")
    private String presenterPhone;
    @Column(name = "root_account")
    private String rootAccount;
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;
    @Column(name = "transaction_type")
    private String transactionType;
    @Column(name = "tran_status")
    private String tranStatus;
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_state")
    private ControlStateEnum controlState; //trạng thái đối soát

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_request_id")
    private InsuranceRequest insuranceRequest;

    @Column(name = "ic_name")
    private String icName;
    @Column(name = "ic_code")
    private String icCode;
    @Column(name = "support_name")
    private String supportName;
    @Column(name = "support_code")
    private String supportCode;
    @Column(name = "support_email")
    private String supportEmail;
    @Column(name = "support_phone_number")
    private String supportPhoneNumber;

    @Column(name = "branch_name")
    private String branchName;
    @Column(name = "branch_code")
    private String branchCode;
    @Column(name = "department_name")
    private String departmentName;
    @Column(name = "department_code")
    private String departmentCode;

    @Column(name = "way4_docs_id")
    private String way4DocsId;
    @Column(name = "period")
    private String period; // Kỳ hạn
    @Column(name = "periodic_conversion_fee")
    private String periodicConversionFee; // % phí trả góp trên tổng số tiền phải trả
    @Column(name = "fees_pay_able")
    private String feesPayable; // Phí trả hàng tháng: (periodicConversionFee * tổng số tiền) / period
    @Column(name = "installment_status")
    private String installmentStatus; // Trạng thái đăng ký trả góp
    @Column(name = "card_class")
    private String cardClass;

    // Thanh toán bằng tài khoản
    @Column(name = "normal")
    private boolean normal;

    @Column(name = "auto_pay_set_time")
    private LocalDateTime autoPaySetTime;

    @Column(name = "auto_pay_status")
    private Boolean autoPayStatus;

    @Column(name = "funding_source")
    @Enumerated(value = EnumType.STRING)
    private FundingSource fundingSource;

    @Column(name = "version")
    private Integer version;
    @Column(name = "installment_error_code")
    private String installmentErrorCode; // Ma loi dang ky tra gop

    // GCNBH MBAL
    @Column(name = "mbal_app_no")
    private String mbalAppNo;

    @Column(name = "installment_popup")
    @Enumerated(value = EnumType.STRING)
    private InstallmentPopup installmentPopup;

    @Column(name = "mb_callback_response")
    @Type(type = "jsonb")
    private String mbCallback;

    // GCNBH MIC COMBO
    @Column(name = "mic_contract_num")
    private String micContractNum;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_debit_payment_id")
    private AutoDebitPayment autoDebitPayment;

    @Transient
    private String managingUnit;

    @PrePersist
    public void prePersist() {
        if (paymentTime == null) {
            paymentTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }

}
