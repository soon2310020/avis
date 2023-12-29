package saleson.api.user;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.ValueUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import com.emoldino.framework.util.ConfigUtils;

import saleson.api.mold.MoldService;
import saleson.api.resource.ResourceHandler;
import saleson.api.user.payload.UserInvitePayload;
import saleson.api.user.payload.UserParam;
import saleson.api.user.payload.UserPayload;
import saleson.api.versioning.service.VersioningService;
import saleson.common.config.Const;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.SpecialAlertType;
import saleson.common.notification.MailService;
import saleson.common.payload.ApiResponse;
import saleson.common.security.UserPrincipal;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.*;
import saleson.model.data.MiniComponentData;
import saleson.service.transfer.LogUserAlertRepository;


@RestController
@RequestMapping("/api/users")
public class UserController {
	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UserService userService;


	@Autowired
	UserAlertRepository userAlertRepository;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	MailService mailService;

	@Lazy
	@Autowired
	MoldService moldService;

//	@Autowired
//	ScheduledTask scheduledTask;

	@Autowired
	LogUserAlertRepository logUserAlertRepository;

	@Autowired
	VersioningService  versioningService;

	@Autowired
	PasswordResetTokenService pwTokenService;

	@Autowired
	LocaleResolver localeResolver;
	@Autowired
	ResourceHandler resourceHandler;

	@Autowired
	UserInviteService userInviteService;

	@GetMapping
	public ResponseEntity<Page<User>> users(UserParam param,
											//@PageableDefault(size =1)
											Pageable pageable) {

		Page<User> pageContent = userService.findAll(param, pageable);

		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}


	/**
	 * 회원 등록 (간편)
	 * @param payload
	 * @return
	 */
	@PostMapping
	public ApiResponse post(@RequestBody UserPayload payload) {
		try {
			User user = payload.getModel();

			// 이메일 중복 확인
			/*
					boolean exists = userService.existByLoginIdOrEmail(user.getLoginId(), user.getEmail());
			
					if (exists) {
						return new ApiResponse(false, "Email address is already registered and waiting to be approved. Please use a different email address.");
					}
			*/
			userService.valid(payload, null);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			SecurityUtils.resetPwdExpiredAt(user);
			User finalUser = userService.save(user);
			versioningService.writeHistory(finalUser);
			userService.updateUserAlert(finalUser, payload.getAlertStatus());
			if (finalUser.getRequested() == false) {
				Thread t = new Thread(() -> {
					userService.restoreAlerts(finalUser);
				});
				t.start();
			}
			return new ApiResponse(true, "OK!!", finalUser.getId());
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			return ApiResponse.error(e.getCodeMessage());
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
			return ApiResponse.error();
		}
	}

