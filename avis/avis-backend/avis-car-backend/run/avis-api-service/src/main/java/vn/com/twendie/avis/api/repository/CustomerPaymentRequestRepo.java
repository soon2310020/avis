package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.CustomerPaymentRequest;

import java.util.Collection;
import java.util.List;

public interface CustomerPaymentRequestRepo extends JpaRepository<CustomerPaymentRequest, Long> {

    List<CustomerPaymentRequest> findByContractCustomerIdAndDeletedFalseOrderByCreatedAtDesc(Long contractCustomerId);

    List<CustomerPaymentRequest> findByContractMemberCustomerIdInAndDeletedFalseOrderByCreatedAtDesc(Collection<Long> contractMemberCustomerIds);
}
