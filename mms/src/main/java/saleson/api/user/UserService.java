package saleson.api.user;

import static saleson.common.config.Const.SECUTIRY.LOCK_TIME_DURATION;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.emoldino.framework.util.BeanUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.extern.slf4j.Slf4j;
import saleson.api.mold.MoldService;
import saleson.api.notification.NotificationService;
import saleson.api.role.RoleRepository;
import saleson.api.role.RoleService;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.user.payload.UserParam;
import saleson.api.user.payload.UserPayload;
import saleson.api.versioning.repositories.UserVersionRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.config.Const;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.CountryCode;
import saleson.common.enumeration.PeriodType;
import saleson.common.enumeration.RoleUserData;
import saleson.common.enumeration.SpecialAlertType;
import saleson.common.notification.MailService;
import saleson.common.payload.ApiResponse;
import saleson.common.util.ExcelUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.LogUserAlert;
import saleson.model.Menu;
import saleson.model.MoldCorrective;
import saleson.model.MoldCycleTime;
import saleson.model.MoldDataSubmission;
import saleson.model.MoldDetachment;
import saleson.model.MoldDisconnect;
import saleson.model.MoldDowntimeEvent;
import saleson.model.MoldEfficiency;
import saleson.model.MoldLocation;
import saleson.model.MoldMaintenance;
import saleson.model.MoldMisconfigure;
import saleson.model.MoldRefurbishment;
import saleson.model.PasswordResetToken;
import saleson.model.PasswordResetTokenService;
import saleson.model.QUser;
import saleson.model.QUserAlert;
import saleson.model.Role;
import saleson.model.TabTable;
import saleson.model.TabTableData;
import saleson.model.TerminalDisconnect;
import saleson.model.User;
import saleson.model.UserAlert;
import saleson.model.clone.UserVersion;
import saleson.service.transfer.LogUserAlertRepository;

