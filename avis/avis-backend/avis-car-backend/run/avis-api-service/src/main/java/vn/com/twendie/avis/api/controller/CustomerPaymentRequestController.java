package vn.com.twendie.avis.api.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.constant.AvisApiConstant;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.core.util.PageUtils;
import vn.com.twendie.avis.api.model.response.CustomerPRWithoutDriverWrapperDTO;
import vn.com.twendie.avis.api.model.response.CustomerPaymentRequestDTO;
import vn.com.twendie.avis.api.model.response.CustomerPaymentRequestWrapperDTO;
import vn.com.twendie.avis.api.model.response.PRWithoutDriverDiary;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.api.service.CustomerPaymentRequestService;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.model.CustomerJourneyDiaryDaily;
import vn.com.twendie.avis.data.model.CustomerPaymentRequest;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import java.util.*;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.api.core.util.DateUtils.SHORT_PATTERN;

@RestController
@RequestMapping("/customer_payment_request")
public class CustomerPaymentRequestController {

    private final CustomerPaymentRequestService customerPaymentRequestService;
    private final ListUtils listUtils;
    private final DateUtils dateUtils;
    private final ModelMapper modelMapper;

    public CustomerPaymentRequestController(CustomerPaymentRequestService customerPaymentRequestService,
                                            ListUtils listUtils,
                                            DateUtils dateUtils,
                                            ModelMapper modelMapper) {
        this.customerPaymentRequestService = customerPaymentRequestService;
        this.listUtils = listUtils;
        this.dateUtils = dateUtils;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    @RequirePermission(acceptedRoles = "Customer")
    public ApiResponse<?> getAllCustomerPaymentRequests(
            @CurrentUser UserDetails userDetails,
            @RequestParam(name = "page", defaultValue = AvisApiConstant.DEFAULT_STARTER_PAGE_STRING) int page,
            @RequestParam(name = "page_size", defaultValue = AvisApiConstant.DEFAULT_PAGE_SIZE_STRING) int size
    ) {
        User user = ((UserPrincipal) userDetails).getUser();
        List<CustomerPaymentRequest> customerPaymentRequests = customerPaymentRequestService.findAllByCustomerUser(user);
        List<CustomerPaymentRequestDTO> dtos = listUtils.mapAll(customerPaymentRequests, CustomerPaymentRequestDTO.class);
        return ApiResponse.success(GeneralPageResponse.toResponse(PageUtils.toPage(page, size, dtos)));
    }

    @GetMapping("/{id}")
    @RequirePermission(acceptedRoles = "Customer")
    public ApiResponse<?> getById(
            @PathVariable("id") Long id
    ) {
        CustomerPaymentRequest customerPaymentRequest = customerPaymentRequestService.findById(id);
        validContractType(ContractTypeEnum.WITH_DRIVER, customerPaymentRequest.getContractTypeId());
        return ApiResponse.success(customerPaymentRequestService.convertToDTO(customerPaymentRequest));
    }

    private void validContractType(ContractTypeEnum contractTypeEnum, Long contractTypeId) {
        if (!contractTypeEnum.value().equals(contractTypeId)) {
            throw new BadRequestException("Wrong Contract Type!!!")
                    .displayMessage(Translator.toLocale("contract.error.contract_type_is_invalid"));
        }
    }

    @GetMapping("/without_driver/{id}")
    @RequirePermission(acceptedRoles = "Customer")
    public ApiResponse<?> getPRWithoutDriverById(
            @PathVariable("id") Long id
    ) {
        CustomerPaymentRequest customerPaymentRequest = customerPaymentRequestService.findById(id);
        validContractType(ContractTypeEnum.WITHOUT_DRIVER, customerPaymentRequest.getContractTypeId());
        CustomerPRWithoutDriverWrapperDTO dto = modelMapper.map(customerPaymentRequest,
                CustomerPRWithoutDriverWrapperDTO.class);
        if (CollectionUtils.isNotEmpty(customerPaymentRequest.getCustomerJourneyDiaryDailies())) {
            dto.setDiaries(transform(customerPaymentRequest.getCustomerJourneyDiaryDailies()));
        } else {
            dto.setDiaries(Collections.emptyList());
        }
        return ApiResponse.success(dto);
    }

    private List<PRWithoutDriverDiary> transform(List<CustomerJourneyDiaryDaily> customerJourneyDiaryDailies) {
        List<PRWithoutDriverDiary> result = new ArrayList<>();
        PRWithoutDriverDiary temp = null;
        String startDate = null;

        for (int i = customerJourneyDiaryDailies.size() - 1; i >= 0; i--) {
            if (Objects.nonNull(customerJourneyDiaryDailies.get(i).getKmStart())) {
                if (Objects.nonNull(customerJourneyDiaryDailies.get(i).getKmEnd())) {
                    String date = dateUtils.format(customerJourneyDiaryDailies.get(i).getDate(), SHORT_PATTERN);
                    result.add(PRWithoutDriverDiary.builder()
                            .kmStart(customerJourneyDiaryDailies.get(i).getKmStart())
                            .kmEnd(customerJourneyDiaryDailies.get(i).getKmEnd())
                            .usedKm(customerJourneyDiaryDailies.get(i).getUsedKm())
                            .date(String.format("%s - %s", date, date))
                            .startDate(customerJourneyDiaryDailies.get(i).getDate())
                            .driverName(getDriverName(customerJourneyDiaryDailies.get(i)))
                            .vehicleNumberPlate(getVehicleNumberPlate(customerJourneyDiaryDailies.get(i)))
                            .note(getNote(customerJourneyDiaryDailies.get(i)))
                            .build());
                } else {
                    startDate = dateUtils.format(customerJourneyDiaryDailies.get(i).getDate(), SHORT_PATTERN);
                    temp = PRWithoutDriverDiary.builder()
                            .kmStart(customerJourneyDiaryDailies.get(i).getKmStart())
                            .usedKm(customerJourneyDiaryDailies.get(i).getUsedKm())
                            .startDate(customerJourneyDiaryDailies.get(i).getDate())
                            .driverName(getDriverName(customerJourneyDiaryDailies.get(i)))
                            .vehicleNumberPlate(getVehicleNumberPlate(customerJourneyDiaryDailies.get(i)))
                            .note(getNote(customerJourneyDiaryDailies.get(i)))
                            .build();
                }
            } else if (Objects.nonNull(temp)) {

                if (Objects.nonNull(customerJourneyDiaryDailies.get(i).getNote())) {
                    temp.getNote().addAll(getNote(customerJourneyDiaryDailies.get(i)));
                }

                if (Objects.nonNull(customerJourneyDiaryDailies.get(i).getKmEnd())) {
                    temp.setKmEnd(customerJourneyDiaryDailies.get(i).getKmEnd());
                    temp.setDate(String.format("%s - %s", startDate, dateUtils.format(customerJourneyDiaryDailies.get(i).getDate(), SHORT_PATTERN)));
                    temp.setNote(temp.getNote().stream().distinct().collect(Collectors.toList()));
                    result.add(temp);
                }
            }
        }

        result.sort(Comparator.comparing(PRWithoutDriverDiary::getStartDate));
        result.add(PRWithoutDriverDiary.builder()
                .overKm(customerJourneyDiaryDailies.get(customerJourneyDiaryDailies.size() - 1).getOverKm())
                .usedKm(customerJourneyDiaryDailies.get(customerJourneyDiaryDailies.size() - 1).getUsedKm())
                .build());
        return result;
    }

    private List<String> getNote(CustomerJourneyDiaryDaily customerJourneyDiaryDaily) {
        return Objects.isNull(customerJourneyDiaryDaily.getNote()) ? Collections.emptyList()
                : new ArrayList<>(Arrays.asList(customerJourneyDiaryDaily.getNote().split(" \\\\n")));
    }

    private List<String> getVehicleNumberPlate(CustomerJourneyDiaryDaily customerJourneyDiaryDaily) {
        return Objects.isNull(customerJourneyDiaryDaily.getVehicleNumberPlate()) ? Collections.emptyList()
                : Arrays.asList(customerJourneyDiaryDaily.getVehicleNumberPlate().split(" \\\\n"));
    }

    private List<String> getDriverName(CustomerJourneyDiaryDaily customerJourneyDiaryDaily) {
        return Objects.isNull(customerJourneyDiaryDaily.getDriverName()) ? Collections.emptyList()
                : Arrays.asList(customerJourneyDiaryDaily.getDriverName().split(" \\\\n"));
    }
}
