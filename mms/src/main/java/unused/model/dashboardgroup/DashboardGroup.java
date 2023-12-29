package unused.model.dashboardgroup;

//@Deprecated
//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
//public class DashboardGroup {
//	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
//	private Long id;
//
//	@Enumerated(EnumType.STRING)
//	private DashboardGroupType dashboardGroupType;
//
//	@Enumerated(EnumType.STRING)
//	private GraphType graphType;
//
//	@Column(length = 1)
//	@Convert(converter = BooleanYnConverter.class)
//	private boolean deleted;
//
//	@Column(name = "USER_ID", insertable = false, updatable = false)
//	private Long userId;
//
//	@JsonIgnore
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "USER_ID")
//	private User user;
//
//	public DashboardGroup(DashboardGroupType dashboardGroupType, GraphType graphType) {
//		this.dashboardGroupType = dashboardGroupType;
//		this.graphType = graphType;
//	}
//
//	public DashboardGroup(DashboardGroupType dashboardGroupType, GraphType graphType, User user) {
//		this.dashboardGroupType = dashboardGroupType;
//		this.graphType = graphType;
//		this.user = user;
//	}
//
//	public DashboardGroup(DashboardGroupType dashboardGroupType, GraphType graphType, boolean deleted, User user) {
//		this.dashboardGroupType = dashboardGroupType;
//		this.graphType = graphType;
//		this.deleted = deleted;
//		this.user = user;
//	}
//}
