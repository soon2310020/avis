package saleson.api.role;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import saleson.api.menu.MenuRepository;
import saleson.api.mold.MoldAuthorityRepository;
import saleson.api.role.payload.RolePayload;
import saleson.api.user.UserRepository;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.GraphType;
import saleson.common.enumeration.RoleType;
import saleson.common.payload.ApiResponse;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Menu;
import saleson.model.Role;
import saleson.model.RoleGraph;
import saleson.model.User;

@Deprecated
@Service
public class RoleService {
	@Autowired
	RoleRepository roleRepository;

	@Autowired
	MenuRepository menuRepository;

	@Autowired
	MoldAuthorityRepository moldAuthorityRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleGraphService roleGraphService;

	@Transactional
	public Role save(Role role) {

		if (role.getId() != null) {

		}
		return roleRepository.save(role);
	}

	public Page<Role> findAll(Predicate predicate, Pageable pageable) {
		return roleRepository.findAll(predicate, pageable);
	}

	public Role findById(Long id) {

		return roleRepository.findById(id).orElseThrow(() -> new RuntimeException("조회된 데이터가 없음."));
	}

	public void deleteById(Long id) {
		Optional<Role> optional = roleRepository.findById(id);

		if (optional.isPresent()) {
			Role role = optional.get();

			// 회원에 부여되었는가?
			Long count = userRepository.countByRoleId(role.getId());

			// Mold 권한으로 설정되었는가?
			boolean exists = moldAuthorityRepository.existsByAuthority(role.getAuthority());

			if (exists || count > 0) {
				throw new RuntimeException("You can not delete it because it's in use.");
			} else {
				roleRepository.deleteById(id);
				roleGraphService.deleteByRoleId(id);
			}
		}
	}

	public List<Role> findRolesByRoleType(RoleType roleType) {
		return roleRepository.findRolesByRoleType(roleType);
	}

	public List<Role> findAllByRoleType(RoleType roleType) {
		return roleRepository.findAllByRoleType(roleType);
	}

