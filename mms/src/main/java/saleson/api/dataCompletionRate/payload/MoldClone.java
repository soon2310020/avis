package saleson.api.dataCompletionRate.payload;

import lombok.Data;
import saleson.model.Mold;

@Data
public class MoldClone extends Mold{
/*
    private Long id;
    private Double lastCycleTime;
    private Integer lastShot;
    private Instant lastShotAt;
    private String toolingLetter;
    private String toolingType;
    private String toolingComplexity;
    private Boolean familyTool;
    private Integer designedShot;
    private Integer lifeYears;
    private Integer madeYear;
    private Integer contractedCycleTime;
    private String size;
    private SizeUnit sizeUnit;
    private String weight;
    private WeightUnit weightUnit;
    private Double shotSize;
    private String runnerType;
    private String runnerMaker;
    private Double weightRunner;
    private String hotRunnerDrop;
    private String hotRunnerZone;
    private String injectionMachineId;
    private Double quotedMachineTonnage;
    private Double currentMachineTonnage;
    private Integer preventCycle;
    private String engineer;
    private String engineerDate;
    private List<User> engineers = new ArrayList<>();
    private ToolingCondition toolingCondition;
    private String toolDescription;
    private Integer preventUpcoming;		// upcoming maintenance tolerance
    private Integer preventOverdue;			// overdue maintenance tolerance
    private Integer cycleTimeLimit1;		// contrantedCycleTime 기준 L1 : cycleTimeLimit1 <= L1 < cycleTimeLimit2
    private OutsideUnit cycleTimeLimit1Unit;
    private Integer cycleTimeLimit2;		// contrantedCycleTime 기준 L2 : cycleTimeLimit2 < L2
    private OutsideUnit cycleTimeLimit2Unit;

    */
/* Cost Information *//*

    private Integer cost; // for not Dyson
    private CurrencyType costCurrencyType;
    private Integer accumulatedMaintenanceCost; // for not Dyson

    private Integer totalCmCount;			// 전체 고장정비 횟수
    private Integer totalCmCosts;			// 전체 고장정비 비용.

    private CycleTimeStatus cycleTimeStatus;

    private EfficiencyStatus efficiencyStatus;

    private MisconfigureStatus misconfigureStatus;			// Preset 등록 요청 후 완료 되지 않은 채 데이터가 들어오는 경우

    */
/* add info *//*

    private String resin;
    private Double utilizationRate;

    private Company toolMaker;

    private Company supplier;


    private Integer uptimeTarget;
    private Integer uptimeLimitL1;
    private Integer uptimeLimitL2;
    private String labour;
    //	private String hourPerShift; // delete
    private String shiftsPerDay; // hour per day
    private String productionDays; // day per week
    //	private String productionWeeks; // delete
//	private String supplierTagId; // delete
    private Integer maxCapacityPerWeek; // add Theoretical number of parts to be produced
    private Integer dailyMaxCapacity; // add daily capacity part produced
    private Integer passedDays; // how long does it pass from registered date to current

    private String supplierForToolMaker; // In case Tool-maker created a tooling, free enter to supplier instead

    private NotificationStatus dataSubmission;

    private UseageType useageType;

    private Boolean locationChanged;

    private Boolean maintenanced;

    private Integer maintenanceCount;

    private Long counterId;

    private Counter counter;

    private Long machineId;

    private Machine machine;

    private Instant operatedStartAt;

    private boolean deleted;

    private Instant createdAt;
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;

    public String getCounterCode() {
        if (counter == null) {
            return "";
        }
        return counter.getEquipmentCode();
    }

    private OperatingStatus operatingStatus;


    private Instant operatedAt;

    private Instant activatedAt;


    private String equipmentCode;

    private EquipmentStatus equipmentStatus;

    private Long companyId;
    private String companyCode;

    private Long locationId;

    private Location location;


//	@Column(name = "COMPANY_ID", insertable = false, updatable = false)
//	private Long companyId;

    private String installedBy;
    private String installedAt;

    private String purchasedAt;

//	@ManyToOne(fetch = FetchType.EAGER)
//	@JoinColumn(name = "COMPANY_ID")
//	private Company company;

    private String memo;

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }


    public EquipmentStatus getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(EquipmentStatus equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


    public String getInstalledBy() {
        return installedBy;
    }

    public void setInstalledBy(String installedBy) {
        this.installedBy = installedBy;
    }

    public String getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(String installedAt) {
        this.installedAt = installedAt;
    }

    public String getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(String purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public OperatingStatus getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(OperatingStatus operatingStatus) {
        this.operatingStatus = operatingStatus;
    }

    public Instant getOperatedAt() {
        return operatedAt;
    }

    public void setOperatedAt(Instant operatedAt) { this.operatedAt = operatedAt; }

    public Instant getActivatedAt() { return activatedAt; }

    public void setActivatedAt(Instant activatedAt) { this.activatedAt = activatedAt; }


    public String getOperatedDate() {
        return DateUtils.getDate(getOperatedAt());
    }
    public String getOperatedDateTime() {
        return DateUtils.getDateTime(getOperatedAt());
    }



    // 로케이션 코드
    public String getLocationCode() {
        if (location == null) {
            return "";
        }

        return location.getLocationCode();
    }

    // 로케이션명
    public String getLocationName() {
        if (location == null) {
            return "";
        }

        return location.getName();
    }

    // 로케이션명
    public Boolean getLocationEnabled() {
        if (location == null) {
            return false;
        }

        return location.isEnabled();
    }

    // 회사명
    public String getCompanyName() {
        if (location == null || location.getCompany() == null) {
            return "";
        }

        return location.getCompany().getName();
    }
    public String getCompanyCode() {
        if (location == null || location.getCompany() == null) {
            return "";
        }

        return location.getCompany().getCompanyCode();
    }

    // 회사 enabled
    public Boolean getCompanyEnabled() {
        if (location == null || location.getCompany() == null) {
            return false;
        }

        return location.getCompany().isEnabled();
    }


    // 회사구분
    public String getCompanyTypeText() {
        if (location == null || location.getCompany() == null) {
            return "";
        }

        return location.getCompany().getCompanyTypeText();
    }

    */
/**
     * 카운터 PRESET STATUS
     * @return
     *//*

    public PresetStatus getCounterPresetStatus() {
        if (counter == null) {
            return null;
        }
        return counter.getPresetStatus();
    }

    private Set<MoldAuthority> moldAuthorities = new HashSet<>();

    private List<MoldPart> moldParts = new ArrayList<>();

    private Map<Long,List<CustomFieldValue>> customFieldValueMap=new HashMap<>();

    public Integer getNextMaintenanceShot() {
        int preventCycle = this.preventCycle == null ? 0 : this.preventCycle;
        int maintenanceCount = this.maintenanceCount == null ? 0 : this.maintenanceCount;
        return preventCycle * (maintenanceCount + 1);
    }

    public String getRunnerTypeTitle() {
        return this.runnerType;
//		return this.runnerType == null ? "" : this.runnerType.getTitle();
    }
    public String getSizeUnitTitle() {
        return this.sizeUnit == null ? "" : this.sizeUnit.getTitle();
    }
    public String getWeightUnitTitle() {
        return this.weightUnit == null ? "" : this.weightUnit.getTitle();
    }
    public String getToolingConditionTitle() {
        return this.toolingCondition == null ? "" : this.toolingCondition.getTitle();
    }

//	public String getUpperLimitTemperatureUnitText() {
//		return this.upperLimitTemperatureUnit == null ? "" : this.upperLimitTemperatureUnit.getTitle();
//	}
//	public String getLowerLimitTemperatureUnitText() {
//		return this.lowerLimitTemperatureUnit == null ? "" : this.lowerLimitTemperatureUnit.getTitle();
//	}

    public Double getContractedCycleTimeSeconds() {
        if (contractedCycleTime == null) {
            return null;
        }

        return contractedCycleTime / 10.0;
    }

    // moldParts 등록 시
    public List<MoldPart> getMoldParts() {
        if(moldParts == null || moldParts.size() == 0) return new ArrayList<>();
        Comparator<MoldPart> compareById = Comparator.comparing(MoldPart::getId);
        Collections.sort(moldParts, compareById);
        return new ArrayList<>(moldParts);
    }

    //  use to get current mold parts
    public List<MoldPart> getCurrentMoldParts(){
        if(moldParts != null){
            Comparator<MoldPart> compareById = Comparator.comparing(MoldPart::getId);
            Collections.sort(moldParts, compareById);
        }

        return this.moldParts;
    }

    // mold에 해당하는 part 정보
    public List<PartData> getParts() {
        List<PartData> parts = new ArrayList<>();

        if (moldParts != null && !moldParts.isEmpty()) {
            Comparator<MoldPart> compareById = Comparator.comparing(MoldPart::getId);
            Collections.sort(moldParts, compareById);
            for (MoldPart moldPart : moldParts) {
                PartData partData = new PartData();


                if (moldPart.getPart() != null) {
                    Part part = moldPart.getPart();
                    partData.setPartId(part.getId());
                    partData.setPartCode(part.getPartCode());
                    partData.setPartName(part.getName());
                    partData.setCavity(moldPart.getCavity());
                    partData.setTotalCavities(moldPart.getTotalCavities());
                    partData.setResinCode(moldPart.getResinCode());
                    partData.setResinGrade(moldPart.getResinGrade());
                    partData.setPartVolume(moldPart.getPartSize());
                    partData.setPartWeight(moldPart.getPartWeight());
                    partData.setDesignRevision(moldPart.getDesignRevision());

                    if (part.getCategory() != null) {
                        Category project = part.getCategory();
                        partData.setProjectId(project.getId());
                        partData.setProjectName(project.getName());

                        if (project.getParent() != null) {
                            Category category = project.getParent();
                            partData.setCategoryId(category.getId());
                            partData.setCategoryName(category.getName());
                        }
                    }
                    partData.setWeeklyDemand(moldPart.getPart().getWeeklyDemand());
                }

                parts.add(partData);
            }
        }

        return parts;
    }


    public Long getToolMakerCompanyId() {
        if (toolMaker == null) {
            return null;
        }

        return toolMaker.getId();
    }
    public String getToolMakerCompanyName() {
        if (toolMaker == null) {
            return "";
        }

        return toolMaker.getName();
    }

    public Long getSupplierCompanyId() {
        if (supplier == null) {
            return null;
        }

        return supplier.getId();

    }
    public String getSupplierCompanyName() {
        if (supplier == null) {
            return supplierForToolMaker;
        }

        return supplier.getName();
    }

    public String getSupplierCompanyCode() {
        if (supplier == null) {
            return "";
        }

        return supplier.getCompanyCode();
    }

    public Instant getLastShotAt(){
        if(getCreatedAt() == null || (lastShotAt != null && lastShotAt.compareTo(getCreatedAt()) >= 0)){
            return lastShotAt;
        }
        return null;
    }

    public String getLastShotDateTime() {
        return DateUtils.getDateTime(getLastShotAt());
    }

    public String getLastShotDate(){ return DateUtils.getDate(getLastShotAt()); }

    public String getUseageTypeName(){
        if(useageType == null) return "";
        return useageType.getTitle();
    }

    public List<String> getEngineerNames(){
        List<String> result = new ArrayList<>();
        if(engineers != null && engineers.size() > 0){
            result = engineers.stream().map(x -> x.getName()).collect(Collectors.toList());
        }
        return result;
    }

    public void setEngineers(List<User> engineers) {
        this.engineers = engineers;
        if(engineers!=null && !engineers.isEmpty()){
            this.engineer=engineers.get(0).getName();
        }
    }

    public String getCostCurrencyTypeTitle() {
        if(costCurrencyType!=null) return costCurrencyType.getTitle();
        return null;
    }
    public String getToolingSizeView() {
        if (size == null || size.equalsIgnoreCase("")
                || size.equalsIgnoreCase("undefinedxundefinedxundefined")|| size.equalsIgnoreCase("xx")) return "";
        size=size.replaceAll("undefined","0");
        if(size.startsWith("x"))size="0"+size;
        size=size.replaceAll("x\\s*x","x 0 x");
        if(size.endsWith("x"))size=size+"0";
        size=size.replaceAll("x"," x ").replaceAll("\\s+"," ").trim();
        String unit = getSizeUnit()!=null ? getSizeUnit().getTitle():"";
        return size + " " + unit;
    }
    public String getToolingWeightView() {
        if (weight == null || weight.equalsIgnoreCase("")) return "";
        return weight + " " + (weightUnit != null ? weightUnit.getTitle() : "");
    }
*/
}