	/**
	 * 회원 정보 조회
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public ResponseEntity<User> user(@PathVariable(value = "id", required = true) Long id) {
		User user = userService.findById(id);

		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	/**
	 * 로그인한 회원 정보 조회
	 * @return
	 */
	@GetMapping("/my")
	public ResponseEntity<User> my() {
		User user = userService.findById(SecurityUtils.getUserId());

		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	/**
	 * 로그인한 회원 정보 수정
	 * @return
	 */
	@PutMapping("/my")
	public ApiResponse editMy(@RequestBody UserPayload payload) {

		// 비밀번호가 입력된 경우 암호화.
		if (payload.getPassword() != null && !payload.getPassword().isEmpty()) {
			payload.setPassword(passwordEncoder.encode(payload.getPassword()));
		}

		User user = userService.findById(SecurityUtils.getUserId());

		if (user == null) {
			new ApiResponse(true, "ERROR");
		}

		User userFinal = userService.save(payload.getMyModel(user));
		versioningService.writeHistory(userFinal);
//		userService.updateUserAlert(user, payload.getAlertStatus());

		return new ApiResponse(true, "성공.");
	}

	@GetMapping("/alert-status")
	public ResponseEntity alertStatus(@RequestParam(required = false) Long userId,
									  @RequestParam(required = false) CompanyType companyType,
									  @RequestParam(required = false) List<Long> roleIds){
		return ResponseEntity.ok(userService.getAlertStatus(userId, companyType, roleIds));
	}

	@PutMapping("{id}")
	public ApiResponse put(@PathVariable("id") Long id, @RequestBody UserPayload payload) {
		try {
		// 비밀번호가 입력된 경우 암호화.
		if (payload.getPassword() != null && !payload.getPassword().isEmpty()) {
			payload.setPassword(passwordEncoder.encode(payload.getPassword()));
		}

		User user = userService.findById(id);

		if (user == null) {
			new ApiResponse(true, "ERROR");
		}
		User oldToCheck= DataUtils.deepCopyJackSon(user,User.class);
/*
		User updated = payload.getModel();
		updated.setId(user.getId());
		DataUtils.mapCreateAndUpdateInfoBaseDate(updated, user);
		boolean isIdentical = DataUtils.deepCompare(user, updated);
*/


		userService.valid(payload,id);

		boolean isRequesting = user.getRequested() == true ? true : false;
		User finalUser = userService.save(payload.getModel(user));
		User newToCheck = DataUtils.deepCopyJackSon(finalUser, User.class);
		DataUtils.mapCreateAndUpdateInfoBaseDate(oldToCheck, newToCheck);
		boolean isIdentical = DataUtils.deepCompare(oldToCheck, newToCheck);

		if (!isIdentical) {
			versioningService.writeHistory(finalUser);
		}
		userService.updateUserAlert(finalUser, payload.getAlertStatus());
		if (isRequesting) {
			if (user.getRequested() == false && finalUser.isEnabled()) {
				userService.sendRegisterConfirmMail(finalUser, true);
			} else if (user.getRequested() == false && !finalUser.isEnabled()) {
				userService.sendRegisterConfirmMail(finalUser, false);
			}
			Thread t = new Thread(() -> {
				userService.restoreAlerts(finalUser);
			});
			t.start();
		}

		return new ApiResponse(true, "성공.");
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ApiResponse.error(ae.getCodeMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}

	}

	@GetMapping("/test-user-alert")
	public void getUserAlert(){
		User user = userService.findById(113L);
		userService.restoreAlerts(user);
//		List<UserAlert> abc = userAlertRepository.findByUser(user);
	}

	@PostMapping("/test-update-alert/{id}")
	public ResponseEntity testUpdateAlert(@PathVariable(value = "id") Long id){
		User user = userService.findById(id);
		Map<AlertType, Boolean> map = new HashMap<>();
		map.put(AlertType.DISCONNECTED, true);
		map.put(AlertType.DATA_SUBMISSION, true);
		map.put(AlertType.CYCLE_TIME, true);
		return ResponseEntity.ok(userService.updateUserAlert(user, map));
	}


	/**
	 * 계정 활성 / 비활성 처리.
	 * @param id
	 * @param payload
	 * @return
	 */
	@PutMapping("/{id}/enable")
	public ApiResponse user(@PathVariable(value = "id", required = true) Long id, @RequestBody UserPayload payload) {
		if (!payload.getId().equals(id)) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		User user = userService.findById(id);
		user.setEnabled(payload.isEnabled());
		user.setRequested(false);
		User finalUser = userService.save(user);
		versioningService.writeHistory(finalUser);

		return new ApiResponse(true, "OK!!");
	}


	@DeleteMapping("{id}")
	public ApiResponse deleteLocation(@PathVariable("id") Long id) {
		userService.deleteById(id);
		return new ApiResponse(true, Const.SUCCESS);
	}




	/**
	 * 로그인한 회원의 Alert Noti 설정 정보
	 * @return
	 */
	@GetMapping("/notify/{alertType}")
	public ResponseEntity<UserAlert> notify(@PathVariable AlertType alertType) {
		User user = userService.findById(SecurityUtils.getUserId());

		UserAlert userAlert = userAlertRepository.findByUserAndAlertType(user, alertType)
				.orElse(UserAlert.builder().alertOn(true).alertType(alertType).email(true).build());
		if (userAlert.getSpecialAlertType() == null)
		{
			if (alertType.equals(AlertType.CYCLE_TIME) || alertType.equals(AlertType.EFFICIENCY)){
				userAlert.setSpecialAlertType(SpecialAlertType.L1L2);
			}
			if (alertType.equals(AlertType.MAINTENANCE)){
				userAlert.setSpecialAlertType(SpecialAlertType.UPCOMING_OVERDUE);
			}
			if (alertType.equals(AlertType.REFURBISHMENT)){
				userAlert.setSpecialAlertType(SpecialAlertType.MEDIUM_HIGH);
			}
		}

		if (userAlert.getAlertOn() == null)
			userAlert.setAlertOn(true);

		return new ResponseEntity<>(userAlert, HttpStatus.OK);
	}


	@PutMapping("/notify/{alertType}")
	public ResponseEntity notify(@PathVariable AlertType alertType, @RequestBody UserAlert userAlert) {
		if (!SecurityUtils.isLogin()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Login First!"));
		}

		if (alertType != userAlert.getAlertType()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("AlertType is not matched."));
		}

		User user = userService.findById(SecurityUtils.getUserId());

		UserAlert savedUserAlert = null;
		if (userAlert.getId() == null) {	// 등록
			userAlert.setUser(user);
			savedUserAlert = userAlertRepository.save(userAlert);

		} else {		// 수정
			Optional<UserAlert> optional = userAlertRepository.findById(userAlert.getId());

			if (optional.isPresent()) {
				UserAlert existingUserAlert = optional.get();

				modelMapper.map(userAlert, existingUserAlert);
				if (!existingUserAlert.getAlertOn()) {
					existingUserAlert.setEmail(false);
				}
				savedUserAlert = userAlertRepository.save(existingUserAlert);

			} else {
				userAlert.setUser(user);
				savedUserAlert = userAlertRepository.save(userAlert);

			}
		}

		return ResponseEntity.ok(savedUserAlert);
	}

	@PutMapping("/notify/update")
	public ResponseEntity updateNotify(){
		if (!SecurityUtils.isLogin()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Login First!"));
		}
		return ResponseEntity.ok(userService.updateNotify(SecurityUtils.getUserId()));
	}

	@GetMapping("/notify/status")
	public ResponseEntity getNotify(){
		if (!SecurityUtils.isLogin()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Login First!"));
		}
		return ResponseEntity.ok(userService.getNotify(SecurityUtils.getUserId()));
	}

	@GetMapping("/requested-count")
	public ResponseEntity getRequestedUsersCount(){
		if (!SecurityUtils.isLogin()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("Login First!"));
		}
		return ResponseEntity.ok(userService.getRequestedUsersCount());
	}


