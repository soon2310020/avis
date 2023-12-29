package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.Vehicle;

public interface VehicleService {

    void assignToJourneyDiary(Vehicle vehicle, JourneyDiary journeyDiary);

    void unAssignFromJourneyDiary(Vehicle vehicle);

}
