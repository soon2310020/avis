package com.stg.service.impl.customer;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.InsuranceContract;
import com.stg.entity.customer.Customer;
import com.stg.entity.user.User;
import com.stg.errors.ApplicationException;
import com.stg.errors.CustomerNotFoundException;
import com.stg.errors.UserHasNoPermissionException;
import com.stg.errors.UserNotFoundException;
import com.stg.repository.CustomerRepository;
import com.stg.repository.InsuranceContractRepository;
import com.stg.repository.MbEmployeeRepository;
import com.stg.repository.UserRepository;
import com.stg.service.CustomerService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.customer.*;
import com.stg.service.dto.insurance.ContractIllustrationNumberVo;
import com.stg.service.impl.visitor.customer.ToDtoCustomerVisitor;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.stg.utils.CommonMessageError.MSG37;
import static com.stg.utils.NlpUtil.isMatchFullNameT24;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CustomerImpl implements CustomerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerImpl.class);
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final InsuranceContractRepository insuranceContractRepository;
    private final MbEmployeeRepository mbEmployeeRepository;

    @Autowired
    private final ModelMapper mapper;

    @Override
    public PaginationResponse<CustomerDto> customerFilterList(Long user, int page, int size, String queryName, String segment) {
        isSuperAdminAndAdmin(user);
        PaginationResponse<CustomerDto> response = new PaginationResponse<>();
        List<Customer> customers = customerRepository.listCustomer(page, size, queryName.trim(), segment);
        long totalCustomer = customerRepository.totalCustomer(queryName.trim(), segment);
        response.setData(customers.stream().map(customer -> customer.accept(new ToDtoCustomerVisitor())).collect(Collectors.toList()));
        response.setTotalItem(totalCustomer);
        response.setPageSize(size);
        return response;
    }

    @Override
    public CustomerDetailDto customerDetail(Long idUser, Long idCustomer) {
        isSuperAdminAndAdmin(idUser);
        Customer customer = findCustomer(idCustomer);
        return setFieldCustomerDto(customer);
    }

    @Override
    public List<CustomerDto> customerList() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDto> customerDtos = customers.stream().map(e -> mapper.map(e, CustomerDto.class)).collect(Collectors.toList());
        return customerDtos;
    }

    private Customer findCustomer(long idCustomer) {
        return customerRepository.findById(idCustomer).orElseThrow(
                () -> new CustomerNotFoundException("Has not user with id: " + idCustomer));
    }

    private void isSuperAdminAndAdmin(Long userId) {
        User user = findApplicationUser(userId);
        if (!user.getRole().equals(User.Role.SUPER_ADMIN) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new UserHasNoPermissionException("This user has no permission.");
        }
    }

    private User findApplicationUser(long id) {
        return userRepository.findUserById(id).orElseThrow(
                () -> new UserNotFoundException("There is no user with id=" + id));
    }

    private CustomerDetailDto setFieldCustomerDto(Customer customer) {
        CustomerDetailDto customerDetailDto = new CustomerDetailDto();
        customerDetailDto.setId(customer.getId());
        customerDetailDto.setMbId(customer.getMbId());
        customerDetailDto.setFullName(customer.getFullName());
        customerDetailDto.setFullNameT24(customer.getFullNameT24());
        customerDetailDto.setGender(customer.getGender());
        customerDetailDto.setPhone(customer.getPhone());
        customerDetailDto.setIdentification(customer.getIdentification());
        customerDetailDto.setEmail(customer.getEmail());
        customerDetailDto.setBirthday(DateUtil.localDateTimeToString(DateUtil.DATE_DMY, customer.getBirthday()));
        customerDetailDto.setNationality(customer.getNationality());
        customerDetailDto.setJob(customer.getJob());

        customerDetailDto.setAddress(customer.fullAddress());

        customerDetailDto.setIdCardType(customer.getIdCardType());
        customerDetailDto.setIdentificationDate(customer.getIdentificationDate() != null ? DateUtil.localDateTimeToString(DateUtil.DATE_YYYY_MM_DD, customer.getIdentificationDate()) : "");
        customerDetailDto.setIdIssuedPlace(customer.getIdIssuedPlace());

        customerDetailDto.setManagingUnit(customer.getManagingUnit());

        List<InsuranceContract> insuranceContracts = customer.getInsuranceContracts();
        List<Long> contractIds = insuranceContracts.stream().map(InsuranceContract::getId).collect(Collectors.toList());
        List<ContractIllustrationNumberVo> contractIllustrationNumberVos = insuranceContractRepository.listContractIllustrationNumber(contractIds);
        Map<Long, ContractIllustrationNumberVo> mapContractIdIllustrationNumber = contractIllustrationNumberVos.stream().collect(Collectors.toMap(ContractIllustrationNumberVo::getContractId, Function.identity()));

        List<ContractDTO> insuranceContractDtos = new ArrayList<>();
        insuranceContracts.forEach(contract -> {
            ContractDTO contractDto = new ContractDTO(contract);
            ContractIllustrationNumberVo contractIllustrationNumberVo = mapContractIdIllustrationNumber.get(contract.getId());
            if (contractIllustrationNumberVo != null) {
                contractDto.setIllustrationNumber(contractIllustrationNumberVo.getIllustrationNumber());
            }
            insuranceContractDtos.add(contractDto);
        });
        customerDetailDto.setInsuranceContracts(insuranceContractDtos);
        return customerDetailDto;
    }

    @Override
    public void exportListCustomers(Long user, String queryName, String type, HttpServletResponse response, String segment) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportListCustomers with user=" + user + ", queryName=" + queryName + ", type=" + type);
        isSuperAdminAndAdmin(user);

        List<Customer> customers = customerRepository.listAllPageCustomers(queryName, segment);
        List<CustomerDto> customerDtos = customers.stream().map(customer -> customer.accept(new ToDtoCustomerVisitor())).collect(Collectors.toList());

        List<CustomerListExportDTO> csvDTOList = mapListCustomerToCSV(customerDtos);

        if (ExportType.CSV.name().equalsIgnoreCase(type)) {
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-khach-hang-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, CustomerListExportDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-khach-hang-excel-", response);
            List<String> headers = Arrays.asList("MB_ID", "Tên KH sửa", "Tên KH trên T24", "Thời gian cập nhật", "Email", "Ngày sinh", "Phân khúc", "Nguồn truy vấn");
            Field[] fields = CustomerListExportDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_customer.xlsx", response);
        }
    }

    @Override
    public void exportCustomerDetail(Long idUser, Long idCustomer, String type, HttpServletResponse response) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportDetailCustomers with idUser=" + idUser + ", idCustomer=" + idCustomer);

        CustomerDetailDto customerDetailDto = customerDetail(idUser, idCustomer);

        List<CustomerDetailExportDTO> detailExportDTOS = mapCustomerDetailDtoToCSV(customerDetailDto);

        if (ExportType.CSV.name().equalsIgnoreCase(type)) {
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Chi-tiet-khach-hang-csv-", response);
            OpenCsvUtil.write(response.getWriter(), detailExportDTOS, CustomerDetailExportDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Chi-tiet-khach-hang-excel-", response);
            Field[] fields = CustomerDetailExportDTO.class.getDeclaredFields();
            List<String> headers = Arrays.asList("MB ID", "Họ và tên", "Ngày sinh", "Giới tính", "Quốc tịch", "Số điện thoại",
                    "Nghề nghiệp", "Loại giấy tờ tùy thân", "Số giấy tờ tùy thân", "Email", "Địa chỉ", "Tên gói bảo hiểm", "Số BMH", "Số GCNBH", "Số HSYCBH",
                    "Thời hạn", "Giá trị hợp đồng", "Đơn vị phát hành", "Ngày phát hành GCNBH", "Ngày khởi tạo HSYCBH");
            ExcelUtils.exportExcel(detailExportDTOS, fields, headers, 2, "templates/customer_detail.xlsx", response);
        }
    }

    public List<CustomerListExportDTO> mapListCustomerToCSV(List<CustomerDto> customerDtos) {
        List<CustomerListExportDTO> csvDTOList = new ArrayList<>();
        for (CustomerDto customerDto : customerDtos) {
            CustomerListExportDTO customerListExportDTO = new CustomerListExportDTO();
            customerListExportDTO.mbId = customerDto.getMbId();
            customerListExportDTO.fullName = customerDto.getFullName();
            customerListExportDTO.fullNameT24 = customerDto.getFullNameT24();
            customerListExportDTO.lastUpdated = customerDto.getLastUpdated();
            customerListExportDTO.email = customerDto.getEmail();
            customerListExportDTO.birthDay = customerDto.getBirthday();
            customerListExportDTO.segment = customerDto.getSegment();
            customerListExportDTO.source = customerDto.getSource();

            csvDTOList.add(customerListExportDTO);
        }
        return csvDTOList;
    }

    public List<CustomerDetailExportDTO> mapCustomerDetailDtoToCSV(CustomerDetailDto customerDetailDto) {
        List<CustomerDetailExportDTO> csvDTOList = new ArrayList<>();

        if (customerDetailDto.getInsuranceContracts().isEmpty()) {
            CustomerDetailExportDTO detailExportDTO = new CustomerDetailExportDTO();
            buildExportCustomerDetailDto(customerDetailDto, detailExportDTO);
            csvDTOList.add(detailExportDTO);
            return csvDTOList;
        }

        for (ContractDTO contractDTO : customerDetailDto.getInsuranceContracts()) {
            CustomerDetailExportDTO detailExportDTO = new CustomerDetailExportDTO();
            buildExportCustomerDetailDto(customerDetailDto, detailExportDTO);
            detailExportDTO.nameInsurance = contractDTO.getNameInsurance();
            detailExportDTO.illustrationNumber = contractDTO.getIllustrationNumber();
            detailExportDTO.micContractNum = contractDTO.getMicContractNum();
            detailExportDTO.mbalAppNo = contractDTO.getMbalAppNo();
            detailExportDTO.mbalFeePaymentTime = contractDTO.getMbalFeePaymentTime();
            detailExportDTO.contractValue = contractDTO.getContractValue();
            detailExportDTO.issuer = contractDTO.getIssuer();
            detailExportDTO.releaseDateMic = contractDTO.getReleaseDate();
            detailExportDTO.releaseDateMbal = contractDTO.getReleaseDate();

            csvDTOList.add(detailExportDTO);
        }
        return csvDTOList;
    }

    private static void buildExportCustomerDetailDto(CustomerDetailDto customerDetailDto, CustomerDetailExportDTO detailExportDTO) {
        detailExportDTO.mbId = customerDetailDto.getMbId();
        detailExportDTO.fullName = customerDetailDto.getFullName();
        detailExportDTO.birthDay = customerDetailDto.getBirthday();
        detailExportDTO.gender = customerDetailDto.getGender();
        detailExportDTO.nationality = customerDetailDto.getNationality();
        detailExportDTO.phone = customerDetailDto.getPhone();
        detailExportDTO.job = customerDetailDto.getJob();
        detailExportDTO.idCardType = customerDetailDto.getIdCardType();
        detailExportDTO.identification = customerDetailDto.getIdentification();
        detailExportDTO.email = customerDetailDto.getEmail();
        detailExportDTO.address = customerDetailDto.getAddress();
    }

    @Override
    public CustomerDto customerDetail(String mbId) {
        Customer customer = customerRepository.findByMbId(mbId);
        if (customer == null) {
            throw new CustomerNotFoundException("Không tồn tại Khách hàng có ID = " + mbId);
        }
        return mapper.map(customer, CustomerDto.class);
    }

    @Override
    public CustomerDto updateCustomer(String mbId, CustomerDto customerDto) {

        Customer customer = customerRepository.findByMbId(mbId);
        if (customer == null) {
            throw new CustomerNotFoundException("Không tồn tại Khách hàng có ID = " + mbId);
        }
        if (customerDto.getFullName() != null) {
            if (!isMatchFullNameT24(customer.getFullNameT24(), customerDto.getFullName())) {
                throw new ApplicationException(MSG37);
            }
            customer.setFullName(customerDto.getFullName());
        }

        customer.setBirthday(customerDto.getBirthday());
        customer.setGender(customerDto.getGender());
        customer.setJob(customerDto.getJob());
        customer.setIdCardType(customerDto.getIdCardType());
        customer.setNationality(customerDto.getNationality());
        customer.setEmail(customerDto.getEmail());

        customer.setIdentificationDate(customerDto.getIdentificationDate());
        customer.setIdIssuedPlace(customerDto.getIdIssuedPlace());
        customerRepository.save(customer);
        return mapper.map(customer, CustomerDto.class);
    }
}
