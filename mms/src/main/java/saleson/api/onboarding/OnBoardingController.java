package saleson.api.onboarding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.OnBoardingFeature;
import saleson.common.payload.ApiResponse;
import saleson.model.OnBoarding;

@RestController
@RequestMapping("/api/on-boarding")
public class OnBoardingController {
	@Autowired
	private OnBoardingService service;

	@GetMapping("/get")
	public ApiResponse get(@RequestParam("feature") String feature) {
		try {
			OnBoardingFeature onBoardingFeature = OnBoardingFeature.valueOf(feature);
			OnBoarding onBoarding = service.get(onBoardingFeature);
			return ApiResponse.success(CommonMessage.OK, onBoarding);
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	@PostMapping("/update")
	public ApiResponse update(@RequestBody OnBoarding onBoarding) {
		try {
			onBoarding = service.read(onBoarding);
			return ApiResponse.success(CommonMessage.OK, onBoarding);
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	@GetMapping("/set-all-version-false")
	public ApiResponse setAllVersionFalse(@RequestParam("feature") String feature) {
		try {
			OnBoardingFeature onBoardingFeature = OnBoardingFeature.valueOf(feature);
			service.unreadAll(onBoardingFeature);
			return ApiResponse.success("Success");
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

}
