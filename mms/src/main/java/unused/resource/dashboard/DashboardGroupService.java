package unused.resource.dashboard;

//@Deprecated
//@Service
//@Slf4j
//public class DashboardGroupService {
//	@Autowired
//	private DashboardGroupRepository dashboardGroupRepository;
//	@Autowired
//	private RoleService roleService;
//	@Autowired
//	private UserRepository userRepository;
//
////	@Autowired
////	private GeneralConfigService generalConfigService;
////	@Autowired
////	private UserContentRepository userContentRepository;
//	/*
//	DashboardGroupPayload assPayload;
//	DashboardGroupPayload supplierPayload;
//	final Runnable assRun = ThreadUtils.debounce(new Runnable() {
//	    @Override
//	    public void run() {
//	        if(assPayload==null){
//	            log.info("ass null");
//	            return;
//	        }
//	        log.info("run type {}", assPayload.getDashboardGroupType());
//	        saveGraphProfile(assPayload);
//	    }
//	},1000);
//	final Runnable supplierRun = ThreadUtils.debounce(new Runnable() {
//	    @Override
//	    public void run() {
//	        if(supplierPayload==null){
//	            log.info("supplier null");
//	            return;
//	        }
//	        log.info("run type {}", supplierPayload.getDashboardGroupType());
//	        saveGraphProfile(supplierPayload);
//	    }
//	},1000);
//	
//	public void saveDebounce(DashboardGroupPayload dashboardGroupPayload) {
//	    log.info("call type {}", dashboardGroupPayload.getDashboardGroupType());
//	    if (DashboardGroupType.ASSET_MANAGEMENT_DASHBOARD.equals(dashboardGroupPayload.getDashboardGroupType())) {
//	        assPayload = dashboardGroupPayload;
//	        assRun.run();
//	    } else {
//	        supplierPayload = dashboardGroupPayload;
//	        supplierRun.run();
//	    }
//	}*/
//	public List<DashboardGroup> saveGraphProfile(DashboardGroupPayload dashboardGroupPayload) {
//		if (dashboardGroupPayload == null)
//			return null;
//		List<DashboardGroup> dashboardGroupList = dashboardGroupRepository.findAllByUserIdAndDeletedIsFalse(SecurityUtils.getUserId());
//		List<DashboardGroup> listRemove = dashboardGroupPayload.getGraphTypes() == null ? dashboardGroupList
//				: dashboardGroupList.stream().filter(graphGroup -> !dashboardGroupPayload.getGraphTypes().contains(graphGroup.getGraphType())).collect(Collectors.toList());
//
//		List<GraphType> oldTypeList = dashboardGroupList.stream().map(graphGroup -> graphGroup.getGraphType()).collect(Collectors.toList());
//		List<DashboardGroup> listNew = dashboardGroupPayload.getModels().stream().filter(graphGroup -> !oldTypeList.contains(graphGroup.getGraphType()))
//				.collect(Collectors.toList());
//		if (SecurityUtils.getUserId() != null) {
//			listNew.forEach(g -> g.setUser(userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()))));
//		}
//
//		//had make default config
//		listRemove.stream().forEach(dashboardGroup -> dashboardGroup.setDeleted(true));
//		dashboardGroupRepository.saveAll(listRemove);
////        dashboardGroupRepository.deleteAll(listRemove);
//		dashboardGroupRepository.saveAll(listNew);
//		dashboardGroupList.removeAll(listRemove);
//		dashboardGroupList.addAll(listNew);
//		return dashboardGroupList;
//	}
//
//	public List<DashboardGroup> getGraphProfile(DashboardGroupType dashboardGroupType) {
//		List<DashboardGroup> dashboardGroupList = dashboardGroupRepository.findAllByUserIdAndDeletedIsFalse(SecurityUtils.getUserId());
////        if (dashboardGroupType != null) {
////            dashboardGroupList = dashboardGroupRepository.findAllByDashboardGroupTypeAndDeletedIsFalse(dashboardGroupType);
////        } else {
////            dashboardGroupList = dashboardGroupRepository.findAll();
////        }
//
//		return dashboardGroupList.stream().filter(dashboardGroup -> dashboardGroup.getGraphType() != null && dashboardGroup.getGraphType().isEnabled())
//				.collect(Collectors.toList());
//	}
//
////	public void updateConfigDashboardGroupByUserContent(List<UserContent> userContents) {
////		if (!OptionUtils.isEnabled(ConfigCategory.DASHBOARD_GROUP, false)) {
////			return;
////    	}
////		String dashboardGroupTypeValue = OptionUtils.getValue(ConfigCategory.DASHBOARD_GROUP, "value", "ASSET_MANAGEMENT_DASHBOARD");
////		try {
////			DashboardGroupType dashboardGroupType = DashboardGroupType.valueOf(dashboardGroupTypeValue);
////			List<DashboardGroup> dashboardGroupList = dashboardGroupRepository.findAllByDashboardGroupTypeAndDeletedIsFalse(dashboardGroupType);
////
////			List<GraphType> listActive = userContents.stream()//
////					.filter(userContent -> userContent.getEnabled() == null || userContent.getEnabled())//
////					.map(userContent -> userContent.getType())//
////					.collect(Collectors.toList());
////			List<GraphType> listDisable = userContents.stream()//
////					.filter(userContent -> userContent.getEnabled() != null && !userContent.getEnabled())//
////					.map(userContent -> userContent.getType())//
////					.collect(Collectors.toList());
////
////			List<GraphType> graphTypeList = new ArrayList<>();
////			graphTypeList.addAll(dashboardGroupList.stream().map(dashboardGroup -> dashboardGroup.getGraphType()).collect(Collectors.toList()));
////			graphTypeList.addAll(listActive);
////			graphTypeList.removeAll(listDisable);
////
////			DashboardGroupPayload dashboardGroupPayload = new DashboardGroupPayload(dashboardGroupType, graphTypeList);
////			saveGraphProfile(dashboardGroupPayload);
//////            saveDebounce(dashboardGroupPayload);
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////	}
//
////	public void updateConfigDashboardGroupByUserContent_New(List<UserContent> userContents) {
////		if (!OptionUtils.isEnabled(ConfigCategory.DASHBOARD_GROUP, false)) {
////			return;
////		}
////		String dashboardGroupTypeValue = OptionUtils.getValue(ConfigCategory.DASHBOARD_GROUP, "value", "ASSET_MANAGEMENT_DASHBOARD");
////		try {
////			DashboardGroupType dashboardGroupType = DashboardGroupType.valueOf(dashboardGroupTypeValue);
////			List<DashboardGroup> dashboardGroupList = dashboardGroupRepository.findAllByUserIdAndDeletedIsFalse(SecurityUtils.getUserId());
////
////			List<GraphType> listActive = userContents.stream()//
////					.filter(userContent -> userContent.getEnabled() == null || userContent.getEnabled())//
////					.map(UserContent::getType)//
////					.collect(Collectors.toList());
////			List<GraphType> listDisable = userContents.stream()//
////					.filter(userContent -> userContent.getEnabled() != null && !userContent.getEnabled())//
////					.map(UserContent::getType)//
////					.collect(Collectors.toList());
////
////			List<GraphType> graphTypeList = new ArrayList<>();
////			graphTypeList.addAll(dashboardGroupList.stream().map(DashboardGroup::getGraphType).collect(Collectors.toList()));
////			graphTypeList.addAll(listActive);
////			graphTypeList.removeAll(listDisable);
////
////			DashboardGroupPayload dashboardGroupPayload = new DashboardGroupPayload(dashboardGroupType, graphTypeList);
////			saveGraphProfile(dashboardGroupPayload);
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////	}
//
////	public void updateUserContentActiveByConfigDashboardGroupBy(List<UserContent> userContents) {
////		if (!OptionUtils.isEnabled(ConfigCategory.DASHBOARD_GROUP, false)) {
////			return;
////		}
////		String dashboardGroupTypeValue = OptionUtils.getValue(ConfigCategory.DASHBOARD_GROUP, "value", "ASSET_MANAGEMENT_DASHBOARD");
////		try {
////			DashboardGroupType dashboardGroupType = DashboardGroupType.valueOf(dashboardGroupTypeValue);
////			List<DashboardGroup> dashboardGroupList = dashboardGroupRepository.findAllByDashboardGroupTypeAndDeletedIsFalse(dashboardGroupType);
////			List<GraphType> graphTypeList = dashboardGroupList.stream().map(dashboardGroup -> dashboardGroup.getGraphType()).collect(Collectors.toList());
////			userContents.stream().forEach(userContent -> {
////				if (userContent.getEnabled() != null && userContent.getEnabled() && graphTypeList.contains(userContent.getType())) {
////					userContent.setEnabled(true);
////				} else
////					userContent.setEnabled(false);
////			});
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////	}
//
////	public void updateUserContentActiveByConfigDashboardGroupBy_New(List<UserContent> userContents) {
////		try {
////			List<DashboardGroup> dashboardGroupList = dashboardGroupRepository.findAllByUserIdAndDeletedIsFalse(SecurityUtils.getUserId());
////			List<GraphType> graphTypeList = dashboardGroupList.stream().map(DashboardGroup::getGraphType).collect(Collectors.toList());
////			userContents.forEach(userContent -> {
////				userContent.setEnabled(userContent.getEnabled() != null && userContent.getEnabled() && graphTypeList.contains(userContent.getType()));
////			});
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////	}
//
//	@PostConstruct
//	public void makeDefaultConfig() {
//		if (!dashboardGroupRepository.existsByDashboardGroupType(DashboardGroupType.ASSET_MANAGEMENT_DASHBOARD)) {
//			DashboardGroupPayload dashboardGroupPayload = new DashboardGroupPayload(DashboardGroupType.ASSET_MANAGEMENT_DASHBOARD,
//					Const.DASHBOARD_GROUP_DEFAULT.ASSET_MANAGEMENT_DASHBOARD);
//			saveGraphProfile(dashboardGroupPayload);
//		}
//		if (!dashboardGroupRepository.existsByDashboardGroupType(DashboardGroupType.SUPPLIER_PERFORMANCE_MANAGEMENT_DASHBOARD)) {
//			DashboardGroupPayload dashboardGroupPayload = new DashboardGroupPayload(DashboardGroupType.SUPPLIER_PERFORMANCE_MANAGEMENT_DASHBOARD,
//					Const.DASHBOARD_GROUP_DEFAULT.SUPPLIER_PERFORMANCE_MANAGEMENT_DASHBOARD);
//			saveGraphProfile(dashboardGroupPayload);
//		}
//	}
//
//	public ApiResponse resetDefaultConfig() {
//		try {
//			List<DashboardGroup> current = dashboardGroupRepository.findAllByDeletedIsFalse();
////            current.forEach(c -> c.setDeleted(true));
//			dashboardGroupRepository.deleteAll(current);
//
//			List<DashboardGroup> listToSave = new ArrayList<>();
//			List<User> users = userRepository.findAllByEnabledIsTrueAndDeletedIsFalse();
//			users.forEach(u -> {
////                List<UserContent> userContents = userContentRepository.findByUserIdAndEnabledIsTrue(u.getId());
////                List<GraphType> chosen = userContents.stream().map(UserContent::getType).collect(Collectors.toList());
//				List<GraphType> graphTypes = roleService.getDashboardByUser(u);
//				graphTypes.forEach(g -> {
////                    if (chosen.contains(g))
//					listToSave.add(new DashboardGroup(DashboardGroupType.ASSET_MANAGEMENT_DASHBOARD, g, false, u));
////                    else
////                        listToSave.add(new DashboardGroup(DashboardGroupType.INDIVIDUAL_DASHBOARDS, g, true, u));
//				});
//			});
//			dashboardGroupRepository.saveAll(listToSave);
//			return ApiResponse.success(CommonMessage.OK, listToSave);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ApiResponse.error();
//		}
//	}
//}
