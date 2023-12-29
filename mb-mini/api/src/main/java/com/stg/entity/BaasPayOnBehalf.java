package com.stg.entity;

import com.stg.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "baas_pay_on_behalf")
public class BaasPayOnBehalf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_payment_id")
    private InsurancePayment insurancePayment;
    @Column(name = "client_message_id")
    private String clientMessageId;
    @Column(name = "baas_transaction_id")
    private String baasTransactionId;
    @Column(name = "type")
    private String type;
    @Column(name = "amount")
    private Long amount;
    @Column(name = "error_code")
    private String errorCode;
    @Column(name = "tran_status")
    private String tranStatus;

    // GCNBH MIC
    @Column(name = "mic_contract_num")
    private String micContractNum;

    @Column(name = "ft_number")
    private String ftNumber;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    @Column(name = "detail_error_message")
    private String detailErrorMessage;
    @Column(name = "mb_transaction_id")
    private String mbTransactionId;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private Constants.Status status;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "mbal_hook_status")
    private Boolean mbalHookStatus;

    @PrePersist
    public void prePersist() {
        if (paymentTime == null) {
            paymentTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (mbalHookStatus == null) {
            mbalHookStatus = false;
        }
        if (status == null) {
            status = Constants.Status.FAIL;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
