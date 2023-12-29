package com.emoldino.api.common.resource.base.accesscontrol.service.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleGetIn;
import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleGetUsersIn;
import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleSaveUsersIn;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.PermissionRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.QPermission;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.QRoleControl;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControl;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControlRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.QRoleUser;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.RoleUser;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.RoleUserRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.userlite.UserLite;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.location.LocationRepository;
import saleson.api.role.RoleGraphRepository;
import saleson.api.role.RoleRepository;
import saleson.api.user.UserRepository;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.RoleUserData;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;
import saleson.model.Location;
import saleson.model.QCompany;
import saleson.model.QLocation;
import saleson.model.QRole;
import saleson.model.QUser;
import saleson.model.Role;
import saleson.model.User;

@Service
@Transactional
public class RoleAccessControlService {

	@Autowired
	private RoleControlRepository repo;
	@Autowired
	private RoleUserRepository userRepo;
	@Autowired
	private PermissionRepository permRepo;

	private static final RoleControl ROLE_CONTROL = new RoleControl();

	public Page<RoleControl> getPage(RoleGetIn input, Pageable pageable) {
		initRoles();
		initUsers();

		// Make Filter
		BooleanBuilder filter = new BooleanBuilder();
		{
			QRoleControl table = QRoleControl.roleControl;
			filter.and(table.roleType.eq(ROLE_CONTROL.getRoleType()));
			if (input.getEnabled() != null) {
				filter.and(table.enabled.eq(input.getEnabled()));
			}

			if (input.getCompanyId() != null) {
				Company company = BeanUtils.get(CompanyService.class).findById(input.getCompanyId());
				if (company == null) {
					return new PageImpl<>(Collections.emptyList(), pageable, 0);
				}
				if (company.isEmoldino()) {
					filter.and(table.emoldinoEnabled.isTrue());
				} else if (CompanyType.IN_HOUSE.equals(company.getCompanyType())) {
					filter.and(table.oemEnabled.isTrue());
				} else if (CompanyType.SUPPLIER.equals(company.getCompanyType())) {
					filter.and(table.supplierEnabled.isTrue());
				} else {
					filter.and(table.toolmakerEnabled.isTrue());
				}
			} else {
				if (SecurityUtils.isEmoldino()) {
					filter.and(table.emoldinoEnabled.isTrue());
				} else if (SecurityUtils.isOem()) {
					filter.and(//
							table.oemEnabled.isTrue()//
									.or(table.supplierEnabled.isTrue())//
									.or(table.toolmakerEnabled.isTrue())//
					);
				} else if (SecurityUtils.isSupplier()) {
					filter.and(table.supplierEnabled.isTrue());
				} else {
					filter.and(table.toolmakerEnabled.isTrue());
				}
			}

			if (!ObjectUtils.isEmpty(input.getQuery())) {
				String searchWord = input.getQuery();
				filter.and(//
						table.name.containsIgnoreCase(searchWord)//
								.or(table.description.containsIgnoreCase(searchWord))//
				);
			}
		}

		// Adjust Pageable
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.ASC, "id");
		}

		Page<RoleControl> page = repo.findAll(filter, pageable);

		return page;
	}

	public RoleControl get(Long id) {
		LogicUtils.assertNotNull(id, "id");
		return repo.findById(id).orElse(null);
	}

	public void post(RoleControl data) {
		LogicUtils.assertNotNull(data, "data");
		LogicUtils.assertNotEmpty(data.getName(), "name");

		if (data.getId() != null && repo.existsById(data.getId())) {
			new BizException("DATA_ALREADY_EXIST", "Role id:" + data.getId() + " already exists!!", new Property("model", "Role"), new Property("id", data.getId()));
		} else if (repo.exists(new BooleanBuilder().and(QRoleControl.roleControl.name.eq(data.getName())))) {
			new BizException("DATA_ALREADY_EXIST", "Role name:" + data.getName() + " already exists!!", new Property("model", "Role"), new Property("name", data.getName()));
		}

		data.setEnabled(true);

		repo.save(data);
	}

	public void put(Long id, RoleControl data) {
		LogicUtils.assertNotNull(id, "id");
		LogicUtils.assertNotEmpty(data.getName(), "name");

		if (!repo.existsById(id)) {
			DataUtils.newDataNotFoundException(RoleControl.class, "id", id);
		}

		QRoleControl table = QRoleControl.roleControl;
		if (repo.exists(new BooleanBuilder().and(table.id.ne(id)).and(table.name.eq(data.getName())))) {
			new BizException("DATA_ALREADY_EXIST", "Role name:" + data.getName() + " is already exist!!", new Property("model", "Role"), new Property("name", data.getName()));
		}

		data.setId(id);
		repo.save(data);
	}

	public ListOut<UserLite> getUsers(Long id, RoleGetUsersIn input) {
		// 1. Validation
		LogicUtils.assertNotNull(input.getLocationId(), "locationId");

		// 2. Retrieve
		// Consider the Same Name Users
		Map<String, List<UserLite>> map = new TreeMap<>();
		findRoleUsers(id, input.getLocationId(), input.getQuery()).forEach(item -> {
			UserLite user = item.getUser();
			if (ObjectUtils.isEmpty(user.getName())) {
				return;
			}
			List<UserLite> list;
			String key = user.getName().toUpperCase();
			if (map.containsKey(key)) {
				list = map.get(key);
			} else {
				list = new ArrayList<>();
				map.put(key, list);
			}
			list.add(user);
		});

		// 3. Response
		List<UserLite> content = new ArrayList<>();
		map.values().forEach(list -> list.forEach(item -> content.add(item)));
		return new ListOut<>(content);
	}

	public Page<UserLite> getUsersAvaliable(Long id, RoleGetUsersIn input, Pageable pageable) {
		// 1. Validation
		LogicUtils.assertNotNull(input.getLocationId(), "locationId");
		Location location = BeanUtils.get(LocationRepository.class).findById(input.getLocationId())
				.orElseThrow(() -> new BizException("DATA_NOT_FOUND", "Location id:" + id + " is not found!!", new Property("model", "Location"), new Property("id", id)));
		Long companyId = location.getCompanyId();
		if (companyId == null || companyId == 0L) {
			return Page.empty(pageable);
		}
		// Adjust Pageable
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.ASC, "name");
		}

		// 2. Get All RoleUsers;
		List<Long> userIds = new ArrayList<>();
		findRoleUsers(id, input.getLocationId(), null).forEach(item -> userIds.add(item.getUserId()));

		// 3. Find All Users in the Company not in RoleUsers
		Page<UserLite> users;
		{
			QUser table = QUser.user;
			BooleanBuilder filter = new BooleanBuilder()//
					.and(table.company.id.eq(companyId))//
					.and(table.id.notIn(userIds));
			users = BeanUtils.get(UserRepository.class).findAll(filter, pageable)//
					.map(user -> new UserLite(user.getId(), user.getName(), user.getLoginId()));
		}

		return users;

