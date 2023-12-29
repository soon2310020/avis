//package com.stg.controller.card;
//
//import com.stg.service.ExternalAPIService;
//import com.stg.service.card.CardInstallmentService;
//import com.stg.service.dto.card.GetInstInfoRequest;
//import com.stg.service.dto.card.GetInstallElementResultRequest;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.v3.oas.annotations.Parameter;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.validation.constraints.Size;
//
//@Slf4j
//@Validated
//@RestController
//@RequiredArgsConstructor
//@Tag(name = "API Installment")
//@RequestMapping("/external")
//public class CardInstallmentController {
//    private final CardInstallmentService cardInstallmentService;
//    private final ExternalAPIService externalAPIService;
//
//    @GetMapping("/result")
//    public ResponseEntity<?> getInstallElementResult(@Parameter(description = "RequestID yêu cầu ĐKTG") @RequestParam String requestId,
//                                                     @Parameter(description = "Partner code") @RequestParam String sourceAppId) {
//        return new ResponseEntity<>(cardInstallmentService.getInstallElementResult(new GetInstallElementResultRequest(requestId, sourceAppId)), HttpStatus.OK);
//    }
//
//    @GetMapping("/get-installment-info")
//    public ResponseEntity<?> getInsFee(@Parameter(description = "ID thẻ") @Size(max = 20) @RequestParam String cardId,
//                                       @Parameter(description = "Partner code") @RequestParam String sourceAppId,
//                                       @Parameter(description = "Tên khách hàng") @Size(max = 35) @RequestParam String cusName,
//                                       @Parameter(description = "Merchant giao dịch") @Size(max = 15) @RequestParam String merchant,
//                                       @Parameter(description = "Truy vấn từ ngày (dd/MM/yyyy)") @Size(max = 10) @RequestParam String fromDate,
//                                       @Parameter(description = "Truy vấn đến ngày (dd/MM/yyyy)") @Size(max = 10) @RequestParam String toDate
//    ) {
//        return new ResponseEntity<>(cardInstallmentService.getInstInfo(new GetInstInfoRequest(cardId, sourceAppId, cusName, merchant, fromDate, toDate)), HttpStatus.OK);
//    }
//
////    @PostMapping("/fee-check")
////    public ResponseEntity<?> getInsFee(@AuthenticationPrincipal CustomerIdentifier identifier,
////                                       @Valid @RequestBody GetInstFeeRequest infoReqDto) {
////        return new ResponseEntity<>(externalAPIService.getInsFee(identifier.getMbId(), infoReqDto), HttpStatus.OK);
////    }
//}
