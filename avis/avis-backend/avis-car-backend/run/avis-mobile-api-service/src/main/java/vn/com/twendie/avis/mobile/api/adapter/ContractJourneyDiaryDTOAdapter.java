package vn.com.twendie.avis.mobile.api.adapter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import vn.com.twendie.avis.api.core.ApplicationContextProvider;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.mobile.api.model.response.ListJourneyDiaryByContactIdDTO;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryCostTypeService;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;

@Slf4j
public class ContractJourneyDiaryDTOAdapter implements Function<JourneyDiary, ListJourneyDiaryByContactIdDTO> {

    @Override
    public ListJourneyDiaryByContactIdDTO apply(JourneyDiary journeyDiary) {
        JourneyDiaryCostTypeService journeyDiaryCostTypeService = ApplicationContextProvider.getApplicationContext().getBean(JourneyDiaryCostTypeService.class);

        BigDecimal kmStart = journeyDiary.getKmOdoStart() == null ?
                journeyDiary.getKmDriverStart() == null ? ZERO
                        : journeyDiary.getKmDriverStart()
                : journeyDiary.getKmOdoStart();

        BigDecimal kmEnd = journeyDiary.getKmOdoEnd() == null ?
                journeyDiary.getKmDriverEnd() == null ? ZERO
                        : journeyDiary.getKmDriverEnd()
                : journeyDiary.getKmOdoEnd();

        BigDecimal kmCustomerGetOut = journeyDiary.getKmOdoCustomerGetOut() == null ?
                journeyDiary.getKmDriverCustomerGetOut()
                : journeyDiary.getKmOdoCustomerGetOut();

        BigDecimal kmCustomerGetIn = journeyDiary.getKmOdoCustomerGetIn() == null ?
                journeyDiary.getKmDriverCustomerGetIn()
                : journeyDiary.getKmOdoCustomerGetIn();

        BigDecimal kmTotal = kmEnd.subtract(kmStart).max(ZERO);

        BigDecimal totalCost = journeyDiaryCostTypeService.getTotalCostByJourneyDiaryId(journeyDiary.getId());

        BigDecimal kmEmpty = Objects.isNull(kmCustomerGetIn) ? ZERO
                : Objects.isNull(kmCustomerGetOut) ? kmCustomerGetIn.subtract(kmStart)
                : kmEnd.subtract(kmCustomerGetOut).add(kmCustomerGetIn.subtract(kmStart));

        BigDecimal kmGps = ObjectUtils.defaultIfNull(journeyDiary.getTotalKmGps(), ZERO);

        return ListJourneyDiaryByContactIdDTO.builder()
                .timeStart(journeyDiary.getTimeStart())
                .timeEnd(journeyDiary.getTimeEnd())
                .kmStart(kmStart)
                .kmEnd(kmEnd)
                .kmTotal(kmTotal)
                .kmEmptyTotal(kmEmpty)
                .kmGps(kmGps)
                .totalCost(totalCost)
                .build();
    }
}
