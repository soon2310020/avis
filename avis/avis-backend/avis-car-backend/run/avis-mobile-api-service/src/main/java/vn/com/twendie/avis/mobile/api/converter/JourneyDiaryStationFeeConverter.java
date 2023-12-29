package vn.com.twendie.avis.mobile.api.converter;

import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.data.model.JourneyDiaryStationFee;
import vn.com.twendie.avis.tracking.model.tracking.VehicleFeeStation;

import static vn.com.twendie.avis.api.core.util.DateUtils.*;

@Component
public class JourneyDiaryStationFeeConverter extends AbstractConverter<VehicleFeeStation, JourneyDiaryStationFee> {

    private final DateUtils dateUtils;

    public JourneyDiaryStationFeeConverter(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    @Override
    protected JourneyDiaryStationFee convert(VehicleFeeStation vehicleFeeStation) {
        return JourneyDiaryStationFee.builder()
                .vehiclePlate(vehicleFeeStation.getVehiclePlate())
                .privateCode(vehicleFeeStation.getPrivateCode())
                .vehicleTypeName(vehicleFeeStation.getVehicleTypeName())
                .botName(vehicleFeeStation.getBotName())
                .stageName(vehicleFeeStation.getStageName())
                .inName(vehicleFeeStation.getInName())
                .inTime(dateUtils.convertTimeZone(vehicleFeeStation.getInTime(), LOCAL_TIME_ZONE, UTC_TIME_ZONE))
                .outName(vehicleFeeStation.getOutName())
                .outTime(dateUtils.convertTimeZone(vehicleFeeStation.getOutTime(), LOCAL_TIME_ZONE, UTC_TIME_ZONE))
                .checkName(vehicleFeeStation.getCheckName())
                .checkTime(dateUtils.convertTimeZone(vehicleFeeStation.getCheckTime(), LOCAL_TIME_ZONE, UTC_TIME_ZONE))
                .kmOnStage(vehicleFeeStation.getKmOnStage())
                .fee(vehicleFeeStation.getFee())
                .note(vehicleFeeStation.getNote())
                .build();
    }
}
