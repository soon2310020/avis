package com.stg.service.lock.impl;

import com.stg.service.lock.PaymentLockService;
import com.stg.service.lock.base.ExecHandler;
import com.stg.service.lock.base.RedisLock;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.stg.utils.Constants.COLON;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLockServiceImpl implements PaymentLockService {
    private final RedisLock redisLock;

    /**
     * EXPIRED_TIME
     */
    private static final long MB_START_TRANS_PX = 5 * 1000; // 5s
    private static final long MB_TRANS_CALLBACK_PX = 3 * 60 * 1000; // 3m
    private static final long PAY_ON_BEHALF_PX = 3 * 60 * 1000; // 3m
    private static final long REGISTER_INSTALLMENT_PX = 3 * 60 * 1000; // 3m
    private static final long REGISTER_AUTO_DEBIT_PX = 3 * 60 * 1000; // 3m
    private static final long GET_FEE_INSTALLMENT_PX = 2 * 1000; // 2s

    /**
     * [MbStartTrans:locked]
     * lock 5s
     */
    @Override
    public void doLockMbStartTransOfCombo(String processId) {
        redisLock.lock(LOCK_KEY.MB_START_TRANS_COMB + processId, MB_START_TRANS_PX);
    }
    @Override
    public void doLockMbStartTransOfFlex(Integer processId) {
        redisLock.lock(LOCK_KEY.MB_START_TRANS_FLEX + processId, MB_START_TRANS_PX);
    }


    /**
     * [MbCallbackTrans]
     */
    @Override
    public void lockMbCallbackTrans(String paymentTransId, Runnable runnable) {
        redisLock.using(LOCK_KEY.MB_CALLBACK_TRANS + paymentTransId, MB_TRANS_CALLBACK_PX, runnable);
    }


    /**
     * [PayOnBehalf]
     */
    @Override
    public <T> T lockPayOnBehalf(String paymentTransId, ExecHandler<T> handler) {
        return redisLock.using(LOCK_KEY.PAY_ON_BEHALF + paymentTransId, PAY_ON_BEHALF_PX, handler);
    }

    @Override
    public void lockPayOnBehalf(String paymentTransId, Runnable runnable) {
        redisLock.using(LOCK_KEY.PAY_ON_BEHALF + paymentTransId, PAY_ON_BEHALF_PX, runnable);
    }


    /**
     * [RegisterInstallment]
     */
    @Override
    public <T> T lockRegisterInstallment(String paymentTransId, ExecHandler<T> handler) {
        return redisLock.using(LOCK_KEY.REGISTER_INSTALLMENT + paymentTransId, REGISTER_INSTALLMENT_PX, handler);
    }

    @Override
    public void lockRegisterInstallment(String paymentTransId, Runnable runnable) {
        redisLock.using(LOCK_KEY.REGISTER_INSTALLMENT + paymentTransId, REGISTER_INSTALLMENT_PX, runnable);
    }

    /**
     * [RegisterAutoDebit]
     */
    @Override
    public <T> T lockRegisterAutoDebit(String paymentTransId, ExecHandler<T> handler) {
        return redisLock.using(LOCK_KEY.REGISTER_AUTO_DEBIT + paymentTransId, REGISTER_AUTO_DEBIT_PX, handler);
    }

    @Override
    public <T> T lockGetInstallmentFee(String mbId, String processId, ExecHandler<T> handler) {
        return redisLock.using(LOCK_KEY.GET_INSTALLMENT_FEE + mbId + COLON + processId, GET_FEE_INSTALLMENT_PX, handler);
    }


    /**
     * KEYS
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class LOCK_KEY {
        public static final String MB_START_TRANS_COMB = "MB_START_TRANS_COMB:";
        public static final String MB_START_TRANS_FLEX = "MB_START_TRANS_FLEX:";
        public static final String MB_CALLBACK_TRANS = "MB_CALLBACK_TRANS:";
        public static final String PAY_ON_BEHALF = "PAY_ON_BEHALF:";
        public static final String REGISTER_INSTALLMENT = "REGISTER_INSTALLMENT:";
        public static final String REGISTER_AUTO_DEBIT = "REGISTER_AUTO_DEBIT:";
        public static final String GET_INSTALLMENT_FEE = "GET_INSTALLMENT_FEE:";
    }

}
