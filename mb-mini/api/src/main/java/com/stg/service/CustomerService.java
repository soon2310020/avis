package com.stg.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.customer.CustomerDetailDto;
import com.stg.service.dto.customer.CustomerDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface CustomerService {

    PaginationResponse<CustomerDto> customerFilterList(Long user, int page, int size, String queryName, String segment);

    CustomerDetailDto customerDetail(Long idUser, Long idCustomer);

    List<CustomerDto> customerList();

    void exportListCustomers(Long idUser, String queryName, String type, HttpServletResponse response, String segment) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

    void exportCustomerDetail(Long idUser, Long idCustomer, String type, HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException;

    //Long createCustomer(CustomerDto customerDto);

    CustomerDto customerDetail(String mbId);

    CustomerDto updateCustomer(String mbId, CustomerDto customerDto);
}
