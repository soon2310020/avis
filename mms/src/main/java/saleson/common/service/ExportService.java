package saleson.common.service;

import com.emoldino.framework.util.DateUtils2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saleson.api.filestorage.payload.MultiFileStoragePayload;
import saleson.api.mold.payload.PartData;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.StorageType;
import saleson.model.FileStorage;
import saleson.model.Mold;
import saleson.model.MoldEndLifeCycle;
import saleson.model.User;
import saleson.restdocs.dto.exports.*;
import saleson.service.util.DateTimeUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static saleson.common.enumeration.CurrencyType.USD;

@Service
public class ExportService {
    @Autowired
    FileStorageService fileStorageService;
    public static DecimalFormat formatter = new DecimalFormat("#,###");

    public void bindData(Mold mold, BasicInformationDto basicInformationDto){
        DecimalFormat formatter = new DecimalFormat("#,###");
        basicInformationDto.setToolingId(mold.getEquipmentCode());
        basicInformationDto.setSupplierMoldCode(mold.getSupplierMoldCode());
        basicInformationDto.setToolingLetter(mold.getToolingLetter() != null ? mold.getToolingLetter() : "");
        basicInformationDto.setToolingType(mold.getToolingType() != null ? mold.getToolingType() : "");
        basicInformationDto.setToolingComplexity(mold.getToolingComplexity() != null ? mold.getToolingComplexity() : "");
        basicInformationDto.setCounterId(mold.getCounterCode() != null ? mold.getCounterCode() : "");
        basicInformationDto.setFamilyTool(mold.getFamilyTool() == true ? "Yes" : "No");
        basicInformationDto.setApprovedCycleTime(mold.getContractedCycleTimeSeconds() + "s");
        basicInformationDto.setToolDescription(mold.getToolDescription() != null ? mold.getToolDescription() : "");
        basicInformationDto.setForecastedMaxShots(mold.getDesignedShot() != null ? formatter.format(mold.getDesignedShot()) : "");
        basicInformationDto.setForecastedToolLife(mold.getLifeYears() != null ? mold.getLifeYears() + " years" : "");
        basicInformationDto.setYeaOfToolMade(mold.getMadeYear() != null ? String.valueOf(mold.getMadeYear()) : "");

        String engineerInCharge = "";
        List<String> engineerNames = mold.getEngineerNames();
        for(int i = 0; i < engineerNames.size(); i++){
            if(i == engineerNames.size() - 1)
                engineerInCharge += engineerNames.get(i);
            else
                engineerInCharge += engineerNames.get(i) + ", ";
        }
        basicInformationDto.setEngineerInCharge(engineerInCharge);

        String plantEngineerInCharge = "";
        List<String> plantEngineerNames = mold.getPlantEngineerNames();
        for(int i = 0; i < plantEngineerNames.size(); i++){
            if(i == plantEngineerNames.size() - 1)
                plantEngineerInCharge += plantEngineerNames.get(i);
            else
                plantEngineerInCharge += plantEngineerNames.get(i) + ", ";
        }
        basicInformationDto.setPlantEngineerInCharge(plantEngineerInCharge);
    }

    public void bindData(User user, UserInformationDto userInformationDto){
        userInformationDto.setName(user.getName());
        userInformationDto.setEmail(user.getEmail());
        userInformationDto.setCompany(user.getCompany());
        userInformationDto.setLastLogin(user.getLastLogin());
    }
    public void bindData(MoldEndLifeCycle mm, BasicInformationDto basicInformationDto){
        DecimalFormat formatter = new DecimalFormat("#,###");
        basicInformationDto.setToolingId(mm.getMold().getEquipmentCode());
        basicInformationDto.setToolingLetter(mm.getMold().getToolingLetter() != null ? mm.getMold().getToolingLetter() : "");
        basicInformationDto.setToolingType(mm.getMold().getToolingType() != null ? mm.getMold().getToolingType() : "");
        basicInformationDto.setToolingComplexity(mm.getMold().getToolingComplexity() != null ? mm.getMold().getToolingComplexity() : "");
        basicInformationDto.setCounterId(mm.getMold().getCounterCode() != null ? mm.getMold().getCounterCode() : "");
        basicInformationDto.setFamilyTool(mm.getMold().getFamilyTool() == true ? "Yes" : "No");
        basicInformationDto.setApprovedCycleTime(mm.getMold().getContractedCycleTimeSeconds() + "s");
        basicInformationDto.setToolDescription(mm.getMold().getToolDescription() != null ? mm.getMold().getToolDescription() : "");
        basicInformationDto.setForecastedMaxShots(mm.getMold().getDesignedShot() != null ? formatter.format(mm.getMold().getDesignedShot()) : "");
        basicInformationDto.setForecastedToolLife(mm.getMold().getLifeYears() != null ? mm.getMold().getLifeYears() + " years" : "");
        basicInformationDto.setYeaOfToolMade(mm.getMold().getMadeYear() != null ? String.valueOf(mm.getMold().getMadeYear()) : "");

        String engineerInCharge = "";
        List<String> engineerNames = mm.getMold().getEngineerNames();
        for(int i = 0; i < engineerNames.size(); i++){
            if(i == engineerNames.size() - 1)
                engineerInCharge += engineerNames.get(i);
            else
                engineerInCharge += engineerNames.get(i) + ", ";
        }
        basicInformationDto.setEngineerInCharge(engineerInCharge);
    }

