package vn.com.twendie.avis.mobile.api.adapter;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiaryCostType;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryStationFeeDTO;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryTotalInfo;
import vn.com.twendie.avis.mobile.api.model.response.TotalJourneyDiaryCost;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JourneyDiaryTotalInfoAdapter implements Function<JourneyDiary, JourneyDiaryTotalInfo> {

    @Override
    public JourneyDiaryTotalInfo apply(JourneyDiary journeyDiary) {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        ListUtils listUtils= new ListUtils(modelMapper);

        List<TotalJourneyDiaryCost> totalJourneyDiaryCosts = journeyDiary.getJourneyDiaryCostTypes().stream()
                .filter(journeyDiaryCostType -> !journeyDiaryCostType.isDeleted())
                .collect(Collectors.groupingBy(JourneyDiaryCostType::getCostType))
                .entrySet()
                .stream()
                .map(entry -> TotalJourneyDiaryCost.builder()
                        .costType(new CostTypeDTOAdapter().apply(entry.getKey()))
                        .totalValue(entry.getValue().stream()
                                .map(journeyDiaryCostType -> journeyDiaryCostType.getValue().intValueExact())
                                .reduce(0, Integer::sum))
                        .build())
                .collect(Collectors.toList());

        List<JourneyDiaryStationFeeDTO> journeyDiaryStationFeeDTOs = listUtils.mapAll(
                journeyDiary.getJourneyDiaryStationFees(), JourneyDiaryStationFeeDTO.class);

        return JourneyDiaryTotalInfo.builder()
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
                .timeEnd(journeyDiary.getTimeEnd())
                .addressEnd(journeyDiary.getAddressEnd())
                .kmOdoEnd(journeyDiary.getKmOdoEnd())
                .kmDriverEnd(journeyDiary.getKmDriverEnd())
                .imageOdoLinkEnd(journeyDiary.getImageOdoLinkEnd())
                .step(journeyDiary.getStep())
                .totalJourneyDiaryCosts(totalJourneyDiaryCosts)
                .journeyDiaryStationFees(journeyDiaryStationFeeDTOs)
                .build();

    }

}
