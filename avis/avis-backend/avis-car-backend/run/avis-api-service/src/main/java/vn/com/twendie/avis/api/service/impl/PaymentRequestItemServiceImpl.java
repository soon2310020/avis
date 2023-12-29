package vn.com.twendie.avis.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.PaymentRequestItemRepo;
import vn.com.twendie.avis.api.service.PaymentRequestItemService;
import vn.com.twendie.avis.data.model.PaymentRequestItem;

import java.util.List;

@Service
public class PaymentRequestItemServiceImpl implements PaymentRequestItemService {

    private final PaymentRequestItemRepo paymentRequestItemRepo;

    public PaymentRequestItemServiceImpl(PaymentRequestItemRepo paymentRequestItemRepo) {
        this.paymentRequestItemRepo = paymentRequestItemRepo;
    }

    @Override
    public List<PaymentRequestItem> saveAll(List<PaymentRequestItem> paymentRequestItems) {
        return paymentRequestItemRepo.saveAll(paymentRequestItems);
    }
}