    public void bindData(Mold mold, DynamicInformationDto dynamicInformationDto, Integer timezoneOffsetClient){
        DecimalFormat formatter = new DecimalFormat("#,###");
        if(mold.getEquipmentStatus().equals(EquipmentStatus.INSTALLED)) {
            dynamicInformationDto.setOp(mold.getOperatingStatus() == null ?
                    "" : (mold.getOperatingStatus().equals(OperatingStatus.WORKING) ?
                    "Active" : (mold.getOperatingStatus().equals(OperatingStatus.IDLE) ?
                    "Idle" : (mold.getOperatingStatus().equals(OperatingStatus.NOT_WORKING) ?
                    "Inactive" : (mold.getOperatingStatus().equals(OperatingStatus.DISCONNECTED) ?
                    "Disconnected" : "")))));
        }else dynamicInformationDto.setOp("");
        dynamicInformationDto.setNoOfShots(mold.getLastShot() != null ? formatter.format(mold.getLastShot()) : "");
        String lastShotAt= DateTimeUtils.formatDateTimeWithTimeZone(mold.getLastShotAt(),timezoneOffsetClient);
        dynamicInformationDto.setLastDateOfShots(lastShotAt!=null?lastShotAt:"");

        String utilizationRate = "0%";
        if(mold.getLastShot() != null && mold.getDesignedShot() != 0){
            utilizationRate = String.format("%.1f", Double.valueOf(mold.getLastShot()) * 100 / mold.getDesignedShot()) + "%";
        }
        dynamicInformationDto.setUtilisationRate((mold.getLastShot() != null ? formatter.format(mold.getLastShot()) + "/" : "0/") + formatter.format(mold.getDesignedShot())  +" "+ "(" + utilizationRate + ")");
        dynamicInformationDto.setRemainingPartsCount(mold.getRemainingPartsCount() != null ? formatter.format(mold.getRemainingPartsCount()) : "");

        dynamicInformationDto.setLocation(mold.getLocationName());
        dynamicInformationDto.setCycleTime(mold.getLastCycleTime() != null ? mold.getLastCycleTime() / 10 + "s" : "");
        String weightedAverageCycleTimeString = (mold.getWeightedAverageCycleTime() != null ?  String.format("%.2f", mold.getWeightedAverageCycleTime() / 10) : "");
        String[] weightedAverageCycleTimeArr = weightedAverageCycleTimeString.split("\\.");

        if (weightedAverageCycleTimeArr.length == 2 && StringUtils.isNotBlank(weightedAverageCycleTimeString)) {
            if (weightedAverageCycleTimeArr[1].equalsIgnoreCase("00")) {
                dynamicInformationDto.setWeightedAverageCycleTime(weightedAverageCycleTimeArr[0] +"s");
            } else if (weightedAverageCycleTimeString.substring(weightedAverageCycleTimeString.length() - 1).equalsIgnoreCase("0")) {
                dynamicInformationDto.setWeightedAverageCycleTime(weightedAverageCycleTimeString.substring(0, weightedAverageCycleTimeString.length() - 1)+ "s");
            } else {
                dynamicInformationDto.setWeightedAverageCycleTime(weightedAverageCycleTimeString+ "s");
            }
        } else {
            dynamicInformationDto.setWeightedAverageCycleTime(weightedAverageCycleTimeArr[0]);
        }

    }

