package com.stg.service;

import com.stg.service.dto.external.MbalIllustrationDownloadRespDto;
import com.stg.service.dto.external.requestV2.*;
import com.stg.service.dto.external.responseV2.*;

import java.util.List;

public interface ExternalV2ApiService {

    CreateQuoteV2RespDto mbalCreateProcess(String cif, CreateProcessV2ReqDto createQuoteReqDto) ;

    CreateQuoteV2RespDto mbalCreateQuote(String cif, CreateQuoteV2ReqDto createQuoteReqDto, Integer processId) ;

    CreateQuoteV2RespDto mbalConfirmQuotation(String cif, Integer processId);

    CreateQuoteV2RespDto mbalSubmitApplication(String cif, SubmitApplicationV2ReqDto v2ReqDto, Integer processId) ;

    List<OccupationV2RespDto> mbalListOccupation();

    List<PackageV2RespDto> mbalListPackage();

//    PackageTypeV2RespDtos getPackages(MiniPackageV2ReqDto reqDto) ;

    String mbalSendMail(String cif, String processId, SendMailV2ReqDto v2ReqDto) ;

    MbalIllustrationDownloadRespDto downloadFileBMH(String processId, String cif);

//    UploadFileRespDto uploadMultiFileQuestion(MultipartFile[] files, String processId, String cif) throws IOException, ExecutionException, InterruptedException;

    PaymentNotificationV2RespDto paymentNotifications(PaymentNotificationV2ReqDto paymentNotificationV2ReqDto) ;

    PackageTypeV2RespDtos insurancePackages(MiniPackageV2ReqDto reqDto) ;

    IcInformationResp getIcInformation(String icCode);

}
