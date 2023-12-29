package vn.com.twendie.avis.mobile.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.mobile.api.constant.MobileConstant;
import vn.com.twendie.avis.mobile.api.service.ContractService;
import vn.com.twendie.avis.security.core.annotation.CurrentUser;
import vn.com.twendie.avis.security.core.model.UserPrincipal;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping("")
    public ApiResponse<?> getContracts(
            @RequestParam(required = false,defaultValue = MobileConstant.DEFAULT_STARTER_PAGE) int page,
            @RequestParam(name = "filter_by_date", required = false) Long timestamp,
            @RequestParam(name = "filter_by_status", required = false) String status,
            @CurrentUser UserDetails userDetails) throws Exception {
        User user = ((UserPrincipal) userDetails).getUser();
        return ApiResponse.success(contractService.findAllContractByDriverId(user.getId(), page,
                timestamp, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getContractById(
            @PathVariable("id") Long contractId,
            @CurrentUser UserDetails userDetails
    ) throws Exception {
        User user = ((UserPrincipal) userDetails).getUser();
        if (contractId == null) {
            throw new BadRequestException("contract_id must not be null!!!")
                    .code(HttpStatus.BAD_REQUEST.value());
        }
        return ApiResponse.success(contractService.getDetailByContractIdAndDriverID(user.getId(),
                contractId));
    }
}
