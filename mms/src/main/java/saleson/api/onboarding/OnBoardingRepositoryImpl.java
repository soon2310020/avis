package saleson.api.onboarding;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.model.OnBoarding;

public class OnBoardingRepositoryImpl extends QuerydslRepositorySupport implements OnBoardingRepositoryCustom {
    public OnBoardingRepositoryImpl() {
        super(OnBoarding.class);
    }
}