	@GetMapping("/engineers")
	public ResponseEntity<List<User>> getEngineers(@RequestParam(value = "emailList",required = false) List<String> emailList,
												   @RequestParam(value = "page",required = false)Integer page,
												   @RequestParam(value = "size",required = false)Integer size,
												   @RequestParam(value = "searchText",required = false) String searchText){
		return ResponseEntity.ok(userService.getAllActiveUser(emailList, page,  size, searchText));
	}

	@GetMapping("/type")
	public ResponseEntity isAdmin(){
		if(SecurityUtils.getCompanyType().equals(CompanyType.IN_HOUSE))
			return ResponseEntity.ok("OEM");
		else if(SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER))
			return ResponseEntity.ok("TOOL_MAKER");
		return ResponseEntity.ok("SUPPLIER");
	}

	@Deprecated
	@GetMapping("/server")
	public String getCustomerServer(){
		return ConfigUtils.getServerName();
	}

	@GetMapping("/test-send-email")
	public ResponseEntity sendEmail(){
//		mailService.testMail();
		mailService.sendSystemAlert();
//		moldService.addAlertToUser();
//		return ResponseEntity.ok(mailService.testFunction(SecurityUtils.getUserId()));
		return ResponseEntity.ok("OK");
//		return ResponseEntity.ok(logUserAlertRepository.findByUserIdAndEmail(SecurityUtils.getUserId(), false));
	}

	@GetMapping("/test-date-time")
	public String testDateTime(){
//		return "Start of the month: " + DateTimeUtils.todayIsFirstDayOfMonth() + "\nStart of the week: " + DateTimeUtils.todayIsMonday();
		return "Year week 26: " + DateUtils.getYearWeek("20111231", "yyyyMMdd") +
				"\nYear week 27: " + DateUtils.getYearWeek("20041231", "yyyyMMdd") +
				"\nDate 28: " + DateUtils.getYearWeek("20201228", "yyyyMMdd") +
				"\nNew year: " + DateUtils.getYearWeek("20201229", "yyyyMMdd");
	}

