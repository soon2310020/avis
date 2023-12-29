package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.Vehicle;

import java.util.List;

public interface JourneyDiaryService {

    JourneyDiary save(JourneyDiary journeyDiary);

    List<JourneyDiary> saveAll(Iterable<JourneyDiary> journeyDiaries);

    boolean checkVehicleCurrentInJourneyDiary(Vehicle vehicle);

    void verifyVehicleNotInJourneyDiary(Vehicle vehicle);

    boolean checkContractHavingJourney(Long contractId);

    JourneyDiary findById(Long id);

}