    public void bindData(MoldEndLifeCycle mm, DynamicInformationDto dynamicInformationDto, Integer timezoneOffsetClient){
        DecimalFormat formatter = new DecimalFormat("#,###");
        if(mm.getMold().getEquipmentStatus().equals(EquipmentStatus.INSTALLED)) {
            dynamicInformationDto.setOp(mm.getMold().getOperatingStatus() == null ?
                    "" : (mm.getMold().getOperatingStatus().equals(OperatingStatus.WORKING) ?
                    "Active" : (mm.getMold().getOperatingStatus().equals(OperatingStatus.IDLE) ?
                    "Idle" : (mm.getMold().getOperatingStatus().equals(OperatingStatus.NOT_WORKING) ?
                    "Inactive" : (mm.getMold().getOperatingStatus().equals(OperatingStatus.DISCONNECTED) ?
                    "Disconnected" : "")))));
        }else dynamicInformationDto.setOp("");
        dynamicInformationDto.setNoOfShots(mm.getMold().getLastShot() != null ? formatter.format(mm.getMold().getLastShot()) : "");
        String lastShotAt= DateTimeUtils.formatDateTimeWithTimeZone(mm.getMold().getLastShotAt(),timezoneOffsetClient);
        dynamicInformationDto.setLastDateOfShots(lastShotAt!=null?lastShotAt:"");

        String utilizationRate = "0%";
        if(mm.getMold().getLastShot() != null && mm.getMold().getDesignedShot() != 0){
            utilizationRate = String.format("%.1f", Double.valueOf(mm.getMold().getLastShot()) * 100 / mm.getMold().getDesignedShot()) + "%";
        }
        dynamicInformationDto.setUtilisationRate((mm.getMold().getLastShot() != null ? formatter.format(mm.getMold().getLastShot()) + "/" : "0/") + formatter.format(mm.getMold().getDesignedShot())  +" "+ "(" + utilizationRate + ")");

        dynamicInformationDto.setLocation(mm.getMold().getLocationName());
        dynamicInformationDto.setCycleTime(mm.getMold().getLastCycleTime() != null ? mm.getMold().getLastCycleTime() / 10 + "s" : "");
        dynamicInformationDto.setWeightedAverageCycleTime(mm.getMold().getWeightedAverageCycleTime() != null ? mm.getMold().getWeightedAverageCycleTime() / 10 + "s" : "");
    }

    public void bindData(MoldEndLifeCycle mm, MoldEndLifeCycleInformationDto moldEndLifeCycleInformationDto){
        DecimalFormat formatter = new DecimalFormat("#,###");
        moldEndLifeCycleInformationDto.setPriority(mm.getPriority() + "");
        moldEndLifeCycleInformationDto.setInsights(mm.getEndLifeAtDate());
    }

