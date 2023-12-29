package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.PaymentRequestItem;

import java.util.List;

public interface PaymentRequestItemService {

    List<PaymentRequestItem> saveAll(List<PaymentRequestItem> paymentRequestItems);
}
