package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.mobile.api.adapter.ContractJourneyDiaryDTOAdapter;
import vn.com.twendie.avis.mobile.api.constant.MobileConstant;
import vn.com.twendie.avis.mobile.api.model.payload.ListJourneyDiaryByContractIdPayload;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiaryRepo;
import vn.com.twendie.avis.mobile.api.service.ContractJourneyDiaryService;

import java.sql.Timestamp;

@Service
public class ContractJourneyDiaryServiceImpl implements ContractJourneyDiaryService {

    private JourneyDiaryRepo journeyDiaryRepo;

    @Autowired
    public ContractJourneyDiaryServiceImpl(JourneyDiaryRepo journeyDiaryRepo) {
        this.journeyDiaryRepo = journeyDiaryRepo;
    }

    @Override
    public GeneralPageResponse getListJourneyDiaryByContractId(ListJourneyDiaryByContractIdPayload listJourneyDiaryByContractIdPayload, int page) {
        int pageOffSet = page > 0 ? page - 1 : 0;
        Page<JourneyDiary> journeyDiaries;

        if (listJourneyDiaryByContractIdPayload.getFromTime() == null
                && listJourneyDiaryByContractIdPayload.getToTime() == null) {
            journeyDiaries = journeyDiaryRepo.getListJourneyDiaryByContractId(listJourneyDiaryByContractIdPayload.getContractId(), PageRequest.of(pageOffSet, MobileConstant.DEFAULT_CONTRACTS_PAGE_SIZE));
        } else if (listJourneyDiaryByContractIdPayload.getFromTime() != null
                && listJourneyDiaryByContractIdPayload.getToTime() == null) {
            Timestamp fromTime = new Timestamp(listJourneyDiaryByContractIdPayload.getFromTime());
            String inputTime = DateUtils.dateWithTimeZone(fromTime, DateUtils.MEDIUM_PATTERN,
                    DateUtils.UTC_TIME_ZONE);
            journeyDiaries = journeyDiaryRepo.getListJourneyDiaryByContractIdFromTime(listJourneyDiaryByContractIdPayload.getContractId(),
                    inputTime,
                    PageRequest.of(pageOffSet, MobileConstant.DEFAULT_CONTRACTS_PAGE_SIZE));
        } else if (listJourneyDiaryByContractIdPayload.getToTime() != null
                && listJourneyDiaryByContractIdPayload.getFromTime() == null) {
            Timestamp toTime = new Timestamp(listJourneyDiaryByContractIdPayload.getToTime());
            String inputTime = DateUtils.dateWithTimeZone(toTime, DateUtils.MEDIUM_PATTERN,
                    DateUtils.UTC_TIME_ZONE);
            journeyDiaries = journeyDiaryRepo.getListJourneyDiaryByContractIdToTime(listJourneyDiaryByContractIdPayload.getContractId(),
                    inputTime,
                    PageRequest.of(pageOffSet, MobileConstant.DEFAULT_CONTRACTS_PAGE_SIZE));
        } else {
            Timestamp fromTime = new Timestamp(listJourneyDiaryByContractIdPayload.getFromTime());
            String inputFromTime = DateUtils.dateWithTimeZone(fromTime, DateUtils.MEDIUM_PATTERN,
                    DateUtils.UTC_TIME_ZONE);
            Timestamp toTime = new Timestamp(listJourneyDiaryByContractIdPayload.getToTime());
            String inputToTime = DateUtils.dateWithTimeZone(toTime, DateUtils.MEDIUM_PATTERN,
                    DateUtils.UTC_TIME_ZONE);
            journeyDiaries = journeyDiaryRepo.getListJourneyDiaryByContractIdWhichInTime(listJourneyDiaryByContractIdPayload.getContractId(),
                    inputFromTime, inputToTime,
                    PageRequest.of(pageOffSet, MobileConstant.DEFAULT_CONTRACTS_PAGE_SIZE));
        }

            return new GeneralPageResponse().toResponse(
                    journeyDiaries.map(journeyDiary
                            -> new ContractJourneyDiaryDTOAdapter().apply(journeyDiary)));
    }
}