	public List<GraphType> getDashboardByUser(User user) {
		if (user.isAdmin()) {
			return new ArrayList<>(EnumSet.allOf(GraphType.class));
		}
		List<Long> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(roleIds)) {
			List<Role> accessFeatures = roleRepository.findByIdIsInAndRoleType(roleIds, RoleType.ROLE_MENU);
			return roleGraphService.findGraphTypeByRoleIdIn(accessFeatures.stream().map(Role::getId).collect(Collectors.toList())).stream().filter(GraphType::isEnabled)
					.collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	public List<GraphType> getDashboardByRole_Old() {
		if (SecurityUtils.isAdmin()) {
			return new ArrayList<>(EnumSet.allOf(GraphType.class)).stream().filter(GraphType::isEnabled).collect(Collectors.toList());
		}
		List<Long> roleIds = SecurityUtils.getRoleIds();
		List<Role> accessFeatures = roleRepository.findByIdIsInAndRoleType(roleIds, RoleType.ROLE_MENU);
		return roleGraphService.findGraphTypeByRoleIdIn(accessFeatures.stream().map(Role::getId).collect(Collectors.toList())).stream().filter(GraphType::isEnabled)
				.collect(Collectors.toList());
	}

//	public List<GraphType> getDashboardByRole() {
//		if (SecurityUtils.isAdmin()) {
//			return new ArrayList<>(EnumSet.allOf(GraphType.class).stream().filter(e -> e.isEnabled()).collect(Collectors.toList()));
//		}
//
//		List<Long> roleIds = SecurityUtils.getRoleIds();
//		List<Role> roleList = roleRepository.findByIdIsInAndRoleType(roleIds, RoleType.ROLE_MENU);
//		List<DashboardGroupType> dashboardGroupTypeList = new ArrayList<>();
//		List<Long> roleIndividualIds = new ArrayList<>();
//		roleList.stream().forEach(role -> {
//			if (Arrays.asList(DashboardGroupType.ASSET_MANAGEMENT_DASHBOARD, DashboardGroupType.SUPPLIER_PERFORMANCE_MANAGEMENT_DASHBOARD).contains(role.getDashboardGroupType())) {
//				dashboardGroupTypeList.add(role.getDashboardGroupType());
//			} else {
//				roleIndividualIds.add(role.getId());
//			}
//
//		});
//
//		List<GraphType> graphTypeList = new ArrayList<>();
//		Set<GraphType> graphTypeSet = new HashSet<>();
//		List<DashboardGroup> dashboardGroupList = BeanUtils.get(DashboardGroupRepository.class).findAllByDashboardGroupTypeIn(dashboardGroupTypeList);
//		dashboardGroupList.stream().forEach(dashboardGroup -> {
//			if (dashboardGroup.getGraphType() != null)
//				graphTypeSet.add(dashboardGroup.getGraphType());
//		});
//		if (!roleIndividualIds.isEmpty()) {
//			List<GraphType> graphTypeIndividualList = roleGraphService.findGraphTypeByRoleIdIn(roleIds);
//			graphTypeSet.addAll(graphTypeIndividualList);
//		}
//		graphTypeList = graphTypeSet.stream().collect(Collectors.toList());
//		Comparator<GraphType> compareByName = Comparator.comparing(GraphType::getPosition);
//		Collections.sort(graphTypeList, compareByName);
//		return graphTypeList.stream().filter(e -> e.isEnabled()).collect(Collectors.toList());
////		return roleGraphService.findGraphTypeByRoleIdIn(roleIds);
//	}

	public List<Menu> getMenuAlertByRoleIdIn(List<Long> roleIds) {
		List<Role> roles = roleRepository.findByIdIsInAndRoleType(roleIds, RoleType.ROLE_MENU);
		List<Menu> menuList = new ArrayList<>();
		if (roles == null || roles.size() == 0)
			return menuList;
		roles.forEach(role -> {
			Set<Menu> menuSet = role.getMenus();
			List<Menu> menuAlert = menuSet.stream().filter(x -> x.getParentId() == 1200L && !menuList.contains(x) && x.isEnabled()).collect(Collectors.toList());
			if (menuAlert != null && menuAlert.size() > 0)
				menuList.addAll(menuAlert);
		});
		return menuList;
	}

	public List<Menu> getMenuByRoleIdIn(List<Long> roleIds) {
		if (SecurityUtils.isAdmin())
			return menuRepository.findAll();
		List<Role> roles = roleRepository.findByIdIsInAndRoleType(roleIds, RoleType.ROLE_MENU);
		List<Menu> menuList = new ArrayList<>();
		if (roles == null || roles.size() == 0)
			return menuList;
		roles.forEach(role -> {
			Set<Menu> menuSet = role.getMenus();
			List<Menu> menuAlert = menuSet.stream().filter(x -> !menuList.contains(x) && x.isEnabled()).collect(Collectors.toList());
			if (menuAlert != null && menuAlert.size() > 0)
				menuList.addAll(menuAlert);
		});
		return menuList;
	}

	public void updateOldData() {
		List<Role> roles = roleRepository.findAll();
		List<Long> roleIds = roles.stream().map(x -> x.getId()).collect(Collectors.toList());
		List<RoleGraph> newRoleGraphs = new ArrayList<>();
		roleIds.forEach(roleId -> {
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.QUICK_STATS));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.DISTRIBUTION));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.HIERARCHY));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.CAPACITY));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.CYCLE_TIME_STATUS));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.TOOLING));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.MAINTENANCE_STATUS));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.UPTIME_STATUS));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.COMMON_PARTS));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.DOWNTIME));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.OEE));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.TOOLING_COST));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.RECENT_ACTIVITIES));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.DEVICE_INSTALLATION));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.PRODUCTION_RATE));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.UTILIZATION_RATE));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.MAINTENANCE_EFFECTIVENESS));
//			newRoleGraphs.add(new RoleGraph(roleId, GraphType.SUPPORTING_DOCUMENT));
		});
		roleGraphService.save(newRoleGraphs);
	}

	public List<Role> myRoles() {
//		List<Role> roles = findRolesByRoleType(RoleType.ROLE_GROUP);
		List<Role> roles = findAllByRoleType(RoleType.ROLE_GROUP);

		if (SecurityUtils.isAdmin()) {
			return roles;
		}

		List<Role> myRoles = new ArrayList<>();
		if (SecurityUtils.getMoldAuthorities() == null || SecurityUtils.getMoldAuthorities().isEmpty()) {
			return myRoles;
		}

		for (String moldAuthority : SecurityUtils.getMoldAuthorities()) {
			for (Role role : roles) {
				if (moldAuthority.equals(role.getAuthority())) {
					myRoles.add(role);
					break;
				}
			}
		}

		return myRoles;
	}

	public boolean existsCode(RoleType roleType, String code, Long id) {
		if (id != null)
			return roleRepository.existsRoleByRoleTypeAndAuthorityAndIdNot(roleType, code, id);
		return roleRepository.existsRoleByRoleTypeAndAuthority(roleType, code);

	}

	public boolean existsName(RoleType roleType, String name, Long id) {
		if (id != null)
			return roleRepository.existsRoleByRoleTypeAndNameAndIdNot(roleType, name, id);
		return roleRepository.existsRoleByRoleTypeAndName(roleType, name);
	}

	public ApiResponse validRole(RolePayload payload, Long id) {
		if (payload.getRoleType() == null) {
			return new ApiResponse(false, "Type is required.");
		}
		payload.setAuthority(StringUtils.trimWhitespace(payload.getAuthority()));
		payload.setName(StringUtils.trimWhitespace(payload.getName()));
		boolean existsCode = existsCode(payload.getRoleType(), payload.getAuthority(), id);
		boolean existsName = existsName(payload.getRoleType(), payload.getName(), id);

		if (existsCode) {
			return new ApiResponse(false, payload.getRoleType().getTitle() + " ID is already registered in the system.");
		}
		if (existsName) {
			return new ApiResponse(false, payload.getRoleType().getTitle() + " Name is already registered in the system.");
		}
		return null;
	}

	public ApiResponse deleteInBatch(List<Long> ids) {
		try {
			ids.forEach(this::deleteById);
			return ApiResponse.success(CommonMessage.OK);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	@Transactional
	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
		try {
			List<Role> roles = roleRepository.findAllById(dto.getIds());
			roles.forEach(role -> {
				;
				role.setEnabled(dto.isEnabled());
				save(role);
			});
			return ApiResponse.success(CommonMessage.OK, roles);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

}
