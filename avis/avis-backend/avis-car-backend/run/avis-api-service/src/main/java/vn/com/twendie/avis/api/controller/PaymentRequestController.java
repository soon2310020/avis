package vn.com.twendie.avis.api.controller;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.adapter.PRWithoutDriverDiaryAdapter;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.model.payload.ExportCustomerPaymentRequestPayload;
import vn.com.twendie.avis.api.model.projection.PRLogContractCostProjection;
import vn.com.twendie.avis.api.model.response.JourneyDiaryDailyDTO;
import vn.com.twendie.avis.api.model.response.PRWithoutDriverDiary;
import vn.com.twendie.avis.api.model.response.PRWithoutDriverResponseWrapper;
import vn.com.twendie.avis.api.model.response.PaymentRequestItemDTO;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.enumtype.ContractTypeEnum;
import vn.com.twendie.avis.data.enumtype.MemberCustomerRoleEnum;
import vn.com.twendie.avis.data.model.*;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.annotation.RequirePermission;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.data.enumtype.ContractVATEnum.VAT_BEFORE_TOLL_FEE;
import static vn.com.twendie.avis.data.enumtype.PaymentRequestEnum.*;

@RestController
@RequestMapping("/payment_requests")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;
    private final ContractService contractService;
    private final JourneyDiaryDailyService journeyDiaryDailyService;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final CustomerPaymentRequestService customerPaymentRequestService;
    private final CustomerJourneyDiaryDailyService customerJourneyDiaryDailyService;
    private final PaymentRequestItemService paymentRequestItemService;
    private final LogContractNormListService logContractNormListService;
    private final ContractChangeHistoryService contractChangeHistoryService;

    private final ExportController exportController;

    private final DateUtils dateUtils;
    private final ListUtils listUtils;

    public PaymentRequestController(PaymentRequestService paymentRequestService,
                                    ContractService contractService,
                                    JourneyDiaryDailyService journeyDiaryDailyService,
                                    VehicleService vehicleService,
                                    UserService userService,
                                    CustomerPaymentRequestService customerPaymentRequestService,
                                    CustomerJourneyDiaryDailyService customerJourneyDiaryDailyService,
                                    PaymentRequestItemService paymentRequestItemService,
                                    LogContractNormListService logContractNormListService,
                                    ContractChangeHistoryService contractChangeHistoryService,
                                    @Lazy ExportController exportController,
                                    DateUtils dateUtils,
                                    ListUtils listUtils) {
        this.paymentRequestService = paymentRequestService;
        this.contractService = contractService;
        this.journeyDiaryDailyService = journeyDiaryDailyService;
        this.vehicleService = vehicleService;
        this.userService = userService;
        this.customerPaymentRequestService = customerPaymentRequestService;
        this.customerJourneyDiaryDailyService = customerJourneyDiaryDailyService;
        this.paymentRequestItemService = paymentRequestItemService;
        this.logContractNormListService = logContractNormListService;
        this.contractChangeHistoryService = contractChangeHistoryService;
        this.exportController = exportController;
        this.dateUtils = dateUtils;
        this.listUtils = listUtils;
    }

    @GetMapping("/")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<List<PaymentRequestItemDTO>> getPaymentRequestWithDriver(
            @RequestParam long from,
            @RequestParam long to,
            @RequestParam("contract_id") Long contractId,
            @RequestParam(value = "member_customer_ids", required = false) String memberCustomerIds
    ) throws Exception{

        List<Long> memberCustomerIdList = null;
        if(StringUtils.isNotEmpty(memberCustomerIds)){
            try {
                memberCustomerIdList = Arrays.stream(memberCustomerIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
            }catch (NumberFormatException e){
                throw new BadRequestException("member customer id incorrect").code(HttpStatus.BAD_REQUEST.value());
            }
        }

        Contract contract = contractService.findById(contractId);
        contractService.validContractType(ContractTypeEnum.WITH_DRIVER, contract.getContractType());

        Timestamp fromDate = journeyDiaryDailyService.getStartDate(contract, new Timestamp(from));
        Timestamp toDate = journeyDiaryDailyService.getEndDate(contract, new Timestamp(to));

        if (fromDate.after(toDate)) {
            throw new BadRequestException("Filter with time range got wrong!!!")
                    .displayMessage(Translator.toLocale("pr.error.time_filter_is_invalid"));
        }

        List<JourneyDiaryDaily> dailyJourneyDiaries = journeyDiaryDailyService.findAndName("", contract.getId(), fromDate, toDate, memberCustomerIdList);
        List<PaymentRequestItemDTO> list = getListPaymentRequestItem(fromDate, toDate, contract, dailyJourneyDiaries);

        return ApiResponse.success(list);
    }

    @GetMapping("/with_out_driver")
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<PRWithoutDriverResponseWrapper> getPaymentRequestWithoutDriver(
            @RequestParam long from,
            @RequestParam long to,
            @RequestParam("contract_id") Long contractId
    ) throws Exception {
        Contract contract = contractService.findById(contractId);
        contractService.validContractType(ContractTypeEnum.WITHOUT_DRIVER, contract.getContractType());

        Timestamp fromDate = journeyDiaryDailyService.getStartDate(contract, new Timestamp(from));
        Timestamp toDate = journeyDiaryDailyService.getEndDate(contract, new Timestamp(to));

        if (fromDate.after(toDate)) {
            throw new BadRequestException("Filter with time range got wrong!!!")
                    .displayMessage(Translator.toLocale("pr.error.time_filter_is_invalid"));
        }

        List<JourneyDiaryDaily> dailyJourneyDiaries = journeyDiaryDailyService.findInMonth(contract.getId(), fromDate);
        List<PaymentRequestItemDTO> list = getListPaymentRequestItem(fromDate, toDate, contract, dailyJourneyDiaries);

        List<PRWithoutDriverDiary> diaries;
        if (CollectionUtils.isNotEmpty(dailyJourneyDiaries)) {
            diaries = new PRWithoutDriverDiaryAdapter(dateUtils,
                    exportController.calculateTotalRowJDDWithoutDriver(fromDate, toDate, contract, dailyJourneyDiaries))
                    .apply(dailyJourneyDiaries);
        } else {
            diaries = Collections.emptyList();
        }

        return ApiResponse.success(PRWithoutDriverResponseWrapper.builder()
                .diaries(diaries)
                .paymentRequestItemDTOS(list)
                .build());
    }

    public List<PaymentRequestItemDTO> getListPaymentRequestItem(Timestamp fromDate, Timestamp toDate,
                                                                 Contract contract, List<JourneyDiaryDaily> dailyJourneyDiaries) throws ExecutionException, InterruptedException {
        List<PaymentRequestItemDTO> list = new ArrayList<>();
        double diff = paymentRequestService.calculateDaysDiff(fromDate, toDate);
        list.add(paymentRequestService.getRentalPrice(toDate, contract, diff));

        if (ContractTypeEnum.WITHOUT_DRIVER.value().equals(contract.getContractType().getId())) {
            list.add(paymentRequestService.getOverKMContractWithOutDriver(fromDate, toDate, contract, dailyJourneyDiaries));
            PaymentRequestItemDTO totalBeforeVAT = calculateTotalPriceBeforeVAT(list);
            PaymentRequestItemDTO VATFee = calculateVATFee(totalBeforeVAT);
            list.add(totalBeforeVAT);
            list.add(VATFee);
            list.add(calculateTotalFee(totalBeforeVAT, VATFee));
        } else {
            Map<String, List<PRLogContractCostProjection>> map = paymentRequestService.costTypeMap(contract.getId(), fromDate, toDate);

            Triplet<List<JourneyDiaryDaily>, Timestamp, Double> triplet = paymentRequestService.checkAndConvertDiariesIfOverDay(contract, toDate,
                    clone(dailyJourneyDiaries), diff);

            CompletableFuture<List<PaymentRequestItemDTO>> driverFees = CompletableFuture.supplyAsync(
                    () -> paymentRequestService.getDriverFee(contract, fromDate, toDate, triplet.getValue0(),
                            map, triplet.getValue1(), triplet.getValue2())
            );

            CompletableFuture<List<PaymentRequestItemDTO>> overnightFee = CompletableFuture.supplyAsync(
                    () -> paymentRequestService.getOvernightFee(triplet.getValue0(), fromDate, toDate, contract, map)
            );

            CompletableFuture<PaymentRequestItemDTO> getTollFee = CompletableFuture.supplyAsync(
                    () -> paymentRequestService.getTollFee(triplet.getValue0())
            );

            list.addAll(driverFees.get());
            list.addAll(overnightFee.get());

            PaymentRequestItemDTO tollFee = getTollFee.get();
            PaymentRequestItemDTO otherFee = paymentRequestService.getOtherFee();
            list.sort(Comparator.comparing(PaymentRequestItemDTO::getId)
                    .thenComparing(PaymentRequestItemDTO::getFromDate));
            PaymentRequestItemDTO totalBeforeVAT;
            PaymentRequestItemDTO VATFee;
            boolean isVATBeforeTollFee = VAT_BEFORE_TOLL_FEE.getValue().equals(contract.getVatTollFee());
            if (isVATBeforeTollFee) {
                totalBeforeVAT = calculateTotalPriceBeforeVAT(list);
                VATFee = calculateVATFee(totalBeforeVAT);
                list.add(totalBeforeVAT);
                list.add(VATFee);
                list.add(tollFee);
                list.add(otherFee);
            } else {
                list.add(tollFee);
                list.add(otherFee);
                totalBeforeVAT = calculateTotalPriceBeforeVAT(list);
                VATFee = calculateVATFee(totalBeforeVAT);
                list.add(totalBeforeVAT);
                list.add(VATFee);
            }

            list.add(calculateTotalFee(totalBeforeVAT, VATFee, tollFee, otherFee, isVATBeforeTollFee));
        }
        return list;
    }

    private List<JourneyDiaryDaily> clone(List<JourneyDiaryDaily> dailyJourneyDiaries) {
        List<JourneyDiaryDaily> clone = new ArrayList<>(dailyJourneyDiaries.size());
        for (JourneyDiaryDaily j : dailyJourneyDiaries) clone.add(j.toBuilder().build());
        return clone;
    }

    private PaymentRequestItemDTO calculateTotalFee(PaymentRequestItemDTO totalBeforeVAT,
                                                    PaymentRequestItemDTO vatFee) {
        return PaymentRequestItemDTO.builder()
                .id(TOTAL_FEE.getId())
                .name(TOTAL_FEE.getName())
                .unit(TOTAL_FEE.getUnit())
                .totalPrice(totalBeforeVAT.getTotalPrice() + vatFee.getTotalPrice())
                .build();
    }

    private PaymentRequestItemDTO calculateTotalFee(PaymentRequestItemDTO totalBeforeVAT,
                                                    PaymentRequestItemDTO vatFee,
                                                    PaymentRequestItemDTO tollFee,
                                                    PaymentRequestItemDTO otherFee,
                                                    boolean isVATBeforeTollFee) {
        long totalVAT = totalBeforeVAT.getTotalPrice() + vatFee.getTotalPrice();
        return PaymentRequestItemDTO.builder()
                .id(TOTAL_FEE.getId())
                .name(TOTAL_FEE.getName())
                .unit(TOTAL_FEE.getUnit())
                .totalPrice(isVATBeforeTollFee ? totalVAT + tollFee.getTotalPrice() + otherFee.getTotalPrice()
                        : totalVAT)
                .build();
    }

    private PaymentRequestItemDTO calculateVATFee(PaymentRequestItemDTO totalBeforeVAT) {
        return PaymentRequestItemDTO.builder()
                .id(VAT_FEE.getId())
                .name(VAT_FEE.getName())
                .unit(VAT_FEE.getUnit())
                .totalPrice(Math.round(totalBeforeVAT.getTotalPrice() * 0.1))
                .build();
    }

    private PaymentRequestItemDTO calculateTotalPriceBeforeVAT(List<PaymentRequestItemDTO> list) {
        long totalPrice = list.stream().mapToLong(PaymentRequestItemDTO::getTotalPrice).sum();
        return PaymentRequestItemDTO.builder()
                .id(TOTAL_FEE_BEFORE_VAT.getId())
                .name(TOTAL_FEE_BEFORE_VAT.getName())
                .unit(TOTAL_FEE_BEFORE_VAT.getUnit())
                .totalPrice(totalPrice)
                .build();
    }

    @PostMapping("/export_to_customer")
    @Transactional
    @RequirePermission(acceptedRoles = {"Superuser", "Admin"})
    public ApiResponse<?> exportToCustomer(
            @Valid @RequestBody ExportCustomerPaymentRequestPayload payload,
            @CurrentUser UserDetails userDetails
    ) throws Exception{
        User user = ((UserPrincipal) userDetails).getUser();
        Contract contract = contractService.findById(payload.getContractId());
        Timestamp fromDate = journeyDiaryDailyService.getStartDate(contract, payload.getFrom());
        Timestamp toDate = journeyDiaryDailyService.getEndDate(contract, payload.getTo());

        if (fromDate.after(toDate)) {
            throw new BadRequestException("Filter with time range got wrong!!!")
                    .displayMessage(Translator.toLocale("pr.error.time_filter_is_invalid"));
        }

        CustomerPaymentRequest customerPaymentRequest = createCustomerPaymentRequest(fromDate, toDate, contract, user, payload.getMemberCustomerIds());

        CustomerPaymentRequest savedCustomerPaymentRequest = customerPaymentRequestService.save(customerPaymentRequest);

        List<Long> memberCustomerIds = null;
        try{
            if(payload.getMemberCustomerIds() != null){
                memberCustomerIds = Arrays.stream(payload.getMemberCustomerIds().split(","))
                        .map(Long::valueOf)
                        .collect(Collectors.toList());;
            }
        }catch (Exception ignored){
        }

        List<JourneyDiaryDaily> dailyJourneyDiaries;
        JourneyDiaryDailyDTO totalRow;
        if (ContractTypeEnum.WITH_DRIVER.value().equals(contract.getContractType().getId())) {
//            dailyJourneyDiaries = journeyDiaryDailyService.find(contract.getId(),
//                    fromDate, toDate);
            dailyJourneyDiaries = journeyDiaryDailyService.findAndName("", contract.getId(), fromDate, toDate, memberCustomerIds);
            totalRow = exportController.calculateTotalRow(fromDate, toDate, contract, dailyJourneyDiaries);
        } else {
//            dailyJourneyDiaries = journeyDiaryDailyService.findInMonth(contract.getId(), fromDate);
            dailyJourneyDiaries = journeyDiaryDailyService.findNameInMonth("", contract.getId(), fromDate, memberCustomerIds);
            totalRow = exportController.calculateTotalRowJDDWithoutDriver(fromDate, toDate, contract, dailyJourneyDiaries);
        }

        List<PaymentRequestItemDTO> paymentRequestItemDTOS = getListPaymentRequestItem(fromDate, toDate,
                contract, dailyJourneyDiaries);

        if (CollectionUtils.isNotEmpty(dailyJourneyDiaries)) {
            List<JourneyDiaryDailyDTO> diaryDailyDTOS = listUtils.mapAll(dailyJourneyDiaries, JourneyDiaryDailyDTO.class);
            diaryDailyDTOS.add(totalRow);

            List<CustomerJourneyDiaryDaily> customerJourneyDiaryDailies = listUtils.mapAll(diaryDailyDTOS,
                    CustomerJourneyDiaryDaily.class);

            for(int i = 0; i < customerJourneyDiaryDailies.size(); i++){
                customerJourneyDiaryDailies.get(i).setJourneyDiaryId(diaryDailyDTOS.get(i).getJourneyDiaryId());
            }

            customerJourneyDiaryDailies.forEach(j -> j.setCustomerPaymentRequest(savedCustomerPaymentRequest));
            customerJourneyDiaryDailyService.saveAll(customerJourneyDiaryDailies);
        }

        List<PaymentRequestItem> paymentRequestItems = listUtils.mapAll(paymentRequestItemDTOS, PaymentRequestItem.class);

        paymentRequestItems.forEach(p -> {
            p.setId(null);
            p.setCustomerPaymentRequest(customerPaymentRequest);
        });
        paymentRequestItemService.saveAll(paymentRequestItems);

        return ApiResponse.success(true);
    }

    private CustomerPaymentRequest createCustomerPaymentRequest(Timestamp fromDate, Timestamp toDate,
                                                                Contract contract, User user, String memberCustomerIds) {
        User driver = userService.getContractDriverAtTime(contract, toDate);
        Vehicle vehicle = vehicleService.getContractVehicleAtTime(contract, toDate);

        return CustomerPaymentRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .memberCustomerIds(memberCustomerIds)
                .adminName(Objects.isNull(contract.getMemberCustomer()) ? "" :
                        MemberCustomerRoleEnum.ADMIN.getCode().equals(contract.getMemberCustomer().getRole()) ?
                                contract.getMemberCustomer().getName() : "")
                .contract(contract)
                .contractCode(contract.getCode())
                .contractTypeId(contract.getContractType().getId())
                .createdBy(user)
                .customerAddress(contract.getCustomer().getAddress())
                .customerName(contract.getCustomer().getName())
                .driverName(Objects.nonNull(driver) ? driver.getName() : null)
                .vehicleNumberPlate(Objects.nonNull(vehicle) ? vehicle.getNumberPlate() : null)
                .build();
    }
}
