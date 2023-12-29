package vn.com.twendie.avis.api.mapping;

import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.payload.EditAppDiaryDailyPayload;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;

@Component
public class EditJDDPayloadMappingProperty extends PropertyMap<EditAppDiaryDailyPayload, JourneyDiaryDaily> {

    @Override
    protected void configure() {
        skip(destination.getJourneyDiaryDailyCostTypes());
        skip(destination.getVehicleNumberPlate());
        skip(destination.getDriverName());
    }
}
