package saleson.api.terminal.payload;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.location.repository.area.AreaRepository;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import saleson.api.equipment.payload.EquipmentPayload;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.IpType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.QTerminal;
import saleson.model.Terminal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerminalPayload extends EquipmentPayload {
	private String installationArea;
	private IpType ipType;


	private String ipAddress;
	private String subnetMask;
	private String gateway;
	private String dns;

	private Boolean enabled;

	private Long tabId;

	private Boolean isDefaultTab;

	private List<Long> ids;

	private List<String> connectionStatusList;

	private List<EquipmentStatus> terminalStatusList;

	private Boolean isTerminalScreen;

	private Long areaId;

	private List<Long> areaIdList;


	public String getInstallationArea() {
		return installationArea;
	}

	public void setInstallationArea(String installationArea) {
		this.installationArea = installationArea;
	}

	public IpType getIpType() {
		return ipType;
	}

	public void setIpType(IpType ipType) {
		this.ipType = ipType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSubnetMask() {
		return subnetMask;
	}

	public void setSubnetMask(String subnetMask) {
		this.subnetMask = subnetMask;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public String getDns() {
		return dns;
	}

	public void setDns(String dns) {
		this.dns = dns;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Long getTabId() {
		return tabId;
	}

	public void setTabId(Long tabId) {
		this.tabId = tabId;
	}

	public Boolean getDefaultTab() {
		return isDefaultTab;
	}

	public void setDefaultTab(Boolean defaultTab) {
		isDefaultTab = defaultTab;
	}

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public List<String> getConnectionStatusList() {
		return connectionStatusList;
	}

	public void setConnectionStatusList(List<String> connectionStatusList) {
		this.connectionStatusList = connectionStatusList;
	}

	public List<EquipmentStatus> getTerminalStatusList() {
		return terminalStatusList;
	}

	public void setTerminalStatusList(List<EquipmentStatus> terminalStatusList) {
		this.terminalStatusList = terminalStatusList;
	}

	public Boolean getIsTerminalScreen() {
		return isTerminalScreen;
	}

	public void setIsTerminalScreen(Boolean terminalScreen) {
		isTerminalScreen = terminalScreen;
	}

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public Boolean getTerminalScreen() {
		return isTerminalScreen;
	}

	public void setTerminalScreen(Boolean terminalScreen) {
		isTerminalScreen = terminalScreen;
	}

	public List<Long> getAreaIdList() {
		return areaIdList;
	}

	public void setAreaIdList(List<Long> areaIdList) {
		this.areaIdList = areaIdList;
	}

	public Terminal getModel() {
		Terminal terminal = new Terminal();
		terminal.setEnabled(true);
		bindData(terminal);
		return terminal;
	}

	public Terminal getModel(Terminal terminal) {
		bindData(terminal);
		return terminal;
	}

	private void bindData(Terminal terminal) {
		terminal.setEquipmentCode(getEquipmentCode());
		terminal.setEquipmentStatus(getEquipmentStatus());
		terminal.setPurchasedAt(getPurchasedAt());
		terminal.setLocationId(getLocationId());
		terminal.setInstalledAt(getInstalledAt());
		terminal.setInstalledBy(getInstalledBy());
		terminal.setInstallationArea(getInstallationArea());
		terminal.setIpType(getIpType());
		terminal.setIpAddress(getIpAddress());
		terminal.setSubnetMask(getSubnetMask());
		terminal.setGateway(getGateway());
		terminal.setDns(getDns());
		terminal.setMemo(getMemo());
		terminal.setEnabled(terminal.isEnabled());
		if (getAreaId() != null && BeanUtils.get(AreaRepository.class).existsById(getAreaId()))
			terminal.setArea(BeanUtils.get(AreaRepository.class).findById(getAreaId()).orElse(null));
		else terminal.setArea(null);
	}


	public Predicate getPredicate() {
		QTerminal terminal = QTerminal.terminal;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(terminal.companyId.in(AccessControlUtils.getAllAccessibleCompanyIds()));
//					.or(terminal.createdBy.eq(SecurityUtils.getUserId())));

/*
			if(SecurityUtils.getCompanyName().equals("FLEXTRONICS") || SecurityUtils.getCompanyName().equals("SKP")){
				builder.and(terminal.companyId.in(JPQLQueryUtils.getSubCompanyIdSubQuery()));
			}else {
				builder.and(terminal.companyId.eq(SecurityUtils.getCompanyId()));
			}
*/
		}
		if(getId()!=null){
			builder.and(terminal.id.eq(getId()));
		}
		if (getOperatingStatus() != null) {
			builder.and(terminal.operatingStatus.eq(getOperatingStatus()));
		}
		if(getOperatingStatuses() != null){
			builder.and(terminal.operatingStatus.in(getOperatingStatuses()));
		}

		if (getEquipmentStatus() != null) {
			builder.and(terminal.equipmentStatus.eq(getEquipmentStatus()));
		}
		if (getEquipmentStatuses() != null) {
			builder.and(terminal.equipmentStatus.in(getEquipmentStatuses()));
		}

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFields(getQuery()));
		}

		if (CollectionUtils.isNotEmpty(ids) || (isDefaultTab != null && !isDefaultTab)) {
			builder.and(terminal.id.in(ids));
		}


		if (!StringUtils.isEmpty(getStatus())) {
			Stream.of(CompanyType.values())
					.filter(companyType -> getStatus().equalsIgnoreCase(companyType.name()))
					.forEach(companyType -> {
						builder.and(terminal.location.company.companyType.eq(companyType));
					});

			if ("disabled".equalsIgnoreCase(getStatus())) {
				builder.and(terminal.enabled.isFalse());
			} else {
				builder.and(terminal.enabled.isTrue());
			}
		} else {
			builder.and(terminal.enabled.isTrue());
		}
		if(getCompanyId()!=null){
			builder.and(terminal.companyId.eq(getCompanyId()));
		}
		if(getLocationId()!=null){
			builder.and(terminal.locationId.eq(getLocationId()));
		}
		if(getAreaIdList()!=null){
			builder.and(terminal.areaId.in(getAreaIdList()));
		}

		checkStatus(builder, terminal);

		return builder;

	}
	private BooleanBuilder buildQueryFromSelectedFields(String query) {
		QTerminal terminal = QTerminal.terminal;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(terminal.equipmentCode.contains(query));

		if (CollectionUtils.isEmpty(getSelectedFields())) {
			builder.or(terminal.equipmentCode.contains(getQuery()))
                    .or(terminal.location.name.contains(getQuery()))
                    .or(terminal.location.locationCode.contains(getQuery()))
                    .or(terminal.location.company.name.contains(getQuery()))
                    .or(terminal.location.company.companyCode.contains(getQuery()));
		} else {
            getSelectedFields().forEach(selectedField -> {
                switch (selectedField) {
                    case "company":
                        builder.or(terminal.location.company.companyCode.contains(query))
                                .or(terminal.location.company.name.contains(getQuery()));
                        break;
                    case "location":
                        builder.or(terminal.location.locationCode.contains(query))
                                .or(terminal.location.name.contains(getQuery()));
                        break;
                    case "installationArea":
                        builder.or(terminal.installationArea.contains(query));
                        break;
//                    case "operatingStatus":
//                        builder.or(terminal.operatingStatus.stringValue().containsIgnoreCase(query));
//                        break;
//                    case "equipmentStatus":
//                        builder.or(terminal.equipmentStatus.stringValue().containsIgnoreCase(query));
//                        break;
                    case "installedBy":
                        builder.or(terminal.installedBy.contains(query));
                        break;
                    case "memo":
                        builder.or(terminal.memo.contains(query));
                        break;
                }
            });
        }

		return builder;
	}

	private void checkStatus(BooleanBuilder builder, QTerminal terminal) {
		if (isTerminalScreen != null && isTerminalScreen
				&& CollectionUtils.isEmpty(terminalStatusList)
				&& CollectionUtils.isEmpty(connectionStatusList)) {
			// no data logic fake
			builder.and(terminal.id.in(Lists.newArrayList()));
		} else {
			BooleanBuilder statusBuilder = new BooleanBuilder();
			if (CollectionUtils.isNotEmpty(connectionStatusList)) {
				for (String connectionStatus : connectionStatusList) {
					switch (connectionStatus) {
						case "ONLINE": {
							statusBuilder.or(terminal.operatingStatus.eq(OperatingStatus.WORKING));
							break;
						}
						case "OFFLINE": {
							statusBuilder.or(terminal.operatingStatus.ne(OperatingStatus.WORKING));
							break;
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(terminalStatusList)) {
				statusBuilder.or(terminal.equipmentStatus.in(terminalStatusList));
			}
			builder.and(statusBuilder);
		}
	}

}