//	@PutMapping("/test-update-op")
//	public ResponseEntity testUpdateOP(){
//		scheduledTask.updateOperationalStatus();
//		return ResponseEntity.ok("OK");
//	}

	@GetMapping("/standardize-data")
	public List<UserAlert> standardize() {
		return userService.updateUserAlert();
	}

	@GetMapping("/fpassword")
	public ModelAndView forgotPassword() {
		ModelAndView mav = new ModelAndView("fpassword");
		return mav;
	}

	@PostMapping("/send_pw_change_mail/{email:.+}")
	public ResponseEntity<?> sendPasswordChangeMail(@PathVariable(value = "email") String email) {
		User user = userService.findByLoginId(email);
		if (user == null) {
			return new ResponseEntity<>(
					"The email address you entered is not registered in the system.",
					HttpStatus.BAD_REQUEST);
		}

		String token = UUID.randomUUID().toString();
		PasswordResetToken pwToken = new PasswordResetToken();
		pwToken.setToken(token);
		pwToken.setEmail(email);
		pwToken.setCreatedAt(Instant.now());
		pwTokenService.save(pwToken);

		boolean emailStatus = mailService.sendResetPWMail(user, token);
		if (!emailStatus) {
			return new ResponseEntity<>(
					"Failed to send email",
					HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity(HttpStatus.OK);
	}

	@GetMapping("/pc_mail_sent/{email:.+}")
	public ModelAndView pcMailSent(@PathVariable(value = "email") String email) {
		ModelAndView mav = new ModelAndView("pc_mail_sent");
		mav.addObject("email", email);
		return mav;
	}

	@GetMapping("/cpassword/{token}")
	public ModelAndView changePassword(@PathVariable(value = "token") String token) {
		PasswordResetToken pwToken = pwTokenService.findByToken(token);
		String errorMessage = null;
		String email = null;
		if (pwToken == null) {
			errorMessage = "Token cannot be found.";
		}
		else if (pwToken.isExpired()) {
			errorMessage = "This password reset link has expired.  Please request a new password reset";

		}
		else {
			email = pwToken.getEmail();
		}
		ModelAndView mav = new ModelAndView("resetpassword");
		mav.addObject("errorMessage", errorMessage);
		mav.addObject("token", token);
		mav.addObject("email", email);
		return mav;
	}

	@PostMapping("/reset_password/{token:.+}/{pw:.+}")
	public ResponseEntity<?> resetPassword(@PathVariable(value = "token") String token, @PathVariable(value = "pw") String pw) throws Exception {

		PasswordResetToken pwToken = pwTokenService.findByToken(token);
		if (pwToken == null) {
			return new ResponseEntity<>(
					"Could not find password reset token",
					HttpStatus.BAD_REQUEST);
		}
		else if (pwToken.isExpired()) {
			return new ResponseEntity<>(
					"This password reset link has expired, please request a new password reset.",
					HttpStatus.BAD_REQUEST);
		}

		User user = userService.findByLoginId(pwToken.getEmail());

		if (user == null) {
			return new ResponseEntity<>(
					"The email address you entered is not registered in the system.",
					HttpStatus.BAD_REQUEST);
		}

		if(user.getPassword() == passwordEncoder.encode(pw)){
			return new ResponseEntity<>(
					"The new password should be different from old password. Please use different password.",
					HttpStatus.BAD_REQUEST);
		}

		user.setPassword(passwordEncoder.encode(pw));
		SecurityUtils.resetPwdExpiredAt(user);
		user.setAccountLocked(false);
		user.setFailedAttempt(0);
		userService.save(user);
		pwTokenService.delete(pwToken);
		return new ResponseEntity(HttpStatus.OK);
	}

	@GetMapping("/password_changed")
	public ModelAndView passwordChanged() {
		ModelAndView mav = new ModelAndView("resetpasswordsuccess");
		return mav;
	}

	@PostMapping("/change_language/{language}")
	public ResponseEntity<?> changeLanguage(@PathVariable(value = "language") String language,
											HttpServletRequest request,
											HttpServletResponse response) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext.getAuthentication().getPrincipal() instanceof UserPrincipal) {
			UserPrincipal up = (UserPrincipal) securityContext.getAuthentication().getPrincipal();
			User user = userService.findByLoginId(up.getEmail());

			if (user == null) {
				return new ResponseEntity<>(
						"User does not exist.",
						HttpStatus.BAD_REQUEST);
			}
			user.setLanguage(language);
			userService.save(user);
			//up.setLanguage(language);

			localeResolver.setLocale(request, response, StringUtils.parseLocale(language));
			resourceHandler.updateLang(language);

			return new ResponseEntity(HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(
					"You must login first to change your language.",
					HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/get_lang")
	public String getLanguage() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext.getAuthentication().getPrincipal() instanceof UserPrincipal) {
			UserPrincipal up = (UserPrincipal) securityContext.getAuthentication().getPrincipal();
			User user = userService.findByLoginId(up.getEmail());
			return user.getLanguage() == null ? "EN" : user.getLanguage();
		}
		else {
			return "";
		}
	}

	@GetMapping("restore-missing-alerts")
	public ResponseEntity restoreMissingAlerts(){
		userService.fixDefaultPeriodType();
		userService.restoreMissingAlerts();
		return ResponseEntity.ok("Success");
	}

	@GetMapping("fix-default-period-type-alerts")
	public ResponseEntity fixDefaultPeriodType(){
		userService.fixDefaultPeriodType();
		return ResponseEntity.ok("Success");
	}

	@GetMapping("fix-default-config-alert")
	public ResponseEntity fixDefaultConfigAlert(){
		userService.fixDefaultConfigAlert();
		return ResponseEntity.ok("Success");
	}

	@GetMapping("/my-menus")
	public ResponseEntity<?> getMyMenus(){
		return ResponseEntity.ok(userService.getMyMenus());
	}

	@PostMapping("/invite-user")
	public ApiResponse inviteUser(@RequestBody UserInvitePayload payload){
		try {
			ApiResponse valid = userInviteService.validInviteUser(payload);
			if (valid != null) {
				return valid;
			}
			List<UserInvite> userInviteList = payload.getModels();
			List<UserInvite> successList= userInviteService.inviteUser(userInviteList);
			return new ApiResponse(true, Const.SUCCESS, successList.size());
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}
	}

	@GetMapping("/create-user/{hashCode}")
	public ApiResponse getInvitation(@PathVariable("hashCode") Integer hashCode){
		return userInviteService.getInvitation(hashCode);
	}

	@PostMapping("/create-user")
	public ApiResponse createUser(@RequestBody UserPayload payload){
		return userInviteService.createUser(payload);
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto){
		return userService.changeStatusInBatch(dto);
	}

	@GetMapping("/exportExcel")
	public void printExcel(HttpServletResponse response,
						   @RequestParam(required = false) List<Long> ids,
						   @RequestParam(name = "fileName", defaultValue = "user-detail", required = false) String fileName, Pageable pageable){
		try {
			OutputStream outputStream = response.getOutputStream();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=user-report-"+new Date().getTime()+".xlsx");
			outputStream.write(userService.exportLoginAuditTrail(ids, pageable).toByteArray());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
