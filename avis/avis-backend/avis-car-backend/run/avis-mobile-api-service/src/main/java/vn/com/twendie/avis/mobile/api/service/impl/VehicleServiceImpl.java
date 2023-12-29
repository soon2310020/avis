package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.Vehicle;
import vn.com.twendie.avis.mobile.api.service.VehicleService;

import java.util.Objects;

import static vn.com.twendie.avis.data.enumtype.VehicleStatusEnum.*;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Override
    public void assignToJourneyDiary(Vehicle vehicle, JourneyDiary journeyDiary) {
        if (Objects.nonNull(vehicle)) {
            vehicle.setCurrentJourneyDiaryId(journeyDiary.getId());
            vehicle.setStatus(UNAVAILABLE.getValue());
        }
    }

    @Override
    public void unAssignFromJourneyDiary(Vehicle vehicle) {
        if (Objects.nonNull(vehicle)) {
            vehicle.setCurrentJourneyDiaryId(null);
            if (Objects.isNull(vehicle.getCurrentContractId()) && Objects.isNull(vehicle.getLendingContractId())) {
                vehicle.setStatus(WAITING.getValue());
            } else {
                vehicle.setStatus(APPOINTED.getValue());
            }
        }
    }
}