//		//Page<UserLite> result = BeanUtils.get(UserRepository.class).findByCompanyIdAndQueryAndAvailble(location.getCompanyId(), input.getQuery(), userIds, pageable);
//		Page<UserLite> result = BeanUtils.get(UserRepository.class).findByCompanyIdAndQueryAndAvailble2(location.getCompanyId(), input.getQuery(), userIds, pageable);
//		return result;
	}

	public void saveUsers(Long id, RoleSaveUsersIn input) {
		LogicUtils.assertNotNull(input.getLocationId(), "locationId");
		Location location = BeanUtils.get(LocationRepository.class).findById(input.getLocationId())
				.orElseThrow(() -> new BizException("DATA_NOT_FOUND", "Location id:" + id + " is not found!!", new Property("model", "Location"), new Property("id", id)));
		Long companyId = location.getCompanyId();
		if (companyId == null || companyId == 0L) {
			return;
		}

		List<UserLite> list = input.getContent();
		List<RoleUser> oldList = findRoleUsers(id, input.getLocationId(), null);

		if (ObjectUtils.isEmpty(list)) {
			if (!oldList.isEmpty()) {
				userRepo.deleteAll(oldList);
			}
			return;
		}

		List<RoleUser> added = new ArrayList<>();
		Map<Long, RoleUser> oldItems = ObjectUtils.isEmpty(oldList) ? Collections.emptyMap()
				: oldList.stream().collect(Collectors.toMap(item -> item.getUser().getId(), item -> item));
		list.forEach(item -> {
			LogicUtils.assertNotNull(item.getId(), "item.id");

			if (item.getId() == null || !oldItems.containsKey(item.getId())) {
				RoleUser user = new RoleUser();
				user.setRoleId(id);
				user.setLocationId(input.getLocationId());
				user.setUserId(item.getId());
				added.add(user);
			} else {
				oldItems.remove(item.getId());
			}
		});

		userRepo.deleteAll(oldItems.values());
		userRepo.saveAll(added);
	}

	private List<RoleUser> findRoleUsers(Long id, Long locationId, String searchWord) {
		LogicUtils.assertNotNull(id, "id");
		LogicUtils.assertNotNull(locationId, "locationId");

		QRoleUser table = QRoleUser.roleUser;
		BooleanBuilder filter = new BooleanBuilder()//
				.and(table.roleId.eq(id))//
				.and(table.locationId.eq(locationId));
		if (!ObjectUtils.isEmpty(searchWord)) {
			filter.and(table.user.name.contains(searchWord));
		}

		List<RoleUser> list = new ArrayList<>();
		userRepo.findAll(filter).forEach(item -> list.add(item));
		return list;
	}

	public void disableList(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}
		for (Long id : ids) {
			RoleControl data = findById(id);
			data.setEnabled(false);
			repo.save(data);
		}
	}

	public void enableList(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}
		for (Long id : ids) {
			RoleControl data = findById(id);
			data.setEnabled(true);
			repo.save(data);
		}
	}

	private RoleControl findById(Long id) {
		return repo.findById(id).orElseThrow(() -> new BizException("DATA_NOT_FOUND", "Role id:" + id + " is not found!!", new Property("model", "Role"), new Property("id", id)));
	}

	private void initUsers() {
		if (!"developer".equals(ConfigUtils.getProfile())) {
			return;
		}

		initUser("steve.esc@emoldino.com", CompanyType.IN_HOUSE, true, false, "SC_MANAGER");
		initUser("steve.easset@emoldino.com", CompanyType.IN_HOUSE, true, false, "ASSET_MANAGER");
		initUser("steve.eprod@emoldino.com", CompanyType.IN_HOUSE, true, false, "PROD_MANAGER");
		initUser("steve.oem@emoldino.com", CompanyType.IN_HOUSE, false, true);
		initUser("steve.osc@emoldino.com", CompanyType.IN_HOUSE, false, false, "SC_MANAGER");
		initUser("steve.oasset@emoldino.com", CompanyType.IN_HOUSE, false, false, "ASSET_MANAGER");
		initUser("steve.oprod@emoldino.com", CompanyType.IN_HOUSE, false, false, "PROD_MANAGER");
		initUser("steve.supplier@emoldino.com", CompanyType.SUPPLIER, false, true);
		initUser("steve.sasset@emoldino.com", CompanyType.SUPPLIER, false, false, "ASSET_MANAGER");
		initUser("steve.sprod@emoldino.com", CompanyType.SUPPLIER, false, false, "PROD_MANAGER");
		initUser("steve.toolmake@emoldino.com", CompanyType.TOOL_MAKER, false, true);
		initUser("steve.tasset@emoldino.com", CompanyType.TOOL_MAKER, false, false, "ASSET_MANAGER");
	}

	private void initUser(String loginId, CompanyType companyType, boolean emoldino, boolean admin, String... authorities) {
		// 1. Validation
		String name = "Steve " + companyType.getTitle() + (admin ? " A" : " B");
		String password = "$2a$10$Urc5WIHERM/.fQ65ykFvneh2U50oJDb1eR8qXTl.uQa2BHqlGcY0K";

		boolean exists = false;
		{
			QUser table = QUser.user;
			BooleanBuilder filter = new BooleanBuilder()//
					.and(table.name.eq(name))//
					.and(table.loginId.eq(loginId))//
					.and(table.password.eq(password))//
					.and(table.admin.eq(admin));
			exists = BeanUtils.get(UserRepository.class).exists(filter);
		}

		Company company = null;
		Location location = null;
		{
			Page<Company> page;
			{
				QCompany table = QCompany.company;
				BooleanBuilder filter = new BooleanBuilder()//
						.and(table.companyType.eq(companyType))//
						.and(table.enabled.isTrue())//
						.and(emoldino ? table.name.equalsIgnoreCase("emoldino") : table.name.notEqualsIgnoreCase("emoldino"));
				page = BeanUtils.get(CompanyRepository.class).findAll(filter, PageRequest.of(0, 20, Sort.by("id")));
			}
			if (page.isEmpty()) {
				return;
			}

			for (Company comp : page.getContent()) {
				QLocation table = QLocation.location;
				List<Location> locs = BeanUtils.get(LocationRepository.class)
						.findAll(new BooleanBuilder().and(table.companyId.eq(comp.getId())), PageRequest.of(0, 1, Sort.by("id"))).getContent();
				for (Location loc : locs) {
					location = loc;
					break;
				}
				if (location != null) {
					company = comp;
				}
			}
		}
		if (location == null) {
			return;
		}

		// 2. Save User
		User user = BeanUtils.get(UserRepository.class).findByLoginIdAndDeletedIsFalse(loginId).orElse(null);
		if (!exists) {
			if (user == null) {
				user = new User();
			}
			if (user.getCompany() == null) {
				user.setCompany(company);
			}
			user.setName(name);
			user.setLoginId(loginId);
			user.setEmail(loginId);
			user.setPassword(password);
			user.setDepartment("Solution Team");
			user.setPosition("Team Leader");
			user.setContactDialingCode("1");
			user.setContactNumber("");
			user.setMobileDialingCode("82");
			user.setMobileNumber("010-3297-1524");
			user.setMemo("");
			user.setLanguage("EN");
			user.setEnabled(true);
			user.setAdmin(admin);
			user.setAccountLocked(false);
			user.setNotify(false);
			user.setAccessRequest(false);
			user.setRequested(false);
			user.setDisableNotificationSystemNode(false);
			user.setRoleUserData(RoleUserData.ROLE_WEB_USER);
			user.setDeleted(false);

			if (admin) {
				boolean required = true;
				if (!ObjectUtils.isEmpty(user.getRoles())) {
					for (Role role : user.getRoles()) {
						if ("ROLE_ADMIN".equals(role.getAuthority())) {
							required = false;
							break;
						}
					}
				}
				if (required) {
					Role role = BeanUtils.get(RoleRepository.class).findOne(new BooleanBuilder().and(QRole.role.authority.eq("ROLE_ADMIN"))).orElse(null);
					if (role != null) {
						if (user.getRoles() == null) {
							user.setRoles(new HashSet<>());
						}
						user.getRoles().add(role);
					}
				}
			} else if (!ObjectUtils.isEmpty(user.getRoles())) {
				user.setRoles(Collections.emptySet());
			}

			BeanUtils.get(UserRepository.class).save(user);
		}

		// 3. Authorities
		if (user != null && !ObjectUtils.isEmpty(authorities)) {
			if (BeanUtils.get(RoleUserRepository.class).exists(QRoleUser.roleUser.userId.eq(user.getId()))) {
				return;
			}
			for (String auth : authorities) {
				RoleControl role = BeanUtils.get(RoleControlRepository.class).findOne(new BooleanBuilder().and(QRoleControl.roleControl.authority.eq(auth))).orElse(null);
				if (role == null) {
					continue;
				}
				RoleUser roleUser = new RoleUser();
				roleUser.setRoleId(role.getId());
				roleUser.setLocationId(location.getId());
				roleUser.setUserId(user.getId());
				BeanUtils.get(RoleUserRepository.class).save(roleUser);
			}
		}

	}

	public void initRoles() {
		List<Long> ids = new ArrayList<>();
//		initRole(110, "System Admin", "SYS_ADMIN", ids);
//		initRole(120, "Cunsultant", "CONSULTANT", ids);
//		initRole(ids, 200, "Supply Chain Manager", "SC_MANAGER", true, true, false, false);
//		initRole(ids, 300, "Asset Manager", "ASSET_MANAGER", true, true, true, true);
//		initRole(ids, 400, "Production Manager", "PROD_MANAGER", true, true, true, false);
		initRole(ids, 800, "OEM Regular", "OEM_REGULAR", true, true, false, false);
		initRole(ids, 810, "Supplier Regular", "SUPPLIER_REGULAR", true, false, true, false);
		initRole(ids, 811, "Supplier Installation", "SUPPLIER_INSTALLATION", true, false, true, false);
		initRole(ids, 820, "Toolmaker Regular", "TOOLMAKER_REGULAR", true, false, false, true);
		initRole(ids, 900, "Role A", "ROLE_A", true, true, true, true);
		initRole(ids, 901, "Role B", "ROLE_B", true, true, true, true);
		initRole(ids, 902, "Role C", "ROLE_C", true, true, true, true);
		initRole(ids, 903, "Role D", "ROLE_D", true, true, true, true);
		initRole(ids, 904, "Role E", "ROLE_E", true, true, true, true);
		initRole(ids, 905, "Role F", "ROLE_F", true, true, true, true);

		QRoleControl table = QRoleControl.roleControl;
		BeanUtils.get(RoleControlRepository.class).findAll(//
				new BooleanBuilder()//
						.and(table.roleType.eq(ROLE_CONTROL.getRoleType()))//
						.and(table.id.notIn(ids))//
		).forEach(role -> {
			permRepo.deleteAll(permRepo.findAll(new BooleanBuilder().and(QPermission.permission.role.id.eq(role.getId()))));
			userRepo.deleteAll(userRepo.findAll(new BooleanBuilder().and(QRoleUser.roleUser.roleId.eq(role.getId()))));
			repo.delete(role);
		});

		ids.add(1L);

		TranUtils.doNewTran(() -> BeanUtils.get(RoleGraphRepository.class).deleteAll());

		BeanUtils.get(RoleRepository.class).findAll(new BooleanBuilder().and(QRole.role.id.notIn(ids))).forEach(role -> {
			try {
				TranUtils.doNewTran(() -> {
					BeanUtils.get(UserRepository.class).findAll(new BooleanBuilder().and(QUser.user.roles.contains(role))).forEach(user -> {
						Set<Role> roles = user.getRoles();
						Role r = null;
						for (Role item : roles) {
							if (item.getId().equals(role.getId())) {
								r = item;
								break;
							}
						}
						if (r != null) {
							roles.remove(r);
							user.setRoles(roles);
							BeanUtils.get(UserRepository.class).save(user);
						}
					});
				});
				role.setMenus(new HashSet<>());
				TranUtils.doNewTran(() -> BeanUtils.get(RoleRepository.class).save(role));
				TranUtils.doNewTran(() -> BeanUtils.get(RoleRepository.class).delete(role));
			} catch (Exception e) {
				return;
			}
		});

	}

	private static void initRole(List<Long> ids, Integer idInt, String name, String authority, Boolean emoldinoEnabled, Boolean oemEnabled, Boolean supplierEnabled,
			Boolean toolmakerEnabled) {
		Long id = ValueUtils.toLong(idInt, null);
		ids.add(id);

		QRoleControl table = QRoleControl.roleControl;
		BooleanBuilder filter = new BooleanBuilder()//
				.and(table.id.eq(id))//
				.and(table.name.eq(name))//
				.and(table.authority.eq(authority))//
				.and(table.emoldinoEnabled.eq(emoldinoEnabled))//
				.and(table.oemEnabled.eq(oemEnabled))//
				.and(table.supplierEnabled.eq(supplierEnabled))//
				.and(table.toolmakerEnabled.eq(toolmakerEnabled))//
				.and(table.roleType.eq(ROLE_CONTROL.getRoleType()));
		if (BeanUtils.get(RoleControlRepository.class).exists(filter)) {
			return;
		}

		RoleControl role = BeanUtils.get(RoleControlRepository.class).findById(id).orElse(null);
		if (role == null) {
			role = new RoleControl();
		}
		role.setId(id);
		role.setName(name);
		role.setAuthority(authority);
		role.setEnabled(true);
		role.setEmoldinoEnabled(emoldinoEnabled);
		role.setOemEnabled(oemEnabled);
		role.setSupplierEnabled(supplierEnabled);
		role.setToolmakerEnabled(toolmakerEnabled);
		BeanUtils.get(RoleControlRepository.class).save(role);
	}

	public void initRoleUserByUserId(Long userId) {
		if (userId == null) {
			return;
		}
		User user = BeanUtils.get(UserRepository.class).findById(userId).orElse(null);
		if (user == null) {
			return;
		}

		SyncCtrlUtils.wrapWithLock("initUserRole." + userId, () -> {
			QRoleUser table = QRoleUser.roleUser;
			RoleUserRepository repo = BeanUtils.get(RoleUserRepository.class);

			Long companyId = user.getCompanyId();
			if (companyId == null) {
				repo.deleteAll(repo.findAll(new BooleanBuilder().and(table.userId.eq(userId))));
				return;
			}

			List<Long> locationIds = BeanUtils.get(LocationRepository.class).findAllIdByPredicate(new BooleanBuilder().and(QLocation.location.companyId.eq(companyId)));
			if (ObjectUtils.isEmpty(locationIds)) {
				repo.deleteAll(repo.findAll(new BooleanBuilder().and(table.userId.eq(userId))));
				return;
			}

			// Check RoleUser
			{

				repo.deleteAll(repo.findAll(new BooleanBuilder()//
						.and(table.userId.eq(userId))//
						.and(table.locationId.notIn(locationIds))//
				));

				if (repo.exists(new BooleanBuilder()//
						.and(table.userId.eq(userId))//
						.and(table.locationId.in(locationIds))//
				)) {
					return;
				}
			}

			Long roleId;
			{
				String roleCode;
				if (SecurityUtils.isEmoldino() || SecurityUtils.isOem()) {
					roleCode = "OEM_REGULAR";
				} else if (SecurityUtils.isSupplier()) {
					roleCode = "SUPPLIER_REGULAR";
				} else {
					roleCode = "TOOLMAKER_REGULAR";
				}
				Role role = BeanUtils.get(RoleRepository.class).findByAuthority(roleCode).orElse(null);
				if (role == null) {
					return;
				}
				roleId = role.getId();
			}

			List<RoleUser> roleUsers = new ArrayList<>();
			for (Long locationId : locationIds) {
				RoleUser roleUser = new RoleUser();
				roleUser.setRoleId(roleId);
				roleUser.setLocationId(locationId);
				roleUser.setUserId(userId);
				roleUsers.add(roleUser);
			}
			repo.saveAll(roleUsers);
		});
	}

}
