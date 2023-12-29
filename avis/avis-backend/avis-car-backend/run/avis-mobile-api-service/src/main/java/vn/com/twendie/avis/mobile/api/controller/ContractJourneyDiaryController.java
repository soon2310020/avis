package vn.com.twendie.avis.mobile.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.mobile.api.constant.MobileConstant;
import vn.com.twendie.avis.mobile.api.model.payload.ListJourneyDiaryByContractIdPayload;
import vn.com.twendie.avis.mobile.api.service.ContractJourneyDiaryService;

@RestController
@RequestMapping(value = "/contract-journey-diary")
public class ContractJourneyDiaryController {

    private final ContractJourneyDiaryService contractJourneyDiaryService;

    public ContractJourneyDiaryController(ContractJourneyDiaryService contractJourneyDiaryService) {
        this.contractJourneyDiaryService = contractJourneyDiaryService;
    }

    @RequestMapping(value = "/list-journey-diary/{contract_id}", method = RequestMethod.GET)
    public ResponseEntity<?> getJourneyDiaryByContractId(
            @PathVariable("contract_id") Long id,
            @RequestParam(required = false, defaultValue = MobileConstant.DEFAULT_STARTER_PAGE) int page,
            @RequestParam(name = "from_time", required = false) Long fromTime,
            @RequestParam(name = "to_time", required = false) Long toTime
    ) {
        if (toTime != null && fromTime != null && toTime < fromTime) {
            throw new BadRequestException("toTime is less than fromTime")
                    .code(400)
                    .displayMessage(Translator.toLocale("error.request.time_range"));
        }

        ListJourneyDiaryByContractIdPayload listJourneyDiaryByContractIdPayload
                = ListJourneyDiaryByContractIdPayload
                .builder()
                .contractId(id)
                .fromTime(fromTime)
                .toTime(toTime)
                .build();

        GeneralPageResponse journeyDiaries = contractJourneyDiaryService.getListJourneyDiaryByContractId(listJourneyDiaryByContractIdPayload, page);

        return new ResponseEntity<>(ApiResponse.success(journeyDiaries), HttpStatus.OK);
    }
}
