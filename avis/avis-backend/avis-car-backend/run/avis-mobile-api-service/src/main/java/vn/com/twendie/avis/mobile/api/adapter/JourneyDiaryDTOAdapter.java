package vn.com.twendie.avis.mobile.api.adapter;

import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryDTO;

import java.util.Objects;
import java.util.function.Function;

public class JourneyDiaryDTOAdapter implements Function<JourneyDiary, JourneyDiaryDTO> {

    @Override
    public JourneyDiaryDTO apply(JourneyDiary journeyDiary) {
        if (Objects.isNull(journeyDiary)) {
            return null;
        }

        return JourneyDiaryDTO.builder()
                .id(journeyDiary.getId())
                .driverId(journeyDiary.getDriver().getId())
                .contractId(journeyDiary.getContract().getId())
                .vehicleId(journeyDiary.getVehicle().getId())
                .timeStart(journeyDiary.getTimeStart())
                .addressStart(journeyDiary.getAddressStart())
                .kmOdoStart(journeyDiary.getKmOdoStart())
                .kmDriverStart(journeyDiary.getKmDriverStart())
                .imageOdoLinkStart(journeyDiary.getImageOdoLinkStart())
                .timeCustomerGetIn(journeyDiary.getTimeCustomerGetIn())
                .addressCustomerGetIn(journeyDiary.getAddressCustomerGetIn())
                .kmOdoCustomerGetIn(journeyDiary.getKmOdoCustomerGetIn())
                .kmDriverCustomerGetIn(journeyDiary.getKmDriverCustomerGetIn())
                .customerNameUsed(journeyDiary.getCustomerNameUsed())
                .customerDepartment(journeyDiary.getCustomerDepartment())
                .imageOdoLinkCustomerGetIn(journeyDiary.getImageOdoLinkCustomerGetIn())
                .imageCustomerGetInLink(journeyDiary.getImageCustomerGetInLink())
                .timeCustomerGetOut(journeyDiary.getTimeCustomerGetOut())
                .addressCustomerGetOut(journeyDiary.getAddressCustomerGetOut())
                .kmOdoCustomerGetOut(journeyDiary.getKmOdoCustomerGetOut())
                .kmDriverCustomerGetOut(journeyDiary.getKmDriverCustomerGetOut())
                .imageOdoLinkCustomerGetOut(journeyDiary.getImageOdoLinkCustomerGetOut())
                .imageCustomerGetOutLink(journeyDiary.getImageCustomerGetOutLink())
                .timeBreakdown(journeyDiary.getTimeBreakdown())
                .kmOdoBreakdown(journeyDiary.getKmOdoBreakdown())
                .kmDriverBreakdown(journeyDiary.getKmDriverBreakdown())
                .imageBreakdownLink(journeyDiary.getImageBreakdownLink())
                .timeEnd(journeyDiary.getTimeEnd())
                .addressEnd(journeyDiary.getAddressEnd())
                .kmOdoEnd(journeyDiary.getKmOdoEnd())
                .kmDriverEnd(journeyDiary.getKmDriverEnd())
                .imageOdoLinkEnd(journeyDiary.getImageOdoLinkEnd())
                .step(journeyDiary.getStep())
                .build();
    }

}
