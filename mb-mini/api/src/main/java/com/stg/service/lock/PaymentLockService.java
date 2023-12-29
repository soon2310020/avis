package com.stg.service.lock;

import com.stg.service.lock.base.ExecHandler;

public interface PaymentLockService {
    /** [MbStartTrans] */
    void doLockMbStartTransOfCombo(String processId);
    void doLockMbStartTransOfFlex(Integer processId);

    /** [MbCallbackTrans] */
    void lockMbCallbackTrans(String paymentTransId, Runnable runnable);


    /** [PayOnBehalf] */
    <T> T lockPayOnBehalf(String paymentTransId, ExecHandler<T> handler);
    void lockPayOnBehalf(String paymentTransId, Runnable runnable);


    /** [RegisterInstallment] */
    <T> T lockRegisterInstallment(String paymentTransId, ExecHandler<T> handler);
    void lockRegisterInstallment(String paymentTransId, Runnable runnable);


    /** [RegisterAutoDebit] */
    <T> T lockRegisterAutoDebit(String paymentTransId, ExecHandler<T> handler);

    /** [Get Fee Installment] */
    <T> T lockGetInstallmentFee(String mbId, String processId, ExecHandler<T> handler);
}
