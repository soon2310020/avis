package vn.com.twendie.avis.api.mapping;

import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.payload.EditDriverPayload;
import vn.com.twendie.avis.data.model.User;

@Component
public class UpdateUserMappingProperty extends PropertyMap<EditDriverPayload, User> {

    @Override
    protected void configure() {
        skip(destination.getPassword());
    }
}
