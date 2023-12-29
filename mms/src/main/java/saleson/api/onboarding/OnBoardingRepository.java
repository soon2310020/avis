package saleson.api.onboarding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.OnBoardingFeature;
import saleson.model.OnBoarding;
import saleson.model.User;

import java.util.List;
import java.util.Optional;

public interface OnBoardingRepository extends JpaRepository<OnBoarding, Long>, QuerydslPredicateExecutor<OnBoarding>, OnBoardingRepositoryCustom {
    Optional<List<OnBoarding>> findByUser(User user);
    List<OnBoarding> findByFeature(OnBoardingFeature feature);
    Optional<OnBoarding> findByUserAndFeature(User user, OnBoardingFeature feature);
}
