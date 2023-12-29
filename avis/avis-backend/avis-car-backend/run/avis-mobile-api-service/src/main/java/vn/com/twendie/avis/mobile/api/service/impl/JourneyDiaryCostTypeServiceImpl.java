package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.data.model.JourneyDiaryCostType;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiaryCostTypeRepo;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryCostTypeService;

import java.math.BigDecimal;

@Service
public class JourneyDiaryCostTypeServiceImpl implements JourneyDiaryCostTypeService {

    private final JourneyDiaryCostTypeRepo journeyDiaryCostTypeRepo;

    public JourneyDiaryCostTypeServiceImpl(JourneyDiaryCostTypeRepo journeyDiaryCostTypeRepo) {
        this.journeyDiaryCostTypeRepo = journeyDiaryCostTypeRepo;
    }

    @Override
    public JourneyDiaryCostType findById(long id) {
        return journeyDiaryCostTypeRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found journey diary cost type with id: " + id));
    }

    @Override
    public JourneyDiaryCostType save(JourneyDiaryCostType journeyDiaryCostType) {
        return journeyDiaryCostTypeRepo.save(journeyDiaryCostType);
    }

    @Override
    public BigDecimal getTotalCostByJourneyDiaryId(Long journeyDiaryId) {
        BigDecimal totalCost = journeyDiaryCostTypeRepo.getTotalCostByJourneyDiaryId(journeyDiaryId);
        return totalCost == null ? BigDecimal.ZERO : totalCost;
    }
}
