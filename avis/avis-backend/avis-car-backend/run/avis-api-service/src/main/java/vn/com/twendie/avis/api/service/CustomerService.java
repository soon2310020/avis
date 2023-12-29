package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.payload.EnterpriseCustomerPayload;
import vn.com.twendie.avis.api.model.payload.IndividualCustomerPayload;
import vn.com.twendie.avis.api.model.response.CustomerDTO;
import vn.com.twendie.avis.data.model.Customer;
import vn.com.twendie.avis.data.model.User;

import java.util.List;

public interface CustomerService {

    CustomerDTO createOrUpdateEnterpriseCustomer(EnterpriseCustomerPayload payload, User currentUser, String customerCode, List<String> userMemberCodes);

    Customer createIndividualCustomer(IndividualCustomerPayload payload, User currentUser);

    Customer findById(long id);

    Customer save(Customer customer);

    Boolean updateIndividualCustomer(IndividualCustomerPayload payload, User currentUser);

    Boolean deleteIndividualCustomer(Long id);

    void updateInContract(Customer customer);

    String suggestCustomerCode();

}