@Slf4j
@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	RoleService roleService;

	@Autowired
	ExcelUtils excelUtils;

	@Lazy
	@Autowired
	MoldService moldService;

	@Autowired
	UserAlertRepository userAlertRepository;

	@Autowired
	LogUserAlertRepository logUserAlertRepository;

	@Autowired
	MailService mailService;

	@Autowired
	UserVersionRepository userVersionRepository;

	@Autowired
	VersioningService versioningService;
	@Autowired
	PasswordResetTokenService pwTokenService;

	@Autowired
	private TabTableRepository tabTableRepository;

	@Autowired
	private TabTableDataRepository tabTableDataRepository;

	public Page<User> findAll(UserParam searchParam, Pageable pageable) {
		changeTabPayload(searchParam);
		return userRepository.findAll(searchParam, pageable);
	}
	public List<Long> getAllIds(UserParam payLoad) {
		changeTabPayload(payLoad);
		QUser user = QUser.user;
		JPQLQuery<Long> query = com.emoldino.framework.util.BeanUtils.get(JPAQueryFactory.class)
				.select(Projections.constructor(Long.class, user.id))
				.from(user);
		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(user.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(user.id.eq(SecurityUtils.getUserId()))//
					.or(user.isPublic.isTrue()));
		}
		if (!StringUtils.isEmpty(payLoad.getQuery())) {
			query.where(user.name.like('%' + payLoad.getQuery() + '%')//
					.or(user.email.like('%' + payLoad.getQuery() + '%'))//
					.or(user.company.name.contains(payLoad.getQuery())));
		}
		if (!StringUtils.isEmpty(payLoad.getStatus())) {
			String status = payLoad.getStatus();

			if ("active".equalsIgnoreCase(status)) {
				query.where(user.enabled.isTrue().and(user.requested.isNull().or(user.requested.isFalse())));

			} else if ("disabled".equalsIgnoreCase(status)) {
				query.where(user.enabled.isFalse().and(user.requested.isNull().or(user.requested.isFalse())));

			} else if ("admins".equalsIgnoreCase(status)) {
				query.where(user.admin.isTrue().and(user.requested.isNull().or(user.requested.isFalse())));
			} else if ("requested".equalsIgnoreCase(status)) {
				query.where(user.requested.isTrue());

			}
		}
		if (!StringUtils.isEmpty(payLoad.getCompanyType())) {
			String status = payLoad.getCompanyType();

			if ("in_house".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.IN_HOUSE));

			} else if ("tool_maker".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.TOOL_MAKER));

			} else if ("supplier".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER));

			} else if ("non_admin".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER).or(user.company.companyType.eq(CompanyType.TOOL_MAKER)));
			} else if ("non_inhouse".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER)
						.and(user.company.companyType.eq(CompanyType.TOOL_MAKER).and(user.company.companyType.eq(CompanyType.IN_HOUSE))));
			}

		}

		if (!StringUtils.isEmpty(payLoad.getAccessLevel())) {
			String status = payLoad.getAccessLevel();

			if ("rest_user".equalsIgnoreCase(status)) {
				query.where(user.roleUserData.eq(RoleUserData.ROLE_REST_USER));

			} else if ("regular".equalsIgnoreCase(status)) {
				query.where(user.admin.isFalse());
			} else if ("all_admins".equalsIgnoreCase(status)) {
				query.where(user.admin.isTrue());
			}
		}

		if (payLoad.getId() != null) {
			query.where(user.id.eq(payLoad.getId()));
		}
		//other filter
		if (payLoad.getUserIds() != null || (payLoad.getIsDefaultTab() != null && !payLoad.getIsDefaultTab())) {
			query.where(user.id.in(payLoad.getUserIds()));
		}
		//add filter delete
		query.where(user.deleted.isFalse());

		QueryResults<Long> results = query.fetchResults();
		return results.getResults();
	}

	public boolean existByLoginIdOrEmail(String loginId, String email) {

		Optional<User> user = userRepository.findByLoginIdOrEmailAndDeletedIsFalse(loginId, email);
		if (user.isPresent()) {
			return true;
		}
		return false;
	}


	List<User> findAll() {
		return userRepository.findAll();
	}

	public User findByLoginId(String loginId) {
		return userRepository.findByLoginIdAndDeletedIsFalse(loginId).orElse(null);
	}

	public User findById(Long id) {
		return userRepository.getOne(id);
	}

	public User getUserById(Long id){
		return userRepository.findById(id).orElse(null);
	}

	@Transactional
	public User save(User user) {
		Optional<Role> adminRole = roleRepository.findByAuthority("ROLE_ADMIN");
		if (adminRole.isPresent()) {
			// admin role 삭제
			if (!user.isAdmin() && user.getRoles() != null && !user.getRoles().isEmpty()) {
				user.getRoles().removeIf(role -> role.getId().equals(adminRole.get().getId()));
			}

			// admin인 경우 ROLE_ADMIN을 부여함..
			if (user.isAdmin()) {
				if (user.getRoles() == null || user.getRoles().isEmpty()) {
					Set<Role> roleSet = new HashSet<>();
					roleSet.add(adminRole.get());
					user.setRoles(roleSet);
				} else {
					user.getRoles().add(adminRole.get());
				}
			}
		}

		boolean isNew = user.getId() == null;
		user = userRepository.save(user);
		updateDefaultAlertConfig(user);
		if (isNew && ValueUtils.toBoolean(user.getRequested(), false)) {
			sendAccessRequest(user);
		}

		return user;
	}
	
	public void sendAccessRequest(User user) {
		List<User> admins = userRepository.findByCompanyTypeAndIsEmoldinoIsTrueAndEnabledIsTrue(CompanyType.IN_HOUSE);
		BeanUtils.get(NotificationService.class).createUserRequestAccessNotification(admins, user);
//		mailService.sendRegistrationMail(admins, Arrays.asList(user));
	}

	public void updateDefaultAlertConfig(User user){
		List<UserAlert> userAlertList = new ArrayList<>();
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.RELOCATION)){
			userAlertList.add(new UserAlert(true, null, AlertType.RELOCATION, user, false, PeriodType.WEEKLY));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.DISCONNECTED)
				&& !userAlertRepository.existsByUserAndAlertType(user, AlertType.TERMINAL_DISCONNECTED)){
			userAlertList.add(new UserAlert(true, null, AlertType.DISCONNECTED, user, false, PeriodType.REAL_TIME));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.CYCLE_TIME)){
			userAlertList.add(new UserAlert(true, SpecialAlertType.L1L2, AlertType.CYCLE_TIME, user, false, PeriodType.DAILY));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.MAINTENANCE)){
			userAlertList.add(new UserAlert(true, SpecialAlertType.UPCOMING_OVERDUE,AlertType.MAINTENANCE, user, false, PeriodType.DAILY));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.CORRECTIVE_MAINTENANCE)){
			userAlertList.add(new UserAlert(true, null,AlertType.CORRECTIVE_MAINTENANCE, user, false, PeriodType.REAL_TIME));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.EFFICIENCY)){
			userAlertList.add(new UserAlert(true, null,AlertType.EFFICIENCY, user, false, PeriodType.DAILY));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.MISCONFIGURE)){
			userAlertList.add(new UserAlert(true, null,AlertType.MISCONFIGURE, user, false, PeriodType.REAL_TIME));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.DATA_SUBMISSION)){
			userAlertList.add(new UserAlert(true, null,AlertType.DATA_SUBMISSION, user, false, PeriodType.REAL_TIME));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.REFURBISHMENT)){
			userAlertList.add(new UserAlert(true, null,AlertType.REFURBISHMENT, user, false, PeriodType.REAL_TIME));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.DETACHMENT)){
			userAlertList.add(new UserAlert(true, null,AlertType.DETACHMENT, user, false, PeriodType.REAL_TIME));
		}
		if(!userAlertRepository.existsByUserAndAlertType(user, AlertType.DOWNTIME)){
			userAlertList.add(new UserAlert(true, null,AlertType.DOWNTIME, user, false, PeriodType.DAILY));
		}
		userAlertRepository.saveAll(userAlertList);
	}

	public void deleteById(Long id) {
		User user = userRepository.findById(id).orElse(null);
		if(user!=null){
			user.setDeleted(true);
			userRepository.save(user);
		}
//		userRepository.deleteById(id);
	}

	public Boolean updateNotify(Long userId){
		User user = findById(userId);
		if(user.getNotify() == null || user.getNotify() == true){ user.setNotify(false); }
		else user.setNotify(true);
		user = userRepository.save(user);
		return user.getNotify();
	}

	public Boolean getNotify(Long userId){
		User user = findById(userId);
		if(user.getNotify() == null) return true;
		return user.getNotify();
	}

	public Long getRequestedUsersCount(){
		Long alertCount = userRepository.countByRequestedAndDeletedIsFalse(true);
		return alertCount;
	}

	public List<User> getEngineers(){
		return userRepository.findByCompanyTypeAndOrderByName(CompanyType.IN_HOUSE.name());
	}
	public List<User> getAllActiveUser(List<String> emailList, Integer page, Integer size, String searchText){
		UserParam searchParam= new UserParam();
		searchParam.setEnabled(true);
		searchParam.setStatus("active");
		Pageable pageable;
		if (page != null && size != null) {
			pageable= PageRequest.of(page, size, new Sort(Sort.Direction.ASC, "email"));
			return userRepository.findAllUserActiveByEmailIn(emailList, searchText, pageable);
		} else {
			pageable= PageRequest.of(0, Integer.MAX_VALUE, new Sort(Sort.Direction.ASC, "name"));
			Page<User> pageContent = findAll(searchParam, pageable);
			return pageContent.getContent();
		}
//		return userRepository.findByEnabledIsTrueOrderByName();
	}
	public List<User> getAllDataLeakUserInList(List<Long> userIds){
		UserParam searchParam= new UserParam();
//		searchParam.setEnabled(true);
		searchParam.setUserIds(userIds);
		Pageable pageable= PageRequest.of(0, Integer.MAX_VALUE, new Sort(Sort.Direction.ASC, "name"));
		Page<User> pageContent = findAll(searchParam, pageable);
		return pageContent.getContent();
//		return userRepository.findByEnabledIsTrueOrderByName();
	}

	public List<UserAlert> getAlertStatus(Long userId, CompanyType companyType, List<Long> roleIds){
		QUserAlert userAlert = QUserAlert.userAlert;
		BooleanBuilder builder = new BooleanBuilder();
		if(!"dyson".equalsIgnoreCase(ConfigUtils.getServerName())){
			builder.and(userAlert.alertType.ne(AlertType.DATA_SUBMISSION));
		}
		List<AlertType> alertTypes = new ArrayList<>();
		if(companyType != null && !companyType.equals(CompanyType.IN_HOUSE)){
			List<Menu> menuAlerts = roleService.getMenuAlertByRoleIdIn(roleIds);
			menuAlerts.forEach(menu -> {
				if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_locations)){
					alertTypes.add(AlertType.RELOCATION);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_disconnected)){
					alertTypes.add(AlertType.DISCONNECTED);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_cycle_time)){
					alertTypes.add(AlertType.CYCLE_TIME);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_maintenance)){
					alertTypes.add(AlertType.MAINTENANCE);
					alertTypes.add(AlertType.CORRECTIVE_MAINTENANCE);
//					alertTypes.add(AlertType.REFURBISHMENT);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_refurbishment)){
					alertTypes.add(AlertType.REFURBISHMENT);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_detachment)){
					alertTypes.add(AlertType.DETACHMENT);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_efficiency)){
					alertTypes.add(AlertType.EFFICIENCY);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_misconfigured)){
					alertTypes.add(AlertType.MISCONFIGURE);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_data_submission)){
					alertTypes.add(AlertType.DATA_SUBMISSION);
				}else if(menu.getMenuKey().equals(Const.ALERT_PATH.mold_downtime)){
					alertTypes.add(AlertType.DOWNTIME);
				}
			});
			builder.and(userAlert.alertType.in(alertTypes));
		}
		if(userId != null){
			builder.and(userAlert.user.id.eq(userId));
			List<UserAlert> userAlertList = StreamSupport.stream(userAlertRepository.findAll(builder).spliterator(), false)
					.collect(Collectors.toList());
			List<AlertType> currentAlertType = userAlertList.stream().map(x -> x.getAlertType()).collect(Collectors.toList());
			alertTypes.forEach(alertType -> {
				if(!currentAlertType.contains(alertType)){
					userAlertList.add(new UserAlert(alertType, false));
				}
			});
			return userAlertList;
		}
		List<UserAlert> userAlertList = StreamSupport.stream(userAlertRepository.findAll(builder).spliterator(), false)
				.collect(Collectors.toList());
		if(userAlertList == null || userAlertList.size() == 0) return new ArrayList<>();
		List<AlertType> newAlertTypes = userAlertList.stream().map(x -> x.getAlertType()).distinct().collect(Collectors.toList());
		List<UserAlert> newUserAlerts = new ArrayList<>();
		newAlertTypes.forEach(alertType -> {
			newUserAlerts.add(new UserAlert(alertType, false));
		});
		return newUserAlerts;
	}

	// Update user alerts by a set of configuration
	public List<UserAlert> updateUserAlert(User user, Map<AlertType, Boolean> alertStatus){
		if(alertStatus == null || alertStatus.size() == 0) return null;
		List<UserAlert> userAlertList = userAlertRepository.findByUserAndAlertTypeIn(user, new ArrayList<>(alertStatus.keySet()));
		userAlertList.forEach(userAlert -> {
			userAlert.setEmail(alertStatus.get(userAlert.getAlertType()));
		});
		return userAlertRepository.saveAll(userAlertList);
	}

	public List<UserAlert> updateUserAlert(User user, UserVersion userVersion){
		return updateUserAlert(user, versioningService.getAlertStatusFromUserVersion(userVersion));
	}

	@Transactional
	public void restoreAlerts(User user){

		List<UserAlert> userAlertList = userAlertRepository.findByUser(user);
		Map<AlertType, Object> alertTypeObjectMap = moldService.getAllLatestAlert();
		Map<AlertType,PeriodType> alertTypePeriodTypeMap= new HashMap<>();
		userAlertList.stream().forEach(userAlert -> alertTypePeriodTypeMap.put(userAlert.getAlertType(),userAlert.getPeriodType()));
		List<MoldLocation> moldLocations = alertTypeObjectMap.containsKey(AlertType.RELOCATION) ? (List<MoldLocation>) alertTypeObjectMap.get(AlertType.RELOCATION) : null;
		List<MoldDisconnect> moldDisconnects = alertTypeObjectMap.containsKey(AlertType.DISCONNECTED) ? (List<MoldDisconnect>) alertTypeObjectMap.get(AlertType.DISCONNECTED) : null;
		List<TerminalDisconnect> terminalDisconnects = alertTypeObjectMap.containsKey(AlertType.TERMINAL_DISCONNECTED) ? (List<TerminalDisconnect>) alertTypeObjectMap.get(AlertType.TERMINAL_DISCONNECTED) : null;
		List<MoldCycleTime> moldCycleTimes = alertTypeObjectMap.containsKey(AlertType.CYCLE_TIME) ? (List<MoldCycleTime>) alertTypeObjectMap.get(AlertType.CYCLE_TIME) : null;
		List<MoldMaintenance> moldMaintenances = alertTypeObjectMap.containsKey(AlertType.MAINTENANCE) ? (List<MoldMaintenance>) alertTypeObjectMap.get(AlertType.MAINTENANCE) : null;
		List<MoldCorrective> moldCorrectiveMaintenances = alertTypeObjectMap.containsKey(AlertType.CORRECTIVE_MAINTENANCE) ? (List<MoldCorrective>) alertTypeObjectMap.get(AlertType.CORRECTIVE_MAINTENANCE) : null;
		List<MoldMisconfigure> moldMisconfigures = alertTypeObjectMap.containsKey(AlertType.MISCONFIGURE) ? (List<MoldMisconfigure>) alertTypeObjectMap.get(AlertType.MISCONFIGURE) : null;
		List<MoldEfficiency> moldEfficiencies = alertTypeObjectMap.containsKey(AlertType.EFFICIENCY) ? (List<MoldEfficiency>) alertTypeObjectMap.get(AlertType.EFFICIENCY) : null;
		List<MoldDataSubmission> moldDataSubmissions = alertTypeObjectMap.containsKey(AlertType.DATA_SUBMISSION) ? (List<MoldDataSubmission>) alertTypeObjectMap.get(AlertType.DATA_SUBMISSION) : null;

		List<MoldRefurbishment> moldEndLifeCycleHistories= alertTypeObjectMap.containsKey(AlertType.REFURBISHMENT) ? (List<MoldRefurbishment>) alertTypeObjectMap.get(AlertType.REFURBISHMENT) : null;
		List<MoldDetachment> moldDetachmentList= alertTypeObjectMap.containsKey(AlertType.DETACHMENT) ? (List<MoldDetachment>) alertTypeObjectMap.get(AlertType.DETACHMENT) : null;
		List<MoldDowntimeEvent> moldDowntimeEventList = alertTypeObjectMap.containsKey(AlertType.DOWNTIME) ? (List<MoldDowntimeEvent>) alertTypeObjectMap.get(AlertType.DOWNTIME) : null;

		if(moldCycleTimes!=null){
			moldCycleTimes=moldCycleTimes.stream().filter(m->alertTypePeriodTypeMap.get(AlertType.CYCLE_TIME)!=null && alertTypePeriodTypeMap.get(AlertType.CYCLE_TIME).equals(m.getPeriodType()) ).collect(Collectors.toList());
		}
		if(moldEfficiencies!=null){
			moldEfficiencies=moldEfficiencies.stream().filter(m->alertTypePeriodTypeMap.get(AlertType.EFFICIENCY)!=null && alertTypePeriodTypeMap.get(AlertType.EFFICIENCY).equals(m.getPeriodType()) ).collect(Collectors.toList());
		}
		Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
		List<LogUserAlert> logUserAlerts = moldService.generateLogUserAlert(userAlertMap, moldLocations, moldDisconnects,
				terminalDisconnects, moldCycleTimes, moldMaintenances, moldMisconfigures, moldEfficiencies, moldDataSubmissions, moldCorrectiveMaintenances);

		if (moldEndLifeCycleHistories != null && !moldEndLifeCycleHistories.isEmpty()) {
			List<LogUserAlert> logUserAlerts1 = moldService.generateLogUserAlert(userAlertMap, moldEndLifeCycleHistories,false);
			logUserAlerts.addAll(logUserAlerts1);
		}
		if (moldDetachmentList != null && !moldDetachmentList.isEmpty()) {
			List<LogUserAlert> logUserAlerts1 = moldService.generateLogUserAlertDetachment(userAlertMap, moldDetachmentList,false);
			logUserAlerts.addAll(logUserAlerts1);
		}
		if (moldDowntimeEventList != null && !moldDowntimeEventList.isEmpty()) {
			List<LogUserAlert> logUserAlerts1 = moldService.generateLogUserDowntimeAlert(userAlertMap, moldDowntimeEventList,false);
			logUserAlerts.addAll(logUserAlerts1);
		}
		logUserAlertRepository.saveAll(logUserAlerts);
	}

	// Fake-method to resolve the terminal/tooling disconnection issue
	public List<UserAlert> updateUserAlert(){
		List<UserAlert> userAlerts = userAlertRepository.findByAlertTypeIn(Arrays.asList(AlertType.TERMINAL_DISCONNECTED, AlertType.DISCONNECTED));
		List<Long> userIds = userAlerts.stream().map(x -> x.getUser().getId()).distinct().collect(Collectors.toList());
		List<UserAlert> newList = new ArrayList<>();
		userIds.forEach(userId -> {
			UserAlert original = userAlerts.stream().filter(x -> x.getUser().getId().equals(userId)).findAny().orElse(null);
			if(original != null) {
				UserAlert userAlert = new UserAlert();
				userAlert.setAlertType(AlertType.DISCONNECTED);
				userAlert.setEmail(original.getEmail());
				userAlert.setPeriodType(PeriodType.REAL_TIME);
				userAlert.setUser(original.getUser());
				newList.add(userAlert);
			}
		});
		userAlertRepository.deleteAll(userAlerts);
		return userAlertRepository.saveAll(newList);
	}

	// Fake-method to test sending mail
	public void sendRegisterConfirmMail(User user, Boolean approval){
//		User user = findById(37L);
		mailService.sendRegisterConfirmMail(user, approval);
//		List<User> aList = Arrays.asList(user);
//		User user1 = findById(90L);
//		List<User> aList1 = Arrays.asList(user1);
//		mailService.sendRegistrationMail(aList, aList1);
	}

	public void restoreMissingAlertsOld(){
		log.info("Start restoreMissingAlerts");
		Map<AlertType, Object> alertTypeObjectMap = moldService.getAllLatestAlert();
//		List<MoldLocation> moldLocations = alertTypeObjectMap.containsKey(AlertType.RELOCATION) ? (List<MoldLocation>) alertTypeObjectMap.get(AlertType.RELOCATION) : null;
//		List<MoldDisconnect> moldDisconnects = alertTypeObjectMap.containsKey(AlertType.DISCONNECTED) ? (List<MoldDisconnect>) alertTypeObjectMap.get(AlertType.DISCONNECTED) : null;
//		List<TerminalDisconnect> terminalDisconnects = alertTypeObjectMap.containsKey(AlertType.TERMINAL_DISCONNECTED) ? (List<TerminalDisconnect>) alertTypeObjectMap.get(AlertType.TERMINAL_DISCONNECTED) : null;
//		List<MoldCycleTime> moldCycleTimes = alertTypeObjectMap.containsKey(AlertType.CYCLE_TIME) ? (List<MoldCycleTime>) alertTypeObjectMap.get(AlertType.CYCLE_TIME) : null;
		List<MoldMaintenance> moldMaintenances = alertTypeObjectMap.containsKey(AlertType.MAINTENANCE) ? (List<MoldMaintenance>) alertTypeObjectMap.get(AlertType.MAINTENANCE) : null;
//		List<MoldCorrective> moldCorrectiveMaintenances = alertTypeObjectMap.containsKey(AlertType.CORRECTIVE_MAINTENANCE) ? (List<MoldCorrective>) alertTypeObjectMap.get(AlertType.CORRECTIVE_MAINTENANCE) : null;
//		List<MoldMisconfigure> moldMisconfigures = alertTypeObjectMap.containsKey(AlertType.MISCONFIGURE) ? (List<MoldMisconfigure>) alertTypeObjectMap.get(AlertType.MISCONFIGURE) : null;
//		List<MoldEfficiency> moldEfficiencies = alertTypeObjectMap.containsKey(AlertType.EFFICIENCY) ? (List<MoldEfficiency>) alertTypeObjectMap.get(AlertType.EFFICIENCY) : null;
//		List<MoldDataSubmission> moldDataSubmissions = alertTypeObjectMap.containsKey(AlertType.DATA_SUBMISSION) ? (List<MoldDataSubmission>) alertTypeObjectMap.get(AlertType.DATA_SUBMISSION) : null;
//
//		List<MoldRefurbishment> moldEndLifeCycleHistories= alertTypeObjectMap.containsKey(AlertType.REFURBISHMENT) ? (List<MoldRefurbishment>) alertTypeObjectMap.get(AlertType.REFURBISHMENT) : null;
//		List<MoldDetachment> moldDetachmentList= alertTypeObjectMap.containsKey(AlertType.DETACHMENT) ? (List<MoldDetachment>) alertTypeObjectMap.get(AlertType.DETACHMENT) : null;

//		List<UserAlert> userAlertList = userAlertRepository.findByUser(user);
		List<UserAlert> userAlertList = userAlertRepository.findAll();

		//fill exist
		Map<User, List<AlertType>> userAlertMap = mailService.getUserAlertListMap(userAlertList);
		List<LogUserAlert> logUserAlertMaintenancesOld=logUserAlertRepository.findByAlertTypeAndAlertIdIn(AlertType.MAINTENANCE, moldMaintenances != null ? moldMaintenances.stream().map(m -> m.getId()).collect(Collectors.toList()) : new ArrayList<>());
		for (User user : userAlertMap.keySet()) {
			List<Long> listAlertIdExist=logUserAlertMaintenancesOld.stream().filter(l-> user.getId().equals(l.getUserId())).map(l->l.getAlertId()).collect(Collectors.toList());
			List<MoldMaintenance> moldMaintenancesAdd = moldMaintenances.stream().filter(m->!listAlertIdExist.contains(m.getId())).collect(Collectors.toList());
			List<LogUserAlert> logUserAlerts = moldService.generateLogUserAlert(new HashMap<User, List<AlertType>>(){{put(user,userAlertMap.get(user));}}, null, null,
					null, null, moldMaintenancesAdd, null, null, null, null);
			log.info("user: "+user.getEmail() +" mum MoldMaintenance exist: "+listAlertIdExist.size() +" num not exist: "+moldMaintenancesAdd.size()+" num add new: "+logUserAlerts.size());
//
//		if (moldEndLifeCycleHistories != null && !moldEndLifeCycleHistories.isEmpty()) {
//			List<LogUserAlert> logUserAlerts1 = moldService.generateLogUserAlert(userAlertMap, moldEndLifeCycleHistories);
//			logUserAlerts.addAll(logUserAlerts1);
//		}
//		if (moldDetachmentList != null && !moldDetachmentList.isEmpty()) {
//			List<LogUserAlert> logUserAlerts1 = moldService.generateLogUserAlertDetachment(userAlertMap, moldDetachmentList);
//			logUserAlerts.addAll(logUserAlerts1);
//		}
			logUserAlertRepository.saveAll(logUserAlerts);
		}

		log.info("End restoreMissingAlerts");

	}

	public void restoreMissingAlerts() {
		int total = 0;
		Instant start = Instant.now();
		log.info("Start restoreMissingAlerts");
		Map<AlertType, Object> alertTypeObjectMap = moldService.getAllLatestAlert();
		List<MoldLocation> moldLocations = alertTypeObjectMap.containsKey(AlertType.RELOCATION) ? (List<MoldLocation>) alertTypeObjectMap.get(AlertType.RELOCATION) : null;
		List<MoldDisconnect> moldDisconnects = alertTypeObjectMap.containsKey(AlertType.DISCONNECTED) ? (List<MoldDisconnect>) alertTypeObjectMap.get(AlertType.DISCONNECTED) : null;
		List<TerminalDisconnect> terminalDisconnects = alertTypeObjectMap.containsKey(AlertType.TERMINAL_DISCONNECTED) ? (List<TerminalDisconnect>) alertTypeObjectMap.get(AlertType.TERMINAL_DISCONNECTED) : null;
		List<MoldCycleTime> moldCycleTimes = alertTypeObjectMap.containsKey(AlertType.CYCLE_TIME) ? (List<MoldCycleTime>) alertTypeObjectMap.get(AlertType.CYCLE_TIME) : null;
		List<MoldMaintenance> moldMaintenances = alertTypeObjectMap.containsKey(AlertType.MAINTENANCE) ? (List<MoldMaintenance>) alertTypeObjectMap.get(AlertType.MAINTENANCE) : null;
		List<MoldCorrective> moldCorrectiveMaintenances = alertTypeObjectMap.containsKey(AlertType.CORRECTIVE_MAINTENANCE) ? (List<MoldCorrective>) alertTypeObjectMap.get(AlertType.CORRECTIVE_MAINTENANCE) : null;
		List<MoldMisconfigure> moldMisconfigures = alertTypeObjectMap.containsKey(AlertType.MISCONFIGURE) ? (List<MoldMisconfigure>) alertTypeObjectMap.get(AlertType.MISCONFIGURE) : null;
		List<MoldEfficiency> moldEfficiencies = alertTypeObjectMap.containsKey(AlertType.EFFICIENCY) ? (List<MoldEfficiency>) alertTypeObjectMap.get(AlertType.EFFICIENCY) : null;
		List<MoldDataSubmission> moldDataSubmissions = alertTypeObjectMap.containsKey(AlertType.DATA_SUBMISSION) ? (List<MoldDataSubmission>) alertTypeObjectMap.get(AlertType.DATA_SUBMISSION) : null;
//
		List<MoldRefurbishment> moldEndLifeCycleHistories = alertTypeObjectMap.containsKey(AlertType.REFURBISHMENT) ? (List<MoldRefurbishment>) alertTypeObjectMap.get(AlertType.REFURBISHMENT) : null;
		List<MoldDetachment> moldDetachmentList = alertTypeObjectMap.containsKey(AlertType.DETACHMENT) ? (List<MoldDetachment>) alertTypeObjectMap.get(AlertType.DETACHMENT) : null;

//		List<UserAlert> userAlertList = userAlertRepository.findByUser(user);
		List<UserAlert> userAlertList = userAlertRepository.findAll();

		//fill exist
		Map<User, Map<AlertType,PeriodType>> userAlertMapAll = mailService.getUserAlertListMapEntry(userAlertList);
/*
		List<Long> allAlertIdList=new ArrayList<>();
		try{
			alertTypeObjectMap.keySet().stream().forEach(key->{
				List<Object> alertList=(List<Object>)alertTypeObjectMap.get(key);
				for (Object o:alertList) {
					try {
						Method field = o.getClass().getDeclaredMethod("getId");
						Long value=(Long) field.invoke(o);
						if(value!=null) allAlertIdList.add(value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
*/
//		List<LogUserAlert> logUserAlertMaintenancesOld=logUserAlertRepository.findByAlertTypeAndAlertIdIn(AlertType.MAINTENANCE, moldMaintenances != null ? moldMaintenances.stream().map(m -> m.getId()).collect(Collectors.toList()) : new ArrayList<>());
		for (User user : userAlertMapAll.keySet()) {
			Map<User, List<AlertType>> userAlertMap=new HashMap<User, List<AlertType>>() {{
				put(user, userAlertMapAll.get(user).keySet().stream().collect(Collectors.toList()));
			}};
			Map<AlertType,PeriodType> alertPeriodMap= userAlertMapAll.get(user);

			Instant st1 = Instant.now();
			Map<AlertType, List<Long>> mapExistId = mapListAlertIdsExist(alertTypeObjectMap, user);
//			List<LogUserAlert> logUserAlertOld=logUserAlertRepository.findByUserIdAndAlertIdIn(user.getId(), allAlertIdList);
//			List<Long> listAlertIdExist= logUserAlertOld.stream().map(o->o.getAlertId()).collect(Collectors.toList());
/*
			List<LogUserAlert> logUserAlertMaintenancesOld=logUserAlertRepository.findByUserIdAndAlertTypeAndAlertIdIn(user.getId(),AlertType.MAINTENANCE,
					moldMaintenances != null ? moldMaintenances.stream().map(m -> m.getId()).collect(Collectors.toList()) : new ArrayList<>());
			List<Long> listMaintenanceAlertIdExist=logUserAlertMaintenancesOld.stream().map(l->l.getAlertId()).collect(Collectors.toList());
*/
//			List<Long> listMaintenanceAlertIdExist=logUserAlertMaintenancesOld.stream().filter(l-> user.getId().equals(l.getUserId())).map(l->l.getAlertId()).collect(Collectors.toList());


			List<MoldMaintenance> moldMaintenancesAdd = moldMaintenances.stream().filter(m -> mapExistId.containsKey(AlertType.MAINTENANCE) && !mapExistId.get(AlertType.MAINTENANCE).contains(m.getId())).collect(Collectors.toList());
			List<MoldLocation> moldLocationsAdd = moldLocations.stream().filter(m -> mapExistId.containsKey(AlertType.RELOCATION) && !mapExistId.get(AlertType.RELOCATION).contains(m.getId())).collect(Collectors.toList());
			List<MoldDisconnect> moldDisconnectsAdd = moldDisconnects.stream().filter(m -> mapExistId.containsKey(AlertType.DISCONNECTED) && !mapExistId.get(AlertType.DISCONNECTED).contains(m.getId())).collect(Collectors.toList());
			List<TerminalDisconnect> terminalDisconnectsAdd = terminalDisconnects.stream().filter(m -> mapExistId.containsKey(AlertType.TERMINAL_DISCONNECTED) && !mapExistId.get(AlertType.TERMINAL_DISCONNECTED).contains(m.getId())).collect(Collectors.toList());
			List<MoldCycleTime> moldCycleTimesAdd = moldCycleTimes.stream().filter(m ->
					mapExistId.containsKey(AlertType.CYCLE_TIME) && !mapExistId.get(AlertType.CYCLE_TIME).contains(m.getId())
					&&( alertPeriodMap.get(AlertType.CYCLE_TIME)==null || alertPeriodMap.get(AlertType.CYCLE_TIME).equals(m.getPeriodType()))
			).collect(Collectors.toList());
			List<MoldCorrective> moldCorrectiveMaintenancesAdd = moldCorrectiveMaintenances.stream().filter(m -> mapExistId.containsKey(AlertType.CORRECTIVE_MAINTENANCE) && !mapExistId.get(AlertType.CORRECTIVE_MAINTENANCE).contains(m.getId())).collect(Collectors.toList());
			List<MoldMisconfigure> moldMisconfiguresAdd = moldMisconfigures.stream().filter(m -> mapExistId.containsKey(AlertType.MISCONFIGURE) && !mapExistId.get(AlertType.MISCONFIGURE).contains(m.getId())).collect(Collectors.toList());
//			List<MoldEfficiency> moldEfficienciesAdd = moldEfficiencies.stream().filter(m -> mapExistId.containsKey(AlertType.EFFICIENCY) && !mapExistId.get(AlertType.EFFICIENCY).contains(m.getId())).collect(Collectors.toList());
			List<MoldEfficiency> moldEfficienciesAdd = moldEfficiencies.stream().filter(m ->
					mapExistId.containsKey(AlertType.EFFICIENCY) && !mapExistId.get(AlertType.EFFICIENCY).contains(m.getId())
							&& (alertPeriodMap.get(AlertType.EFFICIENCY)==null || alertPeriodMap.get(AlertType.EFFICIENCY).equals(m.getPeriodType()))
			).collect(Collectors.toList());

			List<MoldDataSubmission> moldDataSubmissionsAdd = moldDataSubmissions.stream().filter(m -> mapExistId.containsKey(AlertType.DATA_SUBMISSION) && !mapExistId.get(AlertType.DATA_SUBMISSION).contains(m.getId())).collect(Collectors.toList());
			List<MoldRefurbishment> moldEndLifeCycleHistoriesAdd = moldEndLifeCycleHistories.stream().filter(m -> mapExistId.containsKey(AlertType.REFURBISHMENT) && !mapExistId.get(AlertType.REFURBISHMENT).contains(m.getId())).collect(Collectors.toList());
			List<MoldDetachment> moldDetachmentListAdd = moldDetachmentList.stream().filter(m -> mapExistId.containsKey(AlertType.DETACHMENT) && !mapExistId.get(AlertType.DETACHMENT).contains(m.getId())).collect(Collectors.toList());

			List<LogUserAlert> logUserAlerts = moldService.generateLogUserAlert(userAlertMap, moldLocationsAdd, moldDisconnectsAdd,
					terminalDisconnectsAdd, moldCycleTimesAdd, moldMaintenancesAdd, moldMisconfiguresAdd, moldEfficienciesAdd, moldDataSubmissionsAdd, moldCorrectiveMaintenancesAdd);

			if (moldEndLifeCycleHistoriesAdd != null && !moldEndLifeCycleHistoriesAdd.isEmpty()) {
				List<LogUserAlert> logUserAlerts1 = moldService.generateLogUserAlert(userAlertMap, moldEndLifeCycleHistoriesAdd,false);
				logUserAlerts.addAll(logUserAlerts1);
			}
			if (moldDetachmentListAdd != null && !moldDetachmentListAdd.isEmpty()) {
				List<LogUserAlert> logUserAlerts1 = moldService.generateLogUserAlertDetachment(userAlertMap, moldDetachmentListAdd,false);
				logUserAlerts.addAll(logUserAlerts1);
			}
			log.info("user: " + user.getEmail() +
					" due: " + (Instant.now().toEpochMilli() - st1.toEpochMilli()) / 1000 + "s" +
//					" mum exist: "+listAlertIdExist.size() +
					"\n num moldMaintenancesAdd: " + moldMaintenancesAdd.size() +
					"\n num moldLocationsAdd: " + moldLocationsAdd.size() +
					"\n num moldDisconnectsAdd: " + moldDisconnectsAdd.size() +
					"\n num terminalDisconnectsAdd: " + terminalDisconnectsAdd.size() +
					"\n num moldCycleTimesAdd: " + moldCycleTimesAdd.size() +
					"\n num moldCorrectiveMaintenancesAdd: " + moldCorrectiveMaintenancesAdd.size() +
					"\n num moldMisconfiguresAdd: " + moldMisconfiguresAdd.size() +
					"\n num moldEfficienciesAdd: " + moldEfficienciesAdd.size() +
					"\n num moldDataSubmissionsAdd: " + moldDataSubmissionsAdd.size() +
					"\n num moldEndLifeCycleHistoriesAdd: " + moldEndLifeCycleHistoriesAdd.size() +
					"\n num moldDetachmentListAdd: " + moldDetachmentListAdd.size() +
					"\n num add new: " + logUserAlerts.size());
			logUserAlertRepository.saveAll(logUserAlerts);
			total += logUserAlerts.size();
		}

		log.info("End restoreMissingAlerts total add: " + total + " due: " + (Instant.now().toEpochMilli() - start.toEpochMilli()) / 1000 + "s");

	}

	public Map<AlertType, List<Long>> mapListAlertIdsExist(Map<AlertType, Object> alertTypeObjectMap, User user) {
		Map<AlertType, List<Long>> map = new HashMap<>();
		alertTypeObjectMap.keySet().stream().forEach(key -> {
			map.put(key, listAlertIdsExist(alertTypeObjectMap, key, user));
		});
		return map;
	}

	public List<Long> listAlertIdsExist(Map<AlertType, Object> alertTypeObjectMap, AlertType alertType, User user) {
		List<Long> allAlertIdList = new ArrayList<>();
		List<Object> alertList = (List<Object>) alertTypeObjectMap.get(alertType);
		if (alertList != null)
			for (Object o : alertList) {
				try {
					Method field = o.getClass().getDeclaredMethod("getId");
					Long value = (Long) field.invoke(o);
					if (value != null) allAlertIdList.add(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		List<LogUserAlert> logUserAlertMaintenancesOld = logUserAlertRepository.findByUserIdAndAlertTypeAndAlertIdIn(user.getId(), alertType, allAlertIdList);
		List<Long> listMaintenanceAlertIdExist = logUserAlertMaintenancesOld.stream().map(l -> l.getAlertId()).collect(Collectors.toList());
		return listMaintenanceAlertIdExist;
	}

	public void fixDefaultPeriodType() {
		log.info("Start fixDefaultPeriodType");
		List<UserAlert> userAlertList = userAlertRepository.findAll();
		int num = 0;
		for (UserAlert userAlert : userAlertList) {
			if (userAlert.getPeriodType() == null) {
				if (AlertType.DISCONNECTED.equals(userAlert.getPeriodType())) {
					userAlert.setPeriodType(PeriodType.REAL_TIME);
				} else if (AlertType.CORRECTIVE_MAINTENANCE.equals(userAlert.getPeriodType())) {
					userAlert.setPeriodType(PeriodType.REAL_TIME);
				} else if (AlertType.MISCONFIGURE.equals(userAlert.getPeriodType())) {
					userAlert.setPeriodType(PeriodType.REAL_TIME);
				} else if (AlertType.DATA_SUBMISSION.equals(userAlert.getPeriodType())) {
					userAlert.setPeriodType(PeriodType.REAL_TIME);
				} else if (AlertType.REFURBISHMENT.equals(userAlert.getPeriodType())) {
					userAlert.setPeriodType(PeriodType.REAL_TIME);
				} else {
					userAlert.setPeriodType(PeriodType.DAILY);
				}
				userAlertRepository.save(userAlert);
				num++;
			}
		}
		//init default for user
		List<User> userList = userRepository.findAll();
		userList.stream().forEach(user -> updateDefaultAlertConfig(user));

		log.info("End fixDefaultPeriodType " + num);

	}

	public void fixDefaultConfigAlert(){
		log.info("Start fixDefaultConfigAlert");
		List<UserAlert> userAlertList = userAlertRepository.findAll();

		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		userAlertList.forEach(userAlert -> {
			if (userAlert.getAlertOn() == null){
				userAlert.setAlertOn(true);
				a.getAndIncrement();
			}
			if (userAlert.getAlertType().equals(AlertType.MAINTENANCE) && userAlert.getSpecialAlertType() == null){
				userAlert.setSpecialAlertType(SpecialAlertType.UPCOMING_OVERDUE);
				b.getAndIncrement();
			} else if ((userAlert.getAlertType().equals(AlertType.EFFICIENCY) || userAlert.getAlertType().equals(AlertType.CYCLE_TIME)) && userAlert.getSpecialAlertType() == null){
				userAlert.setSpecialAlertType(SpecialAlertType.L1L2);
				b.getAndIncrement();
			}else if ((userAlert.getAlertType().equals(AlertType.REFURBISHMENT) && userAlert.getSpecialAlertType() == null)){
				userAlert.setSpecialAlertType(SpecialAlertType.MEDIUM_HIGH);
				b.getAndIncrement();
			}
		});

		userAlertRepository.saveAll(userAlertList);
		log.info("End fixDefaultConfigAlert: "
				+ "\nmissing alert on: " + a
				+ "\nmissing alert type: " + b);
	}

	public List<Menu> getMyMenus(){
		List<Long> roleIds = SecurityUtils.getRoleIds();
		if(roleIds == null || roleIds.size() == 0) return new ArrayList<>();
		List<Menu> menus = roleService.getMenuByRoleIdIn(roleIds);
		return menus;
	}


	@Transactional
	public void increaseFailedAttempts(User user) {
		int newFailAttempts = user.getFailedAttempt() + 1;
		userRepository.updateFailedAttempts(newFailAttempts, user.getEmail());
	}

	@Transactional
	public void resetFailedAttempts(String email) {
		userRepository.updateFailedAttempts(0, email);
	}

	public void lock(User user) {
		user.setAccountLocked(true);
		user.setLockTime(Instant.now());

		userRepository.save(user);
	}

	public boolean sendPwdExpireReminder(User user){

		String token = UUID.randomUUID().toString();
		PasswordResetToken pwToken = new PasswordResetToken();
		pwToken.setToken(token);
		pwToken.setEmail(user.getEmail());
		pwToken.setCreatedAt(Instant.now());
		pwTokenService.save(pwToken);

		boolean emailStatus = mailService.sendPwdExpireReminderMail(user, token);
		if(!emailStatus){
			log.error("sendLockAccountMail Fail");
		}
		return emailStatus;
	}

	public boolean unlockWhenTimeExpired(User user) {
		long lockTimeInMillis = user.getLockTime().toEpochMilli();
		long currentTimeInMillis = System.currentTimeMillis();

		if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
			user.setAccountLocked(false);
			user.setLockTime(null);
			user.setFailedAttempt(0);
			userRepository.save(user);
			return true;
		}

		return false;
	}
	public boolean sendLockAccountMail(User user){

		String token = UUID.randomUUID().toString();
		PasswordResetToken pwToken = new PasswordResetToken();
		pwToken.setToken(token);
		pwToken.setEmail(user.getEmail());
		pwToken.setCreatedAt(Instant.now());
		pwTokenService.save(pwToken);

		boolean emailStatus = mailService.sendLockAccountMail(user, token);
		if(!emailStatus){
			log.error("sendLockAccountMail Fail");
		}
		return emailStatus;
	}

	public boolean sendPwdExpiredMail(User user){

		String token = UUID.randomUUID().toString();
		PasswordResetToken pwToken = new PasswordResetToken();
		pwToken.setToken(token);
		pwToken.setEmail(user.getEmail());
		pwToken.setCreatedAt(Instant.now());
		pwTokenService.save(pwToken);

		boolean emailStatus = mailService.sendPwdExpiredMail(user, token);
		if(!emailStatus){
			log.error("sendLockAccountMail Fail");
		}
		return emailStatus;
	}

	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto){
		try
		{
			List<User> users = userRepository.findByIdInAndDeletedIsFalse(dto.getIds());
			users.forEach(user -> {
				user.setEnabled(dto.isEnabled());
				user.setRequested(false);
				User finalUser = save(user);
				versioningService.writeHistory(finalUser);
			});
			return ApiResponse.success(CommonMessage.OK, users);
		}
		catch (Exception e){
			return ApiResponse.error(e.getMessage());
		}
	}

	public ByteArrayOutputStream exportLoginAuditTrail(List<Long> ids, Pageable pageable) {
		List<User> userList = null;
		if (ids == null || ids.size() == 0) {
			userList = userRepository.findAllByOrderByIdDesc();
		} else {
			if (pageable != null && pageable.getSort() != null) {
				Pageable pageableNew = Pageable.unpaged();
				if (pageable.getSort() != null) {
					pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
				}
				UserPayload payload = new UserPayload();
				payload.setIds(ids);

				Page<User> pageContent = new PageImpl<>(userRepository.findAllById(ids), pageableNew, ids.size());
				userList = pageContent.getContent();
			} else
				userList = userRepository.findByIdInOrderByIdDesc(ids);
		}

		return excelUtils.exportExcelLoginAuditTrail(userList);
	}
	public void valid(UserPayload item, Long id) {
		ValueUtils.assertNotEmpty(item.getName(), "full_name");
		ValueUtils.assertNotEmpty(item.getEmail(), "email_address");
		if(!EmailValidator.getInstance().isValid(item.getEmail())){
			throw DataUtils.newDataValueInvalidException(User.class, "email_address", item.getEmail());
		}

		if (id == null) {
			boolean exists = existByLoginIdOrEmail(item.getEmail(), item.getEmail());
			if (exists) {
				throw new BizException("email_address_is_already_registered", User.class.getSimpleName());
			}
		}

		// Registering User sometimes doesn't know the Registered Company Information
		// ValueUtils.assertNotEmpty(item.getCompanyId(), "company");
		if(item.isValidImportCheck()) ValueUtils.assertNotEmpty(item.getCompanyId(), "company");

		ValueUtils.assertNotEmpty(item.getDepartment(), "department");
		ValueUtils.assertNotEmpty(item.getPosition(), "position");
		if(!StringUtils.isEmpty(item.getMobileDialingCode())){
			String dialingCode = item.getMobileDialingCode().trim().replace("+","");
			Optional<CountryCode> optionalCountryCode = Arrays.stream(CountryCode.values()).filter(c -> c.getDescription().equals(dialingCode)).findFirst();
			if(!optionalCountryCode.isPresent()){
				throw DataUtils.newDataValueInvalidException(User.class, "mobile_dialing_code", item.getMobileDialingCode());
			}
		}
		if(!StringUtils.isEmpty(item.getMobileNumber()) && !saleson.common.util.DataUtils.isPhoneNumberFull(item.getMobileNumber())){
			throw DataUtils.newDataValueInvalidException("User", "mobile_number", item.getMobileNumber());
		}
		if(!StringUtils.isEmpty(item.getContactDialingCode())){
			String dialingCode = item.getContactDialingCode().trim().replace("+","");
			Optional<CountryCode> optionalCountryCode = Arrays.stream(CountryCode.values()).filter(c -> c.getDescription().equals(dialingCode)).findFirst();
			if (!optionalCountryCode.isPresent()) {
				throw DataUtils.newDataValueInvalidException(User.class, "contact_dialing_code", item.getContactDialingCode());
			}
		}
		if (!StringUtils.isEmpty(item.getContactNumber()) && !saleson.common.util.DataUtils.isPhoneNumberFull(item.getContactNumber())) {
			throw DataUtils.newDataValueInvalidException("User", "contact_number", item.getContactNumber());
		}


	}

	public void changeTabPayload(UserParam payLoad) {
		payLoad.setIsDefaultTab(true);
		if (payLoad.getTabId() != null) {
			Optional<TabTable> tabTableOptional = tabTableRepository.findById(payLoad.getTabId());
			if (tabTableOptional.isPresent() && (tabTableOptional.get().getIsDefaultTab() == null || !tabTableOptional.get().getIsDefaultTab())) {
				List<TabTableData> tabTableDataList = tabTableDataRepository.findAllByTabTableId(payLoad.getTabId());
				List<Long> userIds = tabTableDataList.stream().map(TabTableData::getRefId).collect(Collectors.toList());
				payLoad.setUserIds(userIds);
				payLoad.setIsDefaultTab(tabTableOptional.get().getIsDefaultTab());
			}
		}
	}

}
