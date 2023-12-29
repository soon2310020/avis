package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.mobile.api.model.payload.ListJourneyDiaryByContractIdPayload;

public interface ContractJourneyDiaryService {
    GeneralPageResponse getListJourneyDiaryByContractId(ListJourneyDiaryByContractIdPayload listJourneyDiaryByContractIdPayload, int page);
}