    public void bindData(Mold mold, MaintenanceInformationDto maintenanceInformationDto){
        DecimalFormat formatter = new DecimalFormat("#,###");
        MultiFileStoragePayload fileStoragePayload = new MultiFileStoragePayload();
        fileStoragePayload.setRefId(mold.getId());
        fileStoragePayload.setStorageTypes(Arrays.asList(StorageType.MOLD_CONDITION, StorageType.MOLD_MAINTENANCE_DOCUMENT, StorageType.MOLD_INSTRUCTION_VIDEO));
        Iterable<FileStorage> files = fileStorageService.findAll(fileStoragePayload.getPredicate());

        List<String> conditions = StreamSupport.stream(files.spliterator(), false)
                .filter(x -> x.getStorageType().equals(StorageType.MOLD_CONDITION))
                .map(x -> x.getFileName())
                .collect(Collectors.toList());

        List<String> documents = StreamSupport.stream(files.spliterator(), false)
                .filter(x -> x.getStorageType().equals(StorageType.MOLD_MAINTENANCE_DOCUMENT))
                .map(x -> x.getFileName())
                .collect(Collectors.toList());

        List<String> videos = StreamSupport.stream(files.spliterator(), false)
                .filter(x -> x.getStorageType().equals(StorageType.MOLD_INSTRUCTION_VIDEO))
                .map(x -> x.getFileName())
                .collect(Collectors.toList());

        String conditionOfTooling = "";
        for(int i = 0; i < conditions.size(); i++){
            if(i == conditions.size() - 1)
                conditionOfTooling += conditions.get(i);
            else
                conditionOfTooling += conditions.get(i) + ", ";
        }
        maintenanceInformationDto.setConditionOfTooling(conditionOfTooling);

        String maintenanceDocument = "";
        for(int i = 0; i < documents.size(); i++){
            if(i == documents.size() - 1)
                maintenanceDocument += documents.get(i);
            else
                maintenanceDocument += documents.get(i) + ", ";
        }
        maintenanceInformationDto.setMaintenanceDocument(maintenanceDocument);

        String instructionVideo = "";
        for(int i = 0; i < videos.size(); i++){
            if(i == videos.size() - 1)
                instructionVideo += videos.get(i);
            else
                instructionVideo += videos.get(i) + ", ";
        }
        maintenanceInformationDto.setInstructionVideo(instructionVideo);

        maintenanceInformationDto.setMaintenanceInterval(mold.getPreventCycle()!=null ? formatter.format(mold.getPreventCycle()): "");
        maintenanceInformationDto.setUpcomingMaintenanceTolerance(mold.getPreventUpcoming()!=null? formatter.format(mold.getPreventUpcoming()): "");
//        maintenanceInformationDto.setOverdueMaintenanceTolerance(mold.getPreventOverdue() == null ? "" : formatter.format(mold.getPreventOverdue()));
        maintenanceInformationDto.setCycleTimeToleranceL1(
                (mold.getCycleTimeLimit1() == null ? "" : mold.getCycleTimeLimit1()) + (mold.getCycleTimeLimit1Unit() != null ? mold.getCycleTimeLimit1Unit().getTitle() : "%")
                + " ~ " + (mold.getCycleTimeLimit2() == null ? "" : mold.getCycleTimeLimit2()) + (mold.getCycleTimeLimit2Unit() != null ? mold.getCycleTimeLimit2Unit().getTitle() : "%"));
        maintenanceInformationDto.setCycleTimeToleranceL2(mold.getCycleTimeLimit2() + (mold.getCycleTimeLimit2Unit() != null ? mold.getCycleTimeLimit2Unit().getTitle() : "%"));
        maintenanceInformationDto.setStatus(mold.getUseageTypeName());

        String engineerInCharge = "";
        List<String> engineerNames = mold.getEngineerNames();
        for(int i = 0; i < engineerNames.size(); i++){
            if(i == engineerNames.size() - 1)
                engineerInCharge += engineerNames.get(i);
            else
                engineerInCharge += engineerNames.get(i) + ", ";
        }
        maintenanceInformationDto.setEngineerInCharge(engineerInCharge);
    }

    public List<PartDto> getPartDtos(Mold mold){
        List<PartDto> partDtos = new ArrayList<>();
        mold.getParts().forEach(partData -> {
/*
            PartDto partDto = new PartDto();
            partDto.setCategory(partData.getCategoryName());
            partDto.setProject(partData.getProjectName());
            partDto.setPartID(partData.getPartCode());
            partDto.setPartName(partData.getPartName());
            partDto.setPartResinCode(partData.getResinCode());
            partDto.setPartResinGrade(partData.getResinGrade());
            partDto.setDesignRevisionLevel(partData.getDesignRevision());
            partDto.setPartVolumn(partData.getPartVolume());
            partDto.setPartWeight(partData.getPartWeight());
            partDto.setNumberOfCavities(String.valueOf(partData.getCavity()));
            partDtos.add(partDto);
*/
            partDtos.add(convertToDto(partData));

        });
        return partDtos;
    }

