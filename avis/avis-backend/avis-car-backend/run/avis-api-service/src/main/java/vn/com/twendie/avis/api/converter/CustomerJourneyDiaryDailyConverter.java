package vn.com.twendie.avis.api.converter;

import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import vn.com.twendie.avis.api.model.response.JourneyDiaryDailyDTO;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum;
import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.CustomerJourneyDiaryDaily;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
public class CustomerJourneyDiaryDailyConverter extends AbstractConverter<JourneyDiaryDailyDTO, CustomerJourneyDiaryDaily> {

    @Override
    protected CustomerJourneyDiaryDaily convert(JourneyDiaryDailyDTO source) {
        List<CodeValueModel> costs = CollectionUtils.isEmpty(source.getJourneyDiaryDailyCostTypes()) ?
                Collections.emptyList() : source.getJourneyDiaryDailyCostTypes();

        return CustomerJourneyDiaryDaily.builder()
                .id(null)
                .customerDepartment(String.join(" \\n", source.getCustomerDepartment()))
                .customerNameUsed(String.join(" \\n", source.getCustomerNameUsed()))
                .tripItinerary(String.join(" \\n", source.getTripItinerary()))
                .driverName(String.join(" \\n", source.getDriverName()))
                .vehicleNumberPlate(String.join(" \\n", source.getVehicleNumberPlate()))
                .tollsFee(costs.stream()
                        .filter(c ->
                                JourneyDiaryCostTypeEnum.TOLLS_FEE.code().equals(c.getCode()))
                        .map(CodeValueModel::getValue)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(BigDecimal.ZERO))
                .parkingFee(costs.stream()
                        .filter(c ->
                                JourneyDiaryCostTypeEnum.PARKING_FEE.code().equals(c.getCode()))
                        .map(CodeValueModel::getValue)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(BigDecimal.ZERO))
                .date(source.getDate())
                .isHoliday(source.getIsHoliday())
                .isSelfDrive(source.getIsSelfDrive())
                .isWeekend(source.getIsWeekend())
                .note(String.join(" \\n", source.getNote()))
                .kmStart(source.getKmStart())
                .kmEnd(source.getKmEnd())
                .overKm(source.getOverKm())
                .overKmSelfDrive(source.getOverKmSelfDrive())
                .overnight(source.getOvernight())
                .overTime(source.getOverTime())
                .usedKm(source.getUsedKm())
                .usedKmSelfDrive(source.getUsedKmSelfDrive())
                .workingTimeGpsFrom(source.getWorkingTimeGpsFrom())
                .workingTimeGpsTo(source.getWorkingTimeGpsTo())
                .build();
    }
}
