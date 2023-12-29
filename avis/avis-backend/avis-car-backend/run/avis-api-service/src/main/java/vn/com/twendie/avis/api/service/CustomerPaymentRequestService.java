package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.response.CustomerPaymentRequestWrapperDTO;
import vn.com.twendie.avis.data.model.CustomerPaymentRequest;
import vn.com.twendie.avis.data.model.User;

import java.util.List;

public interface CustomerPaymentRequestService {

    List<CustomerPaymentRequest> findAllByCustomerUser(User user);

    CustomerPaymentRequest findById(Long id);

    CustomerPaymentRequest save(CustomerPaymentRequest customerPaymentRequest);

    CustomerPaymentRequestWrapperDTO convertToDTO(CustomerPaymentRequest customerPaymentRequest);

}
