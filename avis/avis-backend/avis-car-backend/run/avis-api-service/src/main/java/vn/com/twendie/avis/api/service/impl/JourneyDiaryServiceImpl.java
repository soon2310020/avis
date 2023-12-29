package vn.com.twendie.avis.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.JourneyDiaryRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.service.JourneyDiaryService;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.Vehicle;
import vn.com.twendie.avis.locale.config.Translator;

import java.util.List;
import java.util.Objects;

import static vn.com.twendie.avis.data.enumtype.JourneyDiaryStepEnum.FINISHED;

@Service
public class JourneyDiaryServiceImpl implements JourneyDiaryService {

    private final JourneyDiaryRepo journeyDiaryRepo;

    public JourneyDiaryServiceImpl(JourneyDiaryRepo journeyDiaryRepo) {
        this.journeyDiaryRepo = journeyDiaryRepo;
    }

    @Override
    public JourneyDiary save(JourneyDiary journeyDiary) {
        return journeyDiaryRepo.save(journeyDiary);
    }

    @Override
    public List<JourneyDiary> saveAll(Iterable<JourneyDiary> journeyDiaries) {
        return journeyDiaryRepo.saveAll(journeyDiaries);
    }

    @Override
    public boolean checkVehicleCurrentInJourneyDiary(Vehicle vehicle) {
        return journeyDiaryRepo.existsByVehicleIdAndStepNotAndDeletedFalse(vehicle.getId(), FINISHED.value());
    }

    @Override
    public void verifyVehicleNotInJourneyDiary(Vehicle vehicle) {
        if (Objects.nonNull(vehicle) && checkVehicleCurrentInJourneyDiary(vehicle)) {
            throw new BadRequestException("Vehicle is current in another journey diary")
                    .displayMessage(Translator.toLocale("vehicle.vehicle_is_current_in_journey_diary"));
        }
    }

    @Override
    public boolean checkContractHavingJourney(Long contractId) {
        return journeyDiaryRepo.existsByContractIdAndStepNotAndDeletedFalse(contractId, FINISHED.value());
    }

    @Override
    public JourneyDiary findById(Long id) {
        return journeyDiaryRepo.findById(id).orElse(null);
    }

}
