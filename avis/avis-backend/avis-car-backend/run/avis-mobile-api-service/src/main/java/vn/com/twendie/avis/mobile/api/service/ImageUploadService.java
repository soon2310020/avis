package vn.com.twendie.avis.mobile.api.service;

public interface ImageUploadService {

    void uploadImageStartTrip(Long journeyDiaryId);

    void uploadImageCustomerGetIn(Long journeyDiaryId);

    void uploadImageCustomerGetOut(Long journeyDiaryId);

    void uploadImageFinishTrip(Long journeyDiaryId);

    void uploadImageJourneyDiaryCost(Long journeyDiaryCostId);

}