    public List<PartDto> getPartDtos(MoldEndLifeCycle mmm){
        List<PartDto> partDtos = new ArrayList<>();
        mmm.getMold().getParts().forEach(partData -> {
/*
            PartDto partDto = new PartDto();
            partDto.setCategory(partData.getCategoryName());
            partDto.setProject(partData.getProjectName());
            partDto.setPartID(partData.getPartCode());
            partDto.setPartName(partData.getPartName());
            partDto.setPartResinCode(partData.getResinCode());
            partDto.setPartResinGrade(partData.getResinGrade());
            partDto.setDesignRevisionLevel(partData.getDesignRevision());
            partDto.setPartVolumn(partData.getPartVolume());
            partDto.setPartWeight(partData.getPartWeight());
            partDto.setNumberOfCavities(String.valueOf(partData.getCavity()));
            partDtos.add(partDto);
*/
            partDtos.add(convertToDto(partData));

        });
        return partDtos;
    }
    public PartDto convertToDto(PartData partData){
        PartDto partDto = new PartDto();
        partDto.setId(partData.getPartId());
        partDto.setCategory(partData.getCategoryName());
        partDto.setProject(partData.getProjectName());
        partDto.setPartID(partData.getPartCode());
        partDto.setPartName(partData.getPartName());
        partDto.setPartResinCode(partData.getResinCode());
        partDto.setPartResinGrade(partData.getResinGrade());
        partDto.setDesignRevisionLevel(partData.getDesignRevision());
        partDto.setPartVolumn(partData.getPartVolume());
        partDto.setPartWeight(partData.getPartWeight());
        String working_total_cavities=partData.getCavity() != null ? String.valueOf(partData.getCavity()) : "";
        if(partData.getCavity()!=null || partData.getTotalCavities()!=null) working_total_cavities+=" / ";
        if(partData.getTotalCavities()!=null){
            working_total_cavities+= partData.getTotalCavities();
        }
        partDto.setNumberOfCavities(working_total_cavities);
        partDto.setWeeklyDemand(partData.getWeeklyDemand() != null ? String.valueOf(partData.getWeeklyDemand()) : "");
        partDto.setQuantityRequired(partData.getQuantityRequired() != null ? String.valueOf(partData.getQuantityRequired()) : "");
        return partDto;
    }

    public void bindData(Mold mold, PhysicalInformationDto physicalInformationDto){
        String sizeUnit = mold.getSizeUnitTitle();
//        if(sizeUnit.equalsIgnoreCase("m") || sizeUnit.equalsIgnoreCase("mm") || sizeUnit.equalsIgnoreCase("cm"))
//            sizeUnit += "³";
//        physicalInformationDto.setToolSize(mold.getSize() != null && !mold.getSize().equalsIgnoreCase("xx") ? mold.getSize() + " " + sizeUnit : "");
        physicalInformationDto.setToolSize(mold.getToolingSizeView());
//        physicalInformationDto.setToolWeight(mold.getWeight() != null && !mold.getWeight().equalsIgnoreCase("") ? mold.getWeight() + " " + mold.getWeightUnitTitle() : "");
        physicalInformationDto.setToolWeight(mold.getToolingWeightView());
        physicalInformationDto.setShotSize(mold.getShotSize() != null ? mold.getShotSize() + " gram" : "");
        physicalInformationDto.setToolingMaker(mold.getToolMakerCompanyName());
        physicalInformationDto.setInjectionMoldingMachineID(mold.getInjectionMachineId());
        physicalInformationDto.setMachineTonnageQuote(mold.getQuotedMachineTonnage() != null ? mold.getQuotedMachineTonnage() + " ton" : "");
        physicalInformationDto.setMachineTonnageCurrentProduction(mold.getCurrentMachineTonnage() != null ? mold.getCurrentMachineTonnage() + " ton" : "");
        physicalInformationDto.setTotalCavities(mold.getTotalCavities() != null ? mold.getTotalCavities().toString() : "");
    }

    public void bindData(Mold mold, RunnerSystemInformationDto runnerSystemInformationDto){
        runnerSystemInformationDto.setTypeOfRunnerSystem(mold.getRunnerTypeTitle());
        runnerSystemInformationDto.setMakerOfRunnerSystem(mold.getRunnerMaker());
        runnerSystemInformationDto.setWeightOfRunnerSystem(mold.getWeightRunner() != null ? mold.getWeightRunner() + " gram" : "");
        runnerSystemInformationDto.setHotRunnerNumberOfDrop(mold.getHotRunnerDrop());
        runnerSystemInformationDto.setHotRunnerZone(mold.getHotRunnerZone());
    }

