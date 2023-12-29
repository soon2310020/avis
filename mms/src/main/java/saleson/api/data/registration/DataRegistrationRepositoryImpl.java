package saleson.api.data.registration;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.DataRegistration;

public class DataRegistrationRepositoryImpl extends QuerydslRepositorySupport implements DataRegistrationRepositoryCustom {
    public DataRegistrationRepositoryImpl() {
        super(DataRegistration.class);
    }
}
