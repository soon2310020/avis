package vn.com.twendie.avis.api.mapping;

import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.payload.CreateOrUpdateBlankDiaryPayload;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;

@Component
public class CreateOrUpdateJDDPayloadMappingProperty extends PropertyMap<CreateOrUpdateBlankDiaryPayload, JourneyDiaryDaily> {

    @Override
    protected void configure() {
        skip(destination.getJourneyDiaryDailyCostTypes());
    }
}
