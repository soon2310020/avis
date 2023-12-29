package saleson.api.onboarding;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.TranUtils;

import saleson.api.user.UserRepository;
import saleson.common.enumeration.OnBoardingFeature;
import saleson.common.util.SecurityUtils;
import saleson.model.OnBoarding;
import saleson.model.QOnBoarding;
import saleson.model.User;

@Service
@Transactional
public class OnBoardingService {
	@Autowired
	private OnBoardingRepository onBoardingRepository;
	@Autowired
	private UserRepository userRepository;

	public OnBoarding get(OnBoardingFeature feature) {
		if (feature == null) {
			return null;
		}

		User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
		OnBoarding onBoarding = get(user, feature);
		return onBoarding;
	}

	public OnBoarding read(OnBoarding onBoarding) {
		User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
		OnBoarding data = onBoarding.getId() == null ? //
				get(user, onBoarding.getFeature()) //
				: onBoardingRepository.getOne(onBoarding.getId());
		if (data.isSeen()) {
			return data;
		}
		data.setSeen(true);
		onBoardingRepository.save(data);
		return data;
	}

	private OnBoarding get(User user, OnBoardingFeature feature) {
		OnBoarding onBoarding = onBoardingRepository.findByUserAndFeature(user, feature).orElse(null);
		if (onBoarding == null) {
			onBoarding = new OnBoarding();
			onBoarding.setUser(user);
			onBoarding.setUserId(user.getId());
			onBoarding.setFeature(feature);
			onBoarding.setSeen(false);
			onBoardingRepository.save(onBoarding);
		}
		return onBoarding;
	}

	public void unreadAll(OnBoardingFeature feature) {
		if (feature == null) {
			return;
		}

		QOnBoarding table = QOnBoarding.onBoarding;
		DataUtils.runBatch(//
				OnBoardingRepository.class, //
				table.feature.eq(feature).and(table.seen.isNull().or(table.seen.isTrue())), //
				Sort.by("id"), 100, false, //
				onBoarding -> TranUtils.doNewTran(() -> {
					onBoarding.setSeen(false);
					onBoardingRepository.save(onBoarding);
				}));
	}

}