    public void bindData(Mold mold, CostInformationDto costInformationDtoInformationDto, Integer timezoneOffsetClient){
        DecimalFormat formatter = new DecimalFormat("#,###");
        String currencyType = mold.getCostCurrencyTypeTitle() != null ? mold.getCostCurrencyTypeTitle() : USD.getTitle();
        costInformationDtoInformationDto.setCostOfTooling(mold.getCost() != null ? currencyType + formatter.format(mold.getCost()) : "");
        costInformationDtoInformationDto.setAccumulatedMaintenanceCost(mold.getAccumulatedMaintenanceCost() != null ? currencyType+formatter.format(mold.getAccumulatedMaintenanceCost()).toString() : "");
        costInformationDtoInformationDto.setSalvageValue(getNumberWithCurrency(mold.getSalvageValue(), mold.getCostCurrencyType()));
        String poDate= DateTimeUtils.formatDateTimeWithTimeZone(mold.getPoDate(),timezoneOffsetClient, DateUtils2.DatePattern.yyyy_MM_dd);
        costInformationDtoInformationDto.setPoDate(poDate);
        costInformationDtoInformationDto.setPoNumber(StringUtils.defaultString(mold.getPoNumber()));
        costInformationDtoInformationDto.setMemo(mold.getMemo() != null ? mold.getMemo() : "");
    }

    public void bindData(Mold mold, SupplierInformationDto supplierInformationDto){
        DecimalFormat formatter = new DecimalFormat("#,###");
        supplierInformationDto.setTargetUptime(mold.getUptimeTarget() != null ? mold.getUptimeTarget() + "%" : "");
        supplierInformationDto.setUptimeToleranceL1((mold.getUptimeLimitL1() != null ? mold.getUptimeLimitL1() + "%" : "")
                + (mold.getUptimeLimitL2() != null ? " ~ " + mold.getUptimeLimitL2() + "%" : ""));
        supplierInformationDto.setUptimeToleranceL2(mold.getUptimeLimitL2() != null ? mold.getUptimeLimitL2() + "%" : "");
        supplierInformationDto.setSupplierName(mold.getSupplierCompanyName());
        supplierInformationDto.setSupplierCode(mold.getSupplierCompanyCode());
        supplierInformationDto.setLabour(mold.getLabour());
        supplierInformationDto.setProductionHoursPerDays(mold.getShiftsPerDay());
        supplierInformationDto.setProductionDayPerWeek(mold.getProductionDays());
        supplierInformationDto.setMaximumCapacityPerWeek(mold.getMaxCapacityPerWeek() != null ? formatter.format(mold.getMaxCapacityPerWeek()) : "");
    }
    public void bindData(Mold mold, ProductionInformationDTO productionInformationDTO){
        DecimalFormat formatter = new DecimalFormat("#,###");
        productionInformationDTO.setTargetUptime(mold.getUptimeTarget() != null ? mold.getUptimeTarget() + "%" : "");
        productionInformationDTO.setUptimeToleranceL1((mold.getUptimeLimitL1() != null ? mold.getUptimeLimitL1() + "%" : "")
                + (mold.getUptimeLimitL2() != null ? " ~ " + mold.getUptimeLimitL2() + "%" : ""));
        productionInformationDTO.setUptimeToleranceL2(mold.getUptimeLimitL2() != null ? mold.getUptimeLimitL2() + "%" : "");
        if (mold.getContractedCycleTimeSeconds() != null) {
            productionInformationDTO.setApprovedCycleTime(mold.getContractedCycleTimeSeconds() + "s");
        }
        if(mold.getToolmakerContractedCycleTimeSeconds() != null) {
            productionInformationDTO.setToolmakerApprovedCycleTime(mold.getToolmakerContractedCycleTimeSeconds() + "s");
        }

        productionInformationDTO.setCycleTimeToleranceL1(
                (mold.getCycleTimeLimit1() == null ? "" : mold.getCycleTimeLimit1()) + (mold.getCycleTimeLimit1Unit() != null ? mold.getCycleTimeLimit1Unit().getTitle() : "%")
                        + " ~ " + (mold.getCycleTimeLimit2() == null ? "" : mold.getCycleTimeLimit2()) + (mold.getCycleTimeLimit2Unit() != null ? mold.getCycleTimeLimit2Unit().getTitle() : "%"));
        productionInformationDTO.setCycleTimeToleranceL2(mold.getCycleTimeLimit2() + (mold.getCycleTimeLimit2Unit() != null ? mold.getCycleTimeLimit2Unit().getTitle() : "%"));


    }

    public static String getNumberWithCurrency(Double value, CurrencyType costCurrencyType) {
        String currencyType = costCurrencyType != null ? costCurrencyType.getTitle() : USD.getTitle();
        return value != null ? currencyType + formatter.format(value) : "";
    }
}
