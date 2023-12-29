package saleson.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.emoldino.framework.util.BeanUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.object.repository.customfield.CustomFieldRepository;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;

import lombok.extern.slf4j.Slf4j;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.configuration.payload.GeneralConfigPayload;
import saleson.api.workOrder.WorkOrderRepository;
import saleson.api.workOrder.WorkOrderService;
import saleson.common.config.Const;
import saleson.common.config.Const.Column.tooling;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.enumeration.customField.PropertyGroup;
import saleson.common.enumeration.mapper.Code;
import saleson.common.enumeration.mapper.CodeMapper;
import saleson.common.service.ExportService;
import saleson.dto.HeaderDTO;
import saleson.dto.WorkOrderDTO;
import saleson.model.*;
import saleson.model.config.GeneralConfig;
import saleson.model.customField.CustomField;
import saleson.model.customField.CustomFieldValue;
import saleson.restdocs.dto.exports.BasicInformationDto;
import saleson.restdocs.dto.exports.CostInformationDto;
import saleson.restdocs.dto.exports.DynamicInformationDto;
import saleson.restdocs.dto.exports.MaintenanceInformationDto;
import saleson.restdocs.dto.exports.MoldEndLifeCycleInformationDto;
import saleson.restdocs.dto.exports.PartDto;
import saleson.restdocs.dto.exports.PhysicalInformationDto;
import saleson.restdocs.dto.exports.ProductionInformationDTO;
import saleson.restdocs.dto.exports.RunnerSystemInformationDto;
import saleson.restdocs.dto.exports.SlDepreciationDto;
import saleson.restdocs.dto.exports.SupplierInformationDto;
import saleson.restdocs.dto.exports.UpDepreciationDto;
import saleson.service.util.DateTimeUtils;

@Slf4j
@Service
public class ExcelUtils {

    @Autowired
    ExportService exportService;

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${customer.server.name}")
    private String customerServerName;
    @Autowired
    GeneralConfigService generalConfigService;
    @Autowired
    CustomFieldRepository customFieldRepository;
    @Autowired
    CodeMapper codeMapper;
    @Autowired
    CustomFieldValueService customFieldValueService;

    public static InputStream getFileTemplateExcel(String fileName){
        try {
//            return ResourceUtils.getFile("classpath:exports/excels/" + fileName);
            DefaultResourceLoader loader = new DefaultResourceLoader();
            Resource storeFile = loader.getResource("classpath:exports/excels/" + fileName);
            InputStream fileInputStream = storeFile.getInputStream();
//            return storeFile.getFile();
            return fileInputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.error("Cant file template excel !");
        return null;
    }


//    public static void main(String[] args) {
//        new ExcelUtils().exportExcelToolingDetail(new ArrayList<>());
//    }


/*
    private int createCellLabelPart(Sheet sheet, int startRow, CellStyle cellStyle){

        Row row = sheet.getRow(1);
        row.setRowStyle(cellStyle);
        sheet.setColumnWidth(startRow, 4000);
        Cell category = row.createCell(startRow++);
        category.setCellValue("Category");
        category.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 4000);
        Cell project = row.createCell(startRow++);
        project.setCellValue("Product");
        project.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 4000);
        Cell partID = row.createCell(startRow++);
        partID.setCellValue("Part ID");
        partID.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 5000);
        Cell partName = row.createCell(startRow++);
        partName.setCellValue("Part Name");
        partName.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 5000);
        Cell partResinCode = row.createCell(startRow++);
        partResinCode.setCellValue("Part Resin Code");
        partResinCode.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 5000);
        Cell partResinGrade = row.createCell(startRow++);
        partResinGrade.setCellValue("Part Resin Grade");
        partResinGrade.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 6500);
        Cell designRevisionLevel = row.createCell(startRow++);
        designRevisionLevel.setCellValue("Design Revision Level");
        designRevisionLevel.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 5000);
        Cell partVolume = row.createCell(startRow++);
        partVolume.setCellValue("Part Volume");
        partVolume.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 4000);
        Cell partWeight = row.createCell(startRow++);
        partWeight.setCellValue("Part Weight");
        partWeight.setCellStyle(cellStyle);

        sheet.setColumnWidth(startRow, 5500);
        Cell numberOfCavities = row.createCell(startRow++);
        numberOfCavities.setCellValue("Number of Cavities");
        numberOfCavities.setCellStyle(cellStyle);

        return startRow;
    }
*/

    @SuppressWarnings("Duplicates")
    private CellStyle createCellStyleForLabelPart(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);

        cellStyle.setWrapText(true);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);
        return cellStyle;
    }

    @SuppressWarnings("Duplicates")
    private CellStyle createCellStyleValue(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setWrapText(true);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);
        return cellStyle;
    }

    @SuppressWarnings("Duplicates")
    private CellStyle createCellStyleValueTopLeft(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setWrapText(true);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);
        return cellStyle;
    }

/*
    private int importDataPart(Part part, int indexCell, Row row, CellStyle style, Workbook workbook){
        Cell cellTooling = row.createCell(indexCell++);
        cellTooling.setCellValue(part.getPartCode());
        cellTooling.setCellStyle(style);

        Cell counterID = row.createCell(indexCell++);
        counterID.setCellValue(part.getName());
        counterID.setCellStyle(style);

        Cell cellToolingLetter = row.createCell(indexCell++);
        cellToolingLetter.setCellValue(part.getCategoryName());
        cellToolingLetter.setCellStyle(style);

        Cell toolingType = row.createCell(indexCell++);
        toolingType.setCellValue(part.getProjectName());
        toolingType.setCellStyle(style);

        Cell toolingComplexity = row.createCell(indexCell++);
        toolingComplexity.setCellValue(part.getMoldCount());
        toolingComplexity.setCellStyle(style);

        Cell familyTool = row.createCell(indexCell++);
        familyTool.setCellValue(part.getActiveMolds());
        familyTool.setCellStyle(style);

        Cell forecastedMaxShots = row.createCell(indexCell++);
        forecastedMaxShots.setCellValue(part.getIdleMolds());
        forecastedMaxShots.setCellStyle(style);

        Cell yearOfToolMade = row.createCell(indexCell++);
        yearOfToolMade.setCellValue(part.getInactiveMolds());
        yearOfToolMade.setCellStyle(style);

        Cell approvedCycleTime = row.createCell(indexCell++);
        approvedCycleTime.setCellValue(part.getDisconnectedMolds());
        approvedCycleTime.setCellStyle(style);

        Cell toolDescription = row.createCell(indexCell++);
        toolDescription.setCellValue(part.getTotalProduced());
        toolDescription.setCellStyle(style);

        return indexCell;
    }
*/

/*
    private int importDataBasicInformation(BasicInformationDto basicInformationDto, int indexCell, Row row, CellStyle style, Workbook workbook, boolean isCustomServerName){
        Cell cellTooling = row.createCell(indexCell++);
        cellTooling.setCellValue(basicInformationDto.getToolingId());
        cellTooling.setCellStyle(style);

        Cell cellToolingLetter = row.createCell(indexCell++);
        cellToolingLetter.setCellValue(basicInformationDto.getToolingLetter());
        cellToolingLetter.setCellStyle(style);

        Cell toolingType = row.createCell(indexCell++);
        toolingType.setCellValue(basicInformationDto.getToolingType());
        toolingType.setCellStyle(style);

        Cell toolingComplexity = row.createCell(indexCell++);
        toolingComplexity.setCellValue(basicInformationDto.getToolingComplexity());
        toolingComplexity.setCellStyle(style);

        Cell counterID = row.createCell(indexCell++);
        counterID.setCellValue(basicInformationDto.getCounterId());
        counterID.setCellStyle(style);


        Cell forecastedMaxShots = row.createCell(indexCell++);
        forecastedMaxShots.setCellValue(basicInformationDto.getForecastedMaxShots());
        forecastedMaxShots.setCellStyle(style);



        Cell yearOfToolMade = row.createCell(indexCell++);
        yearOfToolMade.setCellValue(basicInformationDto.getYeaOfToolMade());
        yearOfToolMade.setCellStyle(style);

        Cell approvedCycleTime = row.createCell(indexCell++);
        approvedCycleTime.setCellValue(basicInformationDto.getApprovedCycleTime());
        approvedCycleTime.setCellStyle(style);

        Cell toolDescription = row.createCell(indexCell++);
        toolDescription.setCellValue(basicInformationDto.getToolDescription());

        toolDescription.setCellStyle(createCellStyleValueTopLeft(workbook));

        return indexCell;
    }
*/

    public ByteArrayOutputStream exportExcelLoginAuditTrail(List<User> users){

        InputStream  fileTemplate;

        fileTemplate = getFileTemplateExcel("user_export_template.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 2;
            int index = 1;
            int indexCell = 0;

            CellStyle styleOfValue = createCellStyleValue(workbook);


            for(User user : users){

                Row row = sheet.createRow(startRow);
                Cell cell = row.createCell(indexCell ++);
                cell.setCellValue(index);
                cell.setCellStyle(styleOfValue);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String name = user.getName() != null ? user.getName(): "";
                cell.setCellValue(name);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String email = user.getEmail() != null ? user.getEmail(): "";
                cell.setCellValue(email);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String company = user.getCompany().getName() != null ? user.getCompany().getName(): "";
                cell.setCellValue(company);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String lastLogin = DateUtils2.format(user.getLastLogin(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS) != null ? DateUtils2.format(user.getLastLogin(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS) : "";

                cell.setCellValue(lastLogin);

                indexCell = 0;
                startRow++;
                index++;
            }

            workbook.write(outputStream);
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream;
    }

    private int importDataPhysicalInformation(PhysicalInformationDto physicalInformationDto, int indexCell, Row row, CellStyle style
            , List<String> deleteProperties) {
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.size)) {
            Cell toolSize = row.createCell(indexCell++);
            toolSize.setCellValue(physicalInformationDto.getToolSize());
            toolSize.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.weight)) {
            Cell toolWeight = row.createCell(indexCell++);
            toolWeight.setCellValue(physicalInformationDto.getToolWeight());
            toolWeight.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.shotSize)) {
            Cell shotWeight = row.createCell(indexCell++);
            shotWeight.setCellValue(physicalInformationDto.getShotSize());
            shotWeight.setCellStyle(style);
        }
//        Cell typeOfRunnerSystem = row.createCell(indexCell++);
////        typeOfRunnerSystem.setCellValue(physicalInformationDto.getTypeOfRunnerSystem());
//        typeOfRunnerSystem.setCellStyle(style);
//
//        Cell makerOfRunnerSystem = row.createCell(indexCell++);
////        makerOfRunnerSystem.setCellValue(physicalInformationDto.getMakerOfRunnerSystem());
//        makerOfRunnerSystem.setCellStyle(style);
//
//        Cell weightOfRunnerSystem  = row.createCell(indexCell++);
////        weightOfRunnerSystem.setCellValue(physicalInformationDto.getWeightOfRunnerSystem());
//        weightOfRunnerSystem.setCellStyle(style);

        if (!deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.toolMakerCompanyName)) {
            Cell toolMaker = row.createCell(indexCell++);
            toolMaker.setCellValue(physicalInformationDto.getToolingMaker());
            toolMaker.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.injectionMachineId)) {
            Cell injectionModlingMachineID = row.createCell(indexCell++);
            injectionModlingMachineID.setCellValue(physicalInformationDto.getInjectionMoldingMachineID());
            injectionModlingMachineID.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.quotedMachineTonnage)) {
            Cell machineTonnageQuoted = row.createCell(indexCell++);
            machineTonnageQuoted.setCellValue(physicalInformationDto.getMachineTonnageQuote());
            machineTonnageQuoted.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.totalCavities)) {
            Cell totalCavities = row.createCell(indexCell++);
            totalCavities.setCellValue(physicalInformationDto.getTotalCavities());
            totalCavities.setCellStyle(style);
        }

//        Cell machineTonnageCurrentProduction = row.createCell(indexCell++);
//        machineTonnageCurrentProduction.setCellValue(physicalInformationDto.getMachineTonnageCurrentProduction());
//        machineTonnageCurrentProduction.setCellStyle(style);

        return indexCell;
    }

    private int importDataRunnerSystem(RunnerSystemInformationDto runnerSystemInformationDto, int indexCell, Row row, CellStyle style
            , List<String> deleteProperties){
        if (!deleteProperties.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.runnerType)) {
            Cell typeOfRunnerSystem = row.createCell(indexCell++);
            typeOfRunnerSystem.setCellValue(runnerSystemInformationDto.getTypeOfRunnerSystem());
            typeOfRunnerSystem.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.runnerMaker)) {
            Cell makerOfRunnerSystem = row.createCell(indexCell++);
            makerOfRunnerSystem.setCellValue(runnerSystemInformationDto.getMakerOfRunnerSystem());
            makerOfRunnerSystem.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.weightRunner)) {
            Cell weightOfRunnerSystem  = row.createCell(indexCell++);
            weightOfRunnerSystem.setCellValue(runnerSystemInformationDto.getWeightOfRunnerSystem());
            weightOfRunnerSystem.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.hotRunnerDrop)) {
            Cell hotRunnerNumberOfDrop  = row.createCell(indexCell++);
            hotRunnerNumberOfDrop.setCellValue(runnerSystemInformationDto.getHotRunnerNumberOfDrop());
            hotRunnerNumberOfDrop.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.hotRunnerZone)) {
            Cell hotRunnerZone = row.createCell(indexCell++);
            hotRunnerZone.setCellValue(runnerSystemInformationDto.getHotRunnerZone());
            hotRunnerZone.setCellStyle(style);
        }
        return indexCell;
    }

    private int importDataDynamicInformation(DynamicInformationDto dynamicInformationDto, int indexCell, Row row, CellStyle style
            , List<String> deleteProperties){

        Cell op = row.createCell(indexCell++);
        op.setCellValue(dynamicInformationDto.getOp());
        op.setCellStyle(style);

        Cell numberOfShots = row.createCell(indexCell++);
        numberOfShots.setCellValue(dynamicInformationDto.getNoOfShots());
        numberOfShots.setCellStyle(style);

        Cell lastDateOfShots = row.createCell(indexCell++);
        lastDateOfShots.setCellValue(dynamicInformationDto.getLastDateOfShots());
        lastDateOfShots.setCellStyle(style);

        Cell utilisationRate = row.createCell(indexCell++);
        utilisationRate.setCellValue(dynamicInformationDto.getUtilisationRate());
        utilisationRate.setCellStyle(style);

        Cell p = row.createCell(indexCell++);
        p.setCellValue(dynamicInformationDto.getRemainingPartsCount());
        p.setCellStyle(style);

        Cell location = row.createCell(indexCell++);
        location.setCellValue(dynamicInformationDto.getLocation());
        location.setCellStyle(style);

        Cell cycleTime = row.createCell(indexCell++);
        cycleTime.setCellValue(dynamicInformationDto.getCycleTime());
        cycleTime.setCellStyle(style);

        Cell wact = row.createCell(indexCell++);
        wact.setCellValue(dynamicInformationDto.getWeightedAverageCycleTime());
        wact.setCellStyle(style);

        return indexCell;
    }


/*
    private int importMoldEndLifeInfromation(MoldEndLifeCycleInformationDto moldEndLifeCycleInformationDto, int indexCell, Row row, CellStyle style){
        Cell op = row.createCell(indexCell++);
        op.setCellValue(moldEndLifeCycleInformationDto.getPriority());
        op.setCellStyle(style);

        Cell numberOfShots = row.createCell(indexCell++);
        numberOfShots.setCellValue(moldEndLifeCycleInformationDto.getInsights());
        numberOfShots.setCellStyle(style);

        return indexCell;
    }
*/

    private int importDataCostInformation(CostInformationDto costInformationDto, int indexCell, Row row, CellStyle style
            , List<String> deleteProperties){
        if (!deleteProperties.contains(Const.ColumnCode.tooling.COST.cost)) {
            Cell costOfToolingInUSD = row.createCell(indexCell++);
            costOfToolingInUSD.setCellValue(costInformationDto.getCostOfTooling());
            costOfToolingInUSD.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.COST.accumulatedMaintenanceCost)) {
            Cell accumulatedMaintenanceCost = row.createCell(indexCell++);
            accumulatedMaintenanceCost.setCellValue(costInformationDto.getAccumulatedMaintenanceCost());
            accumulatedMaintenanceCost.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.COST.salvageValue))
            ExcelCommonUtils.bindDataToCell(costInformationDto.getSalvageValue(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.COST.poDate))
            ExcelCommonUtils.bindDataToCell(costInformationDto.getPoDate(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.COST.poNumber))
            ExcelCommonUtils.bindDataToCell(costInformationDto.getPoNumber(), row, indexCell++, style);

        if (!deleteProperties.contains(Const.ColumnCode.tooling.COST.memo)) {
            Cell memo = row.createCell(indexCell++);
            memo.setCellValue(costInformationDto.getMemo());
            memo.setCellStyle(style);
        }
        return indexCell;
    }

    private int exportDataSlDepreciation(SlDepreciationDto dto, int indexCell, Row row, CellStyle style
        , List<String> deleteProperties) {
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SL_DEPRECIATION.slCurrentBookValue))
            ExcelCommonUtils.bindDataToCell(dto.getSlCurrentBookValue(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SL_DEPRECIATION.slDepreciation))
            ExcelCommonUtils.bindDataToCell(dto.getSlDepreciation(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SL_DEPRECIATION.slDepreciationTerm))
            ExcelCommonUtils.bindDataToCell(dto.getSlDepreciationTerm(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SL_DEPRECIATION.slYearlyDepreciation))
            ExcelCommonUtils.bindDataToCell(dto.getSlYearlyDepreciation(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SL_DEPRECIATION.slLatestDepreciationPoint))
            ExcelCommonUtils.bindDataToCell(dto.getSlLatestDepreciationPoint(), row, indexCell++, style);
        return indexCell;
    }

    private int exportDataUpDepreciation(UpDepreciationDto dto, int indexCell, Row row, CellStyle style
        , List<String> deleteProperties) {
        if (!deleteProperties.contains(Const.ColumnCode.tooling.UP_DEPRECIATION.upCurrentBookValue))
            ExcelCommonUtils.bindDataToCell(dto.getUpCurrentBookValue(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.UP_DEPRECIATION.depreciationPercentage))
            ExcelCommonUtils.bindDataToCell(dto.getDepreciationPercentage(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.UP_DEPRECIATION.upDepreciationTerm))
            ExcelCommonUtils.bindDataToCell(dto.getUpDepreciationTerm(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.UP_DEPRECIATION.upDepreciationPerShot))
            ExcelCommonUtils.bindDataToCell(dto.getUpDepreciationPerShot(), row, indexCell++, style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.UP_DEPRECIATION.upLatestDepreciationPoint))
            ExcelCommonUtils.bindDataToCell(dto.getUpLatestDepreciationPoint(), row, indexCell++, style);
        return indexCell;
    }

/*
    private int importDataMaintenanceInformation(MaintenanceInformationDto maintenanceInformationDto, int indexCell, Row row, CellStyle style, Workbook workbook, boolean isCustomServerName){
        Cell conditionOfTooling = row.createCell(indexCell++);
        conditionOfTooling.setCellValue(maintenanceInformationDto.getConditionOfTooling());
        conditionOfTooling.setCellStyle(style);

        Cell maintenanceInterval = row.createCell(indexCell++);
        maintenanceInterval.setCellValue(maintenanceInformationDto.getMaintenanceInterval());
        maintenanceInterval.setCellStyle(style);

        Cell upcomingMaintenanceTolerance = row.createCell(indexCell++);
        upcomingMaintenanceTolerance.setCellValue(maintenanceInformationDto.getUpcomingMaintenanceTolerance());
        upcomingMaintenanceTolerance.setCellStyle(style);

        Cell overdueMaintenanceTolerance = row.createCell(indexCell++);
        overdueMaintenanceTolerance.setCellValue(maintenanceInformationDto.getOverdueMaintenanceTolerance());
        overdueMaintenanceTolerance.setCellStyle(style);

        Cell cycleTimeToleranceL1  = row.createCell(indexCell++);
        cycleTimeToleranceL1.setCellValue(maintenanceInformationDto.getCycleTimeToleranceL1());
        cycleTimeToleranceL1.setCellStyle(style);

        Cell cycleTimeToleranceL2  = row.createCell(indexCell++);
        cycleTimeToleranceL2.setCellValue(maintenanceInformationDto.getCycleTimeToleranceL2());
        cycleTimeToleranceL2.setCellStyle(style);


        Cell engineerInCharge = row.createCell(indexCell++);
        engineerInCharge.setCellValue(maintenanceInformationDto.getEngineerInCharge());

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);
        engineerInCharge.setCellStyle(cellStyle);

        Cell maintenanceDocument = row.createCell(indexCell++);
        maintenanceDocument.setCellValue(maintenanceInformationDto.getMaintenanceDocument());
        maintenanceDocument.setCellStyle(style);

        Cell instructionVideo = row.createCell(indexCell++);
        instructionVideo.setCellValue(maintenanceInformationDto.getInstructionVideo());
        instructionVideo.setCellStyle(style);

        return indexCell;

    }

    private int importDataSupplierInformation(SupplierInformationDto supplierInformationDto, int indexCell, Row row, CellStyle style){
        Cell supplier = row.createCell(indexCell++);
        supplier.setCellValue(supplierInformationDto.getSupplierName());
        supplier.setCellStyle(style);

        Cell uptimeTarget = row.createCell(indexCell++);
        uptimeTarget.setCellValue(supplierInformationDto.getTargetUptime());
        uptimeTarget.setCellStyle(style);

        Cell uptimeToleranceL1 = row.createCell(indexCell++);
        uptimeToleranceL1.setCellValue(supplierInformationDto.getUptimeToleranceL1());
        uptimeToleranceL1.setCellStyle(style);

        Cell uptimeToleranceL2 = row.createCell(indexCell++);
        uptimeToleranceL2.setCellValue(supplierInformationDto.getUptimeToleranceL2());
        uptimeToleranceL2.setCellStyle(style);

        Cell labour = row.createCell(indexCell++);
        labour.setCellValue(supplierInformationDto.getLabour());
        labour.setCellStyle(style);

        Cell productionHoursPerDay = row.createCell(indexCell++);
        productionHoursPerDay.setCellValue(supplierInformationDto.getProductionHoursPerDays());
        productionHoursPerDay.setCellStyle(style);

        Cell productionDayPerWeek = row.createCell(indexCell++);
        productionDayPerWeek.setCellValue(supplierInformationDto.getProductionDayPerWeek());
        productionDayPerWeek.setCellStyle(style);

        Cell maximumCapacityPerWeek = row.createCell(indexCell++);
        maximumCapacityPerWeek.setCellValue(supplierInformationDto.getMaximumCapacityPerWeek());
        maximumCapacityPerWeek.setCellStyle(style);

        return indexCell;

    }

    private int importDataPart(PartDto partDto, int indexCell, Row row, CellStyle style){
        Cell category = row.createCell(indexCell++);
        category.setCellValue(partDto.getCategory());
        category.setCellStyle(style);

        Cell project = row.createCell(indexCell++);
        project.setCellValue(partDto.getProject());
        project.setCellStyle(style);

        Cell partID = row.createCell(indexCell++);
        partID.setCellValue(partDto.getPartID());
        partID.setCellStyle(style);

        Cell partName = row.createCell(indexCell++);
        partName.setCellValue(partDto.getPartName());
        partName.setCellStyle(style);

        Cell partResinCode = row.createCell(indexCell++);
        partResinCode.setCellValue(partDto.getPartResinCode());
        partResinCode.setCellStyle(style);

        Cell partResinGrade = row.createCell(indexCell++);
        partResinGrade.setCellValue(partDto.getPartResinGrade());
        partResinGrade.setCellStyle(style);

        Cell designRevisionLevel = row.createCell(indexCell++);
        designRevisionLevel.setCellValue(partDto.getDesignRevisionLevel());
        designRevisionLevel.setCellStyle(style);

        Cell partVolume = row.createCell(indexCell++);
        partVolume.setCellValue(partDto.getPartVolumn());
        partVolume.setCellStyle(style);

        Cell partWeight = row.createCell(indexCell++);
        partWeight.setCellValue(partDto.getPartWeight());
        partWeight.setCellStyle(style);

        Cell numberOfCavities = row.createCell(indexCell++);
        numberOfCavities.setCellValue(partDto.getNumberOfCavities());
        numberOfCavities.setCellStyle(style);

        return indexCell;

    }
*/

/*
    public ByteArrayOutputStream exportExcelPartList(List<Part> parts, String timeRange){
        InputStream fileTemplate = getFileTemplateExcel("part-list-template.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
            final int[] startRow = {2};
            final int[] index = {1};
            final int[] indexCell = {0};

            CellStyle styleOfLabel = createCellStyleForLabelPart(workbook);
            CellStyle styleOfValue = createCellStyleValue(workbook);

            // Create time range header
            Row aRow = sheet.createRow(0);
            Cell aCell = aRow.createCell(0);
            aCell.setCellValue("Time: " + timeRange);
            aCell.setCellStyle(styleOfLabel);

            parts.forEach(part -> {
                Row row = sheet.createRow(startRow[0]);
                Cell cell = row.createCell(indexCell[0]++);
                cell.setCellValue(index[0]);
                cell.setCellStyle(styleOfValue);

                importDataPart(part, indexCell[0], row, styleOfValue, workbook);

                indexCell[0] = 0;
                startRow[0]++;
                index[0]++;
            });

            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream;
    }
*/
/*

    public ByteArrayOutputStream exportExcelToolingDetail(List<Mold> molds){

        boolean isCustomServerName = "dyson".equals(this.customerServerName);
        InputStream  fileTemplate;

        if(!isCustomServerName){
//            fileTemplate = getFileTemplateExcel("tooling-detail-template.xlsx");
//            fileTemplate = getFileTemplateExcel("tooling-detail-template-update-cost.xlsx");
            fileTemplate = getFileTemplateExcel("tooling-detail-template-new.xlsx");
        }else{
//            fileTemplate = getFileTemplateExcel("tooling-detail-template-dyson.xlsx");
            fileTemplate = getFileTemplateExcel("tooling-detail-template-dyson-new.xlsx");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 2;
            int index = 1;
            int indexCell = 0;
            int numberOfPart = 0;
            int maxField = 0;
            boolean assignMaxField = false;

//            Default
            int indexOfBasicInformation = 1;
            int indexOfPhysicalInformationDto = 11;
            int indexOfRunnerSystem = -1;
            int indexOfDynamicInformationDto = 21;
            int indexOfCostInformation = -1;
            int indexOfMaintenanceInformationDto = 27;
            int indexOfSupplierInformationDto = 36;
            int indexOfPart = -1;

            BasicInformationDto basicInformationDto = new BasicInformationDto();
            PhysicalInformationDto physicalInformationDto = new PhysicalInformationDto();
            RunnerSystemInformationDto runnerSystemInformationDto = new RunnerSystemInformationDto();
            DynamicInformationDto dynamicInformationDto = new DynamicInformationDto();
            MaintenanceInformationDto maintenanceInformationDto = new MaintenanceInformationDto();
            SupplierInformationDto supplierInformationDto = new SupplierInformationDto();
            CostInformationDto costInformationDto = new CostInformationDto();
            List<PartDto> parts;
//          Fake data
//            costInformationDto = new CostInformationDto();
//            costInformationDto.setCostOfTooling("Input method: free entry");
//            costInformationDto.setAccumulatedMaintenanceCost("Calculated using the maintenance cost input by the supplier in Corrective Maintenance feature (see task #182)");
//            costInformationDto.setMemo("Input method: free entry ");

//            Style for label part
            CellStyle styleOfLabel = createCellStyleForLabelPart(workbook);
            CellStyle styleOfValue = createCellStyleValue(workbook);
            boolean assignPartNumber = false;

            for(Mold mold : molds){
                exportService.bindData(mold, basicInformationDto);
                exportService.bindData(mold, physicalInformationDto);
                exportService.bindData(mold, dynamicInformationDto,null);
                exportService.bindData(mold, maintenanceInformationDto);
                exportService.bindData(mold, supplierInformationDto);
                exportService.bindData(mold, runnerSystemInformationDto);
                exportService.bindData(mold, costInformationDto);
                parts = exportService.getPartDtos(mold);

                Row row = sheet.createRow(startRow);
                Cell cell = row.createCell(indexCell ++);
                cell.setCellValue(index);
                cell.setCellStyle(styleOfValue);
//                Basic information
                indexOfBasicInformation = indexCell;
                indexCell = importDataBasicInformation(basicInformationDto, indexCell, row, styleOfValue, workbook, isCustomServerName);
//                indexCell = importDataBasicInformation(basicInformationDto, indexCell, row, getStyleByColor(workbook, "000000"));

//                Physical Information
                indexOfPhysicalInformationDto = indexCell;
                indexCell = importDataPhysicalInformation(physicalInformationDto, indexCell, row, styleOfValue);

                indexOfRunnerSystem = indexCell;
                indexCell = importDataRunnerSystem(runnerSystemInformationDto, indexCell, row, styleOfValue);

//                Dynamic Information
                indexOfDynamicInformationDto = indexCell;
                indexCell = importDataDynamicInformation(dynamicInformationDto, indexCell, row, styleOfValue);
//                Maintenance Information
                indexOfMaintenanceInformationDto  = indexCell;
                indexCell = importDataMaintenanceInformation(maintenanceInformationDto, indexCell, row, styleOfValue, workbook,  isCustomServerName);
//                Cost Information
                if(!isCustomServerName) {
                    indexOfCostInformation = indexCell;
                    indexCell = importDataCostInformation(costInformationDto, indexCell, row, styleOfValue);
                }
//                Supplier Information
                indexOfSupplierInformationDto = indexCell;
                indexCell = importDataSupplierInformation(supplierInformationDto, indexCell,row, styleOfValue);
                indexOfPart = indexCell;
//                Count field use for create part label
                if(!assignMaxField){
                    maxField = indexCell;
                    assignMaxField = true;
                }

                for(PartDto partDto : parts) {
                    if(!assignPartNumber){
                        if(numberOfPart < parts.size()){
                            numberOfPart = parts.size();
                        }
                        assignPartNumber = true;
                    }
                    indexCell = importDataPart(partDto, indexCell, row, styleOfValue);
                }
                assignPartNumber = false;
                indexCell = 0;
                startRow++;
                index++;
            }
            int startPart = maxField;
            int startLabelPart = startPart;
            int totalFieldPart = 10;
            for(int count = 1; count <= numberOfPart; count++){
                createCellLabelPart(sheet, startLabelPart, styleOfLabel);
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(startPart);
                cell.setCellValue("Part #" + count);
                int endRow = startPart + totalFieldPart;
                sheet.addMergedRegion(new CellRangeAddress(0,0, startPart, endRow-1));
                startLabelPart = startLabelPart + totalFieldPart;
                startPart = endRow;
            }

            Row row = sheet.createRow(0);
            Cell basic = row.createCell(indexOfBasicInformation);
            basic.setCellValue("Basic Information");
            basic.setCellStyle(getStyleByColor(workbook, "C9DAF8"));

            Cell physical = row.createCell(indexOfPhysicalInformationDto);
            physical.setCellValue("Physical Information");
            physical.setCellStyle(getStyleByColor(workbook, "D9D9D9"));

            Cell runner = row.createCell(indexOfRunnerSystem);
            runner.setCellValue("Runner System Information");
            runner.setCellStyle(getStyleByColor(workbook, "E6B8AF"));

            Cell dynamic = row.createCell(indexOfDynamicInformationDto);
            dynamic.setCellValue("Dynamic Information");
            dynamic.setCellStyle(getStyleByColor(workbook, "D9EAD3"));

            Cell maintenance = row.createCell(indexOfMaintenanceInformationDto);
            maintenance.setCellValue("Maintenance Information");
            maintenance.setCellStyle(getStyleByColor(workbook, "FFF2CC"));
            if(!isCustomServerName) {
                Cell cost = row.createCell(indexOfCostInformation);
                cost.setCellValue("Cost Information");
                cost.setCellStyle(getStyleByColor(workbook, "D0E0E3"));
            }
            Cell supplier = row.createCell(indexOfSupplierInformationDto);
            supplier.setCellValue("Supplier Information");
            supplier.setCellStyle(getStyleByColor(workbook, "F4CCCC"));


            for(int countPart = 0; countPart < numberOfPart; countPart++){
                Cell part = row.createCell(indexOfPart);
                part.setCellValue("Part #" + (countPart + 1));
                part.setCellStyle(getStyleByColor(workbook, "D9D2E9"));
                indexOfPart += totalFieldPart;
            }

            workbook.write(outputStream);
//            FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/textExcel.xlsx"));
//            fileOutputStream.write(outputStream.toByteArray());
//            fileOutputStream.close();
            workbook.close();
        } catch (IOException | DecoderException e) {
            e.printStackTrace();
        }

        return outputStream;
    }
*/

    public ByteArrayOutputStream exportExcelToolingEndLifeCycleDetail(List<MoldEndLifeCycle> molds){

        boolean isCustomServerName = "dyson".equals(this.customerServerName);
        InputStream  fileTemplate=getFileTemplateExcel("insights_data_template.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 1;
            int index = 1;
            int indexCell = 0;
            int numberOfPart = 0;
            int maxField = 0;
            boolean assignMaxField = false;

//            Default
            int indexOfBasicInformation = 1;
            int indexOfPhysicalInformationDto = 11;
            int indexOfRunnerSystem = -1;
            int indexOfDynamicInformationDto = 21;
            int indexOfCostInformation = -1;
            int indexOfMaintenanceInformationDto = 27;
            int indexofMoldEndLifeCycleInformationDto = 36;
            int indexOfPart = -1;

            BasicInformationDto basicInformationDto = new BasicInformationDto();
            PhysicalInformationDto physicalInformationDto = new PhysicalInformationDto();
            RunnerSystemInformationDto runnerSystemInformationDto = new RunnerSystemInformationDto();
            DynamicInformationDto dynamicInformationDto = new DynamicInformationDto();
            MaintenanceInformationDto maintenanceInformationDto = new MaintenanceInformationDto();
            SupplierInformationDto supplierInformationDto = new SupplierInformationDto();
            CostInformationDto costInformationDto = new CostInformationDto();
            MoldEndLifeCycleInformationDto moldEndLifeCycleInformationDto = new MoldEndLifeCycleInformationDto();
            List<PartDto> parts;

//          Fake data
//            costInformationDto = new CostInformationDto();
//            costInformationDto.setCostOfTooling("Input method: free entry");
//            costInformationDto.setAccumulatedMaintenanceCost("Calculated using the maintenance cost input by the supplier in Corrective Maintenance feature (see task #182)");
//            costInformationDto.setMemo("Input method: free entry ");

//            Style for label part
            CellStyle styleOfLabel = createCellStyleForLabelPart(workbook);
            CellStyle styleOfValue = createCellStyleValue(workbook);
            boolean assignPartNumber = false;

            for(MoldEndLifeCycle mold : molds){

                Row row = sheet.createRow(startRow);
                Cell cell = row.createCell(indexCell ++);
                cell.setCellValue(index);
                cell.setCellStyle(styleOfValue);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String moldCode=mold.getMold() !=null? mold.getMold().getEquipmentCode():"";
                cell.setCellValue(moldCode);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String utilizationRate = "     "+mold.getMold().getLastShot() + "/" + mold.getMold().getDesignedShot() + " (" +  mold.getMold().getUtilizationRate() + "%)";
                cell.setCellValue(utilizationRate);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String insight = "Completed end of lifecycle on " + mold.getEndLifeAtDate();
                cell.setCellValue(insight);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String priority = mold.getPriority() != null ? mold.getPriority().toString().substring(0, 1).toUpperCase() + mold.getPriority().toString().substring(1).toLowerCase() : "";
                cell.setCellValue(priority);

                cell = ExcelCommonUtils.getOrCreateCell(row,indexCell++);
                cell.setCellStyle(styleOfValue);
                String status = mold.getStatus() != null ? mold.getStatus().toString() : "";

                if(status.equalsIgnoreCase("IN_COMMUNICATION")){
                    cell.setCellValue("In Communication");
                }
                if(status.equalsIgnoreCase("DISMISS")){
                    cell.setCellValue("Dismissed");
                }
                if(status.equalsIgnoreCase("RESOLVE")){
                    cell.setCellValue("Resolved");
                }




                indexCell = 0;
                startRow++;
                index++;
            }
           /* int startPart = maxField;
            int startLabelPart = startPart;
            int totalFieldPart = 10;
            for(int count = 1; count <= numberOfPart; count++){
                createCellLabelPart(sheet, startLabelPart, styleOfLabel);
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(startPart);
                cell.setCellValue("Part #" + count);
                int endRow = startPart + totalFieldPart;
                sheet.addMergedRegion(new CellRangeAddress(0,0, startPart, endRow-1));
                startLabelPart = startLabelPart + totalFieldPart;
                startPart = endRow;
            }

            Row row = sheet.createRow(0);
            Cell basic = row.createCell(indexOfBasicInformation);
            basic.setCellValue("Basic Information");
            basic.setCellStyle(getStyleByColor(workbook, "C9DAF8"));

            Cell physical = row.createCell(indexOfPhysicalInformationDto);
            physical.setCellValue("Physical Information");
            physical.setCellStyle(getStyleByColor(workbook, "D9D9D9"));

            Cell runner = row.createCell(indexOfRunnerSystem);
            runner.setCellValue("Runner System Information");
            runner.setCellStyle(getStyleByColor(workbook, "E6B8AF"));

            Cell dynamic = row.createCell(indexOfDynamicInformationDto);
            dynamic.setCellValue("Dynamic Information");
            dynamic.setCellStyle(getStyleByColor(workbook, "D9EAD3"));

            Cell maintenance = row.createCell(indexOfMaintenanceInformationDto);
            maintenance.setCellValue("Maintenance Information");
            maintenance.setCellStyle(getStyleByColor(workbook, "FFF2CC"));
            if(!isCustomServerName) {
                Cell cost = row.createCell(indexOfCostInformation);
                cost.setCellValue("Cost Information");
                cost.setCellStyle(getStyleByColor(workbook, "D0E0E3"));
            }
            //Cell supplier = row.createCell(indexOfSupplierInformationDto);
            //supplier.setCellValue("Supplier Information");
            //supplier.setCellStyle(getStyleByColor(workbook, "F4CCCC"));


            for(int countPart = 0; countPart < numberOfPart; countPart++){
                Cell part = row.createCell(indexOfPart);
                part.setCellValue("Part #" + (countPart + 1));
                part.setCellStyle(getStyleByColor(workbook, "D9D2E9"));
                indexOfPart += totalFieldPart;
            }
*/
            workbook.write(outputStream);
//            FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/textExcel.xlsx"));
//            fileOutputStream.write(outputStream.toByteArray());
//            fileOutputStream.close();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream;
    }

/*
    private CellStyle getStyleByColor(Workbook workbook, String color) throws DecoderException {
        byte[] rgbB = Hex.decodeHex(color); // get byte array from hex string
        XSSFColor colorObj = new XSSFColor(rgbB, null);
        XSSFCellStyle cs = (XSSFCellStyle) workbook.createCellStyle();
        cs.setFillForegroundColor(colorObj);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        cs.setFont(font);

        return cs;
    }
*/


    public void readExcel() throws IOException {
        InputStream inputStream = getFileTemplateExcel("excel-import.xlsx");

        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        int indexRow = 0;
        while (true){
            int indexCell = 0;
            Row row = sheet.getRow(indexRow);
            indexRow++;
            if(row == null){
                break;
            }
            System.out.println("Row " + indexRow);
            for(int i = 0; i < row.getLastCellNum(); i ++){
                System.out.println("Cell " + i + ": " + (row.getCell(i) != null ? row.getCell(i).getStringCellValue() :""));
            }
            System.out.println(row.getFirstCellNum() +"-"+ row.getLastCellNum());
            //            Cell cell = row.getCell(indexCell);
//            System.out.println(cell.getStringCellValue());
        }

    }



    public ByteArrayOutputStream exportExcelToolingImportTemplate(){

        boolean isCustomServerName = "dyson".equals(this.customerServerName);
        InputStream  fileTemplate;
        fileTemplate = getFileTemplateExcel("tooling-import-template.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
            int rowGroup = 0;
            int rowHeader = 1;
            int rowData = 3;

//            Default
            int indexOfBasicInformation = 0;
            int indexOfPhysicalInformationDto = 10;
            int indexOfRunnerSystem = 19;
//            int indexOfDynamicInformationDto = 21;
//            int indexOfCostInformation = -1;
            int indexOfCostInformation = 24;
            int indexOfSupplierInformationDto = 27;
            int indexOfProduction = 31;
            int indexOfMaintenanceInformationDto = 39;
            int indexOfPart = 42;
            CellStyle basicGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfBasicInformation);
            CellStyle basicColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfBasicInformation);

            CellStyle physicalGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfPhysicalInformationDto);
            CellStyle physicalColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfPhysicalInformationDto);

            CellStyle runnerSystemGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfRunnerSystem);
            CellStyle runnerSystemColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfRunnerSystem);

//            CellStyle dynamicGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfDynamicInformationDto);
//            CellStyle dynamicColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfDynamicInformationDto);

            CellStyle costGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfCostInformation);
            CellStyle costColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfCostInformation);

            CellStyle supplierGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfSupplierInformationDto);
            CellStyle supplierColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfSupplierInformationDto);

            CellStyle productionGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfProduction);
            CellStyle productionColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfProduction);

            CellStyle maintenanceGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfMaintenanceInformationDto);
            CellStyle maintenanceColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfMaintenanceInformationDto);

            CellStyle partGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfPart);
            CellStyle partColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfPart);

            CellStyle valueStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowData,indexOfBasicInformation);

            // header default
            List<HeaderDTO> basicHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("equipmentCode", tooling.TOOLING_BASIC.Tooling_ID, true)
                    ,new HeaderDTO("counterId", tooling.TOOLING_BASIC.Counter_ID)
                    ,new HeaderDTO("accumulatedShots", tooling.TOOLING_BASIC.Accumulated_shot)
                    , new HeaderDTO("toolingLetter", tooling.TOOLING_BASIC.Tooling_Letter)
                    , new HeaderDTO("toolingType", tooling.TOOLING_BASIC.Tooling_Type)
                    , new HeaderDTO("toolingComplexity", tooling.TOOLING_BASIC.Tooling_Complexity)
                    , new HeaderDTO("designedShot", tooling.TOOLING_BASIC.Forecasted_Max_Shot, true)
                    , new HeaderDTO("lifeYears", tooling.TOOLING_BASIC.Forecasted_Tool_Life_year)
                    , new HeaderDTO("madeYear", tooling.TOOLING_BASIC.Year_of_Tool_Made)
                    , new HeaderDTO("locationCode", tooling.TOOLING_BASIC.Location_ID_Location, true)
                    , new HeaderDTO("engineerEmails", tooling.TOOLING_BASIC.Engineer_in_Charge_Email_Address)
                    , new HeaderDTO("plantEngineerEmails", tooling.TOOLING_BASIC.Plant_Engineer_in_Charge_Email_Address)
                    , new HeaderDTO("toolDescription", tooling.TOOLING_BASIC.Tool_Description)
            ));
            List<HeaderDTO> physicalHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("size_w", tooling.TOOLING_PHYSICAL.Tool_Size_W)
                    , new HeaderDTO("size_l", tooling.TOOLING_PHYSICAL.Tool_Size_L)
                    , new HeaderDTO("size_h", tooling.TOOLING_PHYSICAL.Tool_Size_H)
                    , new HeaderDTO("sizeUnit", tooling.TOOLING_PHYSICAL.Tool_Size_Unit)
                    , new HeaderDTO("weight", tooling.TOOLING_PHYSICAL.Tool_Weight)
                    , new HeaderDTO("weightUnit", tooling.TOOLING_PHYSICAL.Tool_Weight_Unit)
                    , new HeaderDTO("toolMakerCompanyCode", tooling.TOOLING_PHYSICAL.Toolmaker_ID_Toolmaker, true)
                    , new HeaderDTO("injectionMachineId", tooling.TOOLING_PHYSICAL.Injection_Molding_Machine_ID)
                    , new HeaderDTO("quotedMachineTonnage", tooling.TOOLING_PHYSICAL.Machine_Tonnage_ton)
                    , new HeaderDTO("totalCavities", tooling.TOOLING_PHYSICAL.Total_Number_of_Cavities, true)
            ));
            List<HeaderDTO> runnerSystemHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("runnerType", tooling.TOOLING_RUNNER_SYSTEM.Type_of_Runner_System),
                    new HeaderDTO("runnerMaker", tooling.TOOLING_RUNNER_SYSTEM.Maker_of_Runner_System),
                    new HeaderDTO("weightRunner", tooling.TOOLING_RUNNER_SYSTEM.Weight_of_Runner_System_gram),
                    new HeaderDTO("hotRunnerDrop", tooling.TOOLING_RUNNER_SYSTEM.Hot_Runner_Number_of_Drop),
                    new HeaderDTO("hotRunnerZone", tooling.TOOLING_RUNNER_SYSTEM.Hot_Runner_Zone)
            ));
            List<HeaderDTO> costHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("cost", tooling.TOOLING_COST.Cost_of_Tooling),
                    new HeaderDTO("costCurrencyType", tooling.TOOLING_COST.Cost_of_Tooling_Currency),
                    new HeaderDTO("salvageValue", tooling.TOOLING_COST.salvageValue),
                    new HeaderDTO("poDate", tooling.TOOLING_COST.poDate),
                    new HeaderDTO("poNumber", tooling.TOOLING_COST.poNumber),
                    new HeaderDTO("memo", tooling.TOOLING_COST.Memo)
            ));
            List<HeaderDTO> supplierHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("supplierCompanyCode", tooling.TOOLING_SUPPLIER.Supplier_ID_Supplier_Name, true),
                    new HeaderDTO("labour", tooling.TOOLING_SUPPLIER.Required_Labor),
                    new HeaderDTO("shiftsPerDay", tooling.TOOLING_SUPPLIER.Production_Hour_per_Day_default_24_hours),
                    new HeaderDTO("productionDays", tooling.TOOLING_SUPPLIER.Production_Day_per_Week_default_7_days)
            ));

            final List<HeaderDTO> subHeaderContractedCycleTime = Arrays.asList(
                    new HeaderDTO("toolmakerContractedCycleTime", tooling.TOOLING_PRODUCTION.Toolmaker_Approved_Cycle_Time_second, 5000),
                    new HeaderDTO("supplierContractedCycleTime", tooling.TOOLING_PRODUCTION.Supplier_Approved_Cycle_Time_second, 5000));

            List<HeaderDTO> productionHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("contractedCycleTime", tooling.TOOLING_PRODUCTION.Approved_Cycle_Time_second,
                            true, subHeaderContractedCycleTime),
                    new HeaderDTO("cycleTimeLimit1", tooling.TOOLING_PRODUCTION.Cycle_Time_Tolerance_L1, true),
                    new HeaderDTO("cycleTimeLimit1Unit", tooling.TOOLING_PRODUCTION.Cycle_Time_Tolerance_L1_Unit, true),
                    new HeaderDTO("cycleTimeLimit2", tooling.TOOLING_PRODUCTION.Cycle_Time_Tolerance_L2, true),
                    new HeaderDTO("cycleTimeLimit2Unit", tooling.TOOLING_PRODUCTION.Cycle_Time_Tolerance_L2_Unit, true),
                    new HeaderDTO("uptimeTarget", tooling.TOOLING_PRODUCTION.Uptime_Target_percent, true),
                    new HeaderDTO("uptimeLimitL1", tooling.TOOLING_PRODUCTION.Uptime_Tolerance_L1_percent, true),
                    new HeaderDTO("uptimeLimitL2", tooling.TOOLING_PRODUCTION.Uptime_Tolerance_L2_percent, true)
            ));
            List<HeaderDTO> maintenanceHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("preventCycle", tooling.TOOLING_MAINTENANCE.Maintenance_Interval_default_50000, true),
                    new HeaderDTO("preventUpcoming", tooling.TOOLING_MAINTENANCE.Upcoming_Maintenance_Tolerance_default_10000, true)
            ));
            List<HeaderDTO> partHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("partCode", tooling.TOOLING_PART.Part_ID, true)
//                    ,new HeaderDTO("cavity", tooling.TOOLING_PART.Number_of_Cavity, true)
                    ,new HeaderDTO("cavity", "Working Cavities", true)
//                    ,new HeaderDTO("totalCavities", "Total Number of Cavities", true)
            ));
            Map<PropertyGroup,List<HeaderDTO>> propertyGroupListHeaderMap=new HashMap<>();
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_BASIC,basicHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_PHYSICAL,physicalHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_RUNNER_SYSTEM,runnerSystemHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_COST,costHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_SUPPLIER,supplierHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_PRODUCTION,productionHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_MAINTENANCE,maintenanceHeaders);
            List<String> deleteProperties=new ArrayList<>();


            //process for config field
            List<HeaderDTO> allHeaderDTO=updateConfigFieldTooling(deleteProperties,propertyGroupListHeaderMap,new ArrayList<>(),partHeaders,true);

/*
            boolean configMoldEnable = OptionUtils.isEnabled(ConfigCategory.TOOLING, false);
            if(configMoldEnable){
                Map<String,Boolean> mapRequiredField = new HashMap<>();
                GeneralConfigPayload payload = new GeneralConfigPayload();
                payload.setConfigCategory(ConfigCategory.TOOLING);
                List<GeneralConfig> generalConfigList=generalConfigService.findAll(payload.getPredicate());
                generalConfigList.stream().forEach(cfg->{
                    if(!StringUtils.isEmpty(cfg.getFieldName()))
                        mapRequiredField.put(cfg.getFieldName(),cfg.getRequired());
                });
                //process for delete exists properties
                deleteProperties=generalConfigList.stream().filter(generalConfig -> generalConfig.getDeletedField()!=null
                        && generalConfig.getDeletedField()).map(g->g.getFieldName()).collect(Collectors.toList());
                //for dependent field
                if(deleteProperties.contains(Const.ColumnCode.tooling.COST.cost))
                    deleteProperties.add(Const.ColumnCode.tooling.COST.accumulatedMaintenanceCost);

                List<String> deletePropertiesFinal= deleteProperties;

                List<HeaderDTO> allHeader=new ArrayList<>();
                allHeader.addAll(partHeaders);
                propertyGroupListHeaderMap.keySet().stream().forEach(key->{
                    List<HeaderDTO> headerDTOList=propertyGroupListHeaderMap.get(key);
                    List<HeaderDTO> deleteList=headerDTOList.stream().filter(headerDTO -> deletePropertiesFinal.contains(headerDTO.getCode()))
                            .collect(Collectors.toList());
                    headerDTOList.removeAll(deleteList);

                    allHeader.addAll(headerDTOList);
                });
                //update require
                allHeader.stream().forEach(h->{
                    if(mapRequiredField.containsKey(h) && mapRequiredField.get(h)!=null){
                        h.setRequired(mapRequiredField.get(h));
                    } else if (Arrays.asList("size_w", "size_l", "size_h").contains(h.getCode()) && mapRequiredField.containsKey("size") && mapRequiredField.get("size") != null) {
                        h.setRequired(mapRequiredField.get("size"));
                    }
                });
                //update custom field
                List<CustomField> customFieldList = new ArrayList<>();
                Map<PropertyGroup,List<CustomField>> propertyGroupListCustomFieldMap=new HashMap<>();
                customFieldList = customFieldRepository.findByObjectTypeOrderByFieldNameAsc(ObjectType.TOOLING);
                customFieldList.stream().forEach(c -> {
                    List<CustomField> list = new ArrayList<>();
                    if (propertyGroupListCustomFieldMap.containsKey(c.getPropertyGroup())) {
                        list = propertyGroupListCustomFieldMap.get(c.getPropertyGroup());
                    } else {
                        propertyGroupListCustomFieldMap.put(c.getPropertyGroup(), list);
                    }
                    list.add(c);
                });
                //add custom field for list column
                propertyGroupListHeaderMap.keySet().stream().forEach(key->{
                    List<HeaderDTO> headerDTOList = propertyGroupListHeaderMap.get(key);
                    if(propertyGroupListCustomFieldMap.containsKey(key) && propertyGroupListCustomFieldMap.get(key)!=null && headerDTOList!=null){
                        List<CustomField> list = propertyGroupListCustomFieldMap.get(key);
                        List<HeaderDTO> headerCustom=list.stream().map(cf -> new HeaderDTO(cf)).collect(Collectors.toList());
                        headerDTOList.addAll(headerCustom);
                    }
                });

            }
*/
            //validation data
            List<String> sizeUnitList = codeMapper.get(Code.SIZE_UNIT).stream().map(c -> c.getTitle()).collect(Collectors.toList());
            List<String> weightUnitList = codeMapper.get(Code.WEIGHT_UNIT).stream().map(c -> c.getTitle()).collect(Collectors.toList());
            List<String> currencyTypeList = codeMapper.get(Code.Currency_Type).stream().filter(c->c.getEnabled()==true).map(c -> c.getCode()).collect(Collectors.toList());
            List<String> cycleTimeUnits = Arrays.asList("%", "second");
            //make column
            int colGroup=0;
            //basic
            colGroup = ExcelCommonUtils.makeHeader(colGroup,basicGroupStyle,basicColStyle,basicHeaders,"Basic Information",sheet, 0, 1, 3);


//            int colSizeUnit = physicalHeaders.stream().map(p -> p.getCode()).collect(Collectors.toList()).indexOf(Const.ColumnCode.tooling.PHYSICAL.IMPORT.sizeUnit);
//            int colWeightUnit = physicalHeaders.stream().map(p -> p.getCode()).collect(Collectors.toList()).indexOf(Const.ColumnCode.tooling.PHYSICAL.IMPORT.weightUnit);
            int colSizeUnit = getIndexOf(physicalHeaders, Const.ColumnCode.tooling.PHYSICAL.IMPORT.sizeUnit);
            int colWeightUnit = getIndexOf(physicalHeaders, Const.ColumnCode.tooling.PHYSICAL.IMPORT.weightUnit);
            if (colSizeUnit >= 0)
                ExcelCommonUtils.addValidationData(sheet, sizeUnitList, rowData, 999, colGroup + colSizeUnit, colGroup + colSizeUnit);
            if (colWeightUnit >= 0)
                ExcelCommonUtils.addValidationData(sheet, weightUnitList, rowData, 999, colGroup + colWeightUnit, colGroup + colWeightUnit);
            colGroup = ExcelCommonUtils.makeHeader(colGroup,physicalGroupStyle,physicalColStyle,physicalHeaders,"Physical Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,runnerSystemGroupStyle,runnerSystemColStyle,runnerSystemHeaders,"Runner System Information",sheet, 0, 1, 3);
//            if (!isCustomServerName){
//            int colCurrentType = costHeaders.stream().map(p -> p.getCode()).collect(Collectors.toList()).indexOf(Const.ColumnCode.tooling.COST.costCurrencyType);
            int colCurrentType = getIndexOf(costHeaders, Const.ColumnCode.tooling.COST.costCurrencyType);
            if (colCurrentType >= 0)
                ExcelCommonUtils.addValidationData(sheet, currencyTypeList, rowData, 999, colGroup + colCurrentType, colGroup + colCurrentType);
            colGroup = ExcelCommonUtils.makeHeader(colGroup, costGroupStyle, costColStyle, costHeaders, "Cost Information", sheet, 0, 1, 3);
//            }
            colGroup = ExcelCommonUtils.makeHeader(colGroup,supplierGroupStyle,supplierColStyle,supplierHeaders,"Supplier Information",sheet, 0, 1, 3);

//            int colCycleTimeUnitL1=productionHeaders.stream().map(p -> p.getCode()).collect(Collectors.toList())
//                    .indexOf(Const.ColumnCode.tooling.PRODUCTION.IMPORT.cycleTimeLimit1Unit);
//            int colCycleTimeUnitL2=productionHeaders.stream().map(p -> p.getCode()).collect(Collectors.toList())
//                    .indexOf(Const.ColumnCode.tooling.PRODUCTION.IMPORT.cycleTimeLimit2Unit);
            int colCycleTimeUnitL1 = getIndexOf(productionHeaders, Const.ColumnCode.tooling.PRODUCTION.IMPORT.cycleTimeLimit1Unit);
            int colCycleTimeUnitL2 = getIndexOf(productionHeaders, Const.ColumnCode.tooling.PRODUCTION.IMPORT.cycleTimeLimit2Unit);
            if (colCycleTimeUnitL1 >= 0)
                ExcelCommonUtils.addValidationData(sheet, cycleTimeUnits, rowData, 999, colGroup + colCycleTimeUnitL1, colGroup + colCycleTimeUnitL1);
            if (colCycleTimeUnitL2 >= 0)
                ExcelCommonUtils.addValidationData(sheet, cycleTimeUnits, rowData, 999, colGroup + colCycleTimeUnitL2, colGroup + colCycleTimeUnitL2);
            colGroup = ExcelCommonUtils.makeHeader(colGroup,productionGroupStyle,productionColStyle,productionHeaders,"Production Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,maintenanceGroupStyle,maintenanceColStyle,maintenanceHeaders,"Maintenance Information",sheet, 0, 1, 3);
            colGroup = ExcelCommonUtils.makeHeader(colGroup,partGroupStyle,partColStyle,partHeaders,"Part 1",sheet, 0, 1, 3);
            //clear temp column
/*
            if (isCustomServerName){
                for(int i=colGroup;i<(colGroup+costHeaders.size());i++){
                    sheet.getRow(rowGroup).removeCell(ExcelCommonUtils.getOrCreateCell(sheet,rowGroup,i));
                    sheet.getRow(rowHeader).removeCell(ExcelCommonUtils.getOrCreateCell(sheet,rowHeader,i));
                }
            }
*/

            //make style cell value
            for(int r=rowData;r<40;r++){
                for (int c=0;c<colGroup;c++){
                    Cell cell=ExcelCommonUtils.getOrCreateCell(sheet,r,c);
                    cell.setCellStyle(valueStyle);
                }
/*
                if (isCustomServerName){
                    for(int i=colGroup;i<(colGroup+costHeaders.size());i++){
                        sheet.getRow(r).removeCell(ExcelCommonUtils.getOrCreateCell(sheet,r,i));
                    }
                }
*/
            }
            ExcelCommonUtils.removeRageCell(sheet,0,40,colGroup,45);

            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream;
    }

    private int getIndexOf(List<HeaderDTO> headers, String fieldImportName){
        final int[] result = {headers.stream().map(p -> p.getCode()).collect(Collectors.toList()).indexOf(fieldImportName)};

        List<HeaderDTO> subHeaders = headers.stream().filter(x -> x.getSubHeader() != null && !x.getSubHeader().isEmpty())
                .collect(Collectors.toList());

        if(subHeaders == null || subHeaders.size() == 0)
            return result[0];

        subHeaders.forEach(header -> {
            int indexOfSubHeader = headers.indexOf(header);
            if(indexOfSubHeader >= 0 && indexOfSubHeader < result[0]){
                result[0] += header.getSubHeader().size() - 1;
            }
        });
        return result[0];
    }

    public ByteArrayOutputStream exportExcelPartImportTemplate(){

        boolean isCustomServerName = "dyson".equals(this.customerServerName);
        InputStream  fileTemplate;

        fileTemplate = getFileTemplateExcel("part-import-template.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);

            int rowGroup = 0;
            int rowHeader = 1;
            int rowData = 2;

//            Default
            int indexOfBasicInformation = 0;

            //style for lable
            CellStyle basicGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfBasicInformation);
            CellStyle basicColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfBasicInformation);
            CellStyle valueStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowData,indexOfBasicInformation);

            // header default
            List<HeaderDTO> basicPartHeaders = new ArrayList<>(Arrays.asList(
//                    new HeaderDTO("SEQ", "SEQ",2000),
//                    new HeaderDTO("categoryName", "Category Name"),
                    new HeaderDTO(Const.ColumnCode.part.category, "Product Name")
                    ,new HeaderDTO("partCode", "Part ID", true)
                    ,new HeaderDTO("name", "Part Name", true)
                    , new HeaderDTO("resinCode", "Part Resin Code")
                    , new HeaderDTO("resinGrade", "Part Resin Grade")
                    , new HeaderDTO("designRevision", "Design Revision Level")
//                    , new HeaderDTO("size_w", "Part Volume (W)")
//                    , new HeaderDTO("size_d", "Part Volume (D)")
//                    , new HeaderDTO("size_h", "Part Volume (H)")
                    , new HeaderDTO("size", "Part Volume (W x D x H)")
                    , new HeaderDTO("sizeUnit", "Part Volume Unit")
                    , new HeaderDTO("weight", "Part Weight")
                    , new HeaderDTO("weightUnit", "Part Weight Unit")
                    , new HeaderDTO("weeklyDemand", "Weekly Demand")
                    , new HeaderDTO("quantityRequired", "Quantity Required")
            ));

//            Map<PropertyGroup,List<HeaderDTO>> propertyGroupListHeaderMap=new HashMap<>();
//            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_BASIC,basicHeaders);
            List<String> deleteProperties=new ArrayList<>();
            updatePartHeader(basicPartHeaders,deleteProperties,true);

//            propertyGroupListHeaderMap.keySet().stream().forEach(key->{
//                allHeader.addAll(propertyGroupListHeaderMap.get(key));
//            });
/*

            //process for config field
            boolean configPartEnable = OptionUtils.isEnabled(ConfigCategory.PART, false);
            if(configPartEnable){
                Map<String,Boolean> mapRequiredField = new HashMap<>();
                GeneralConfigPayload payload = new GeneralConfigPayload();
                payload.setConfigCategory(ConfigCategory.PART);
                List<GeneralConfig> generalConfigList=generalConfigService.findAll(payload.getPredicate());
                generalConfigList.stream().forEach(cfg->{
                    if(!StringUtils.isEmpty(cfg.getFieldName()))
                        mapRequiredField.put(cfg.getFieldName(),cfg.getRequired());
                });
                //update require
                List<HeaderDTO> allHeader=new ArrayList<>();
                allHeader.addAll(basicPartHeaders);

                allHeader.stream().forEach(h->{
                    if(mapRequiredField.containsKey(h) && mapRequiredField.get(h)!=null){
                        h.setRequired(mapRequiredField.get(h));
                    } else if (Arrays.asList("size_w", "size_d", "size_h").contains(h.getCode()) && mapRequiredField.containsKey("size") && mapRequiredField.get("size") != null) {
                        h.setRequired(mapRequiredField.get("size"));
                    }
                });
                //update custom field
                List<CustomField> customFieldList = new ArrayList<>();
//                Map<PropertyGroup,List<CustomField>> propertyGroupListCustomFieldMap=new HashMap<>();
                customFieldList = customFieldRepository.findByObjectTypeOrderByFieldNameAsc(ObjectType.PART);


                List<HeaderDTO> headerCustom=customFieldList.stream().map(cf -> new HeaderDTO(cf)).collect(Collectors.toList());
                basicPartHeaders.addAll(headerCustom);

            }
*/
            //validation data
            List<String> sizeUnitList = codeMapper.get(Code.SIZE_UNIT)
                    .stream().map(c -> {
                        if("MM".equals(c.getCode()) || "CM".equals(c.getCode()) || "M".equals(c.getCode())) {
                            return c.getTitle()+"3";
                        }
                        return c.getTitle();
                    })
                    .collect(Collectors.toList());
            List<String> weightUnitList = codeMapper.get(Code.WEIGHT_UNIT).stream().map(c -> c.getTitle()).collect(Collectors.toList());

            //make column
            int colGroup=0;
            int colSizeUnit = basicPartHeaders.stream().map(p -> p.getCode()).collect(Collectors.toList()).indexOf(Const.ColumnCode.part.IMPORT.sizeUnit);
            int colWeightUnit = basicPartHeaders.stream().map(p -> p.getCode()).collect(Collectors.toList()).indexOf(Const.ColumnCode.part.IMPORT.weightUnit);
            if (colSizeUnit >= 0)
                ExcelCommonUtils.addValidationData(sheet, sizeUnitList, rowData, 999, colGroup + colSizeUnit, colGroup + colSizeUnit);
            if (colWeightUnit >= 0)
                ExcelCommonUtils.addValidationData(sheet, weightUnitList, rowData, 999, colGroup + colWeightUnit, colGroup + colWeightUnit);
            //basic
            ExcelCommonUtils.makeHeader(colGroup,basicGroupStyle,basicColStyle,basicPartHeaders,"Part Information",sheet, 0, 1,2);
            //make style cell value
            for(int r=rowData;r<40;r++){
                for (int c=0;c<basicPartHeaders.size();c++){
                    Cell cell=ExcelCommonUtils.getOrCreateCell(sheet,r,c);
                    cell.setCellStyle(valueStyle);
                }
            }
            ExcelCommonUtils.removeRageCell(sheet,0,40,basicPartHeaders.size(),15);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream;
    }

    private List<HeaderDTO> updateConfigFieldTooling(final List<String> deletePropertiesFinal,final Map<PropertyGroup,List<HeaderDTO>> propertyGroupListHeaderMap,
                                                     List<HeaderDTO> dynamicHeaders,List<HeaderDTO> partHeaders,boolean isTemplate){
        //process for config field
        if (OptionUtils.isEnabled(ConfigCategory.TOOLING)) {
            Map<String,Boolean> mapRequiredField = new HashMap<>();
            GeneralConfigPayload payload = new GeneralConfigPayload();
            payload.setConfigCategory(ConfigCategory.TOOLING);
            List<GeneralConfig> generalConfigList=generalConfigService.findAll(payload.getPredicate());
            generalConfigList.stream().forEach(cfg->{
                if(!StringUtils.isEmpty(cfg.getFieldName()))
                    mapRequiredField.put(cfg.getFieldName(),cfg.getRequired());
            });

            //default value map
            Map<String, String> defaultValueMap =  generalConfigList.stream()
                    .filter(generalConfig -> generalConfig.getDefaultInput() != null && generalConfig.getDefaultInput() && generalConfig.getDefaultInputValue() != null)
                    .collect(Collectors.toMap(GeneralConfig::getFieldName, GeneralConfig::getDefaultInputValue, (o, n) -> n));


            //process for delete exists properties
            List<String> deleteProperties=generalConfigList.stream().filter(generalConfig -> generalConfig.getDeletedField()!=null
                    && generalConfig.getDeletedField()).map(g->g.getFieldName()).collect(Collectors.toList());
            //for dependent field
            if (deleteProperties.contains(Const.ColumnCode.tooling.COST.cost)) {
                if (isTemplate)
                    deleteProperties.add(Const.ColumnCode.tooling.COST.costCurrencyType);
                else
                    deleteProperties.add(Const.ColumnCode.tooling.COST.accumulatedMaintenanceCost);
            }
//            if (deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.size)) {
//                if (isTemplate){
//                    deleteProperties.add(Const.ColumnCode.tooling.PHYSICAL.IMPORT.size_w);
//                    deleteProperties.add(Const.ColumnCode.tooling.PHYSICAL.IMPORT.size_h);
//                    deleteProperties.add(Const.ColumnCode.tooling.PHYSICAL.IMPORT.size_l);
//                    deleteProperties.add(Const.ColumnCode.tooling.PHYSICAL.IMPORT.sizeUnit);
//                }
//                else{
//                }
//            }
            if (deleteProperties.contains(Const.ColumnCode.tooling.PHYSICAL.weight)) {
                if (isTemplate){
                    deleteProperties.add(Const.ColumnCode.tooling.PHYSICAL.IMPORT.weightUnit);
                }
                else{
                }
            }
            if (deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.cycleTimeLimit1)) {
                if (isTemplate){
                    deleteProperties.add(Const.ColumnCode.tooling.PRODUCTION.IMPORT.cycleTimeLimit1Unit);
                }
                else{
                }
            }
            if (deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.cycleTimeLimit2)) {
                if (isTemplate){
                    deleteProperties.add(Const.ColumnCode.tooling.PRODUCTION.IMPORT.cycleTimeLimit2Unit);
                }
                else{
                }
            }

            deletePropertiesFinal.addAll(deleteProperties);
            List<HeaderDTO> allHeader=new ArrayList<>();
            propertyGroupListHeaderMap.keySet().stream().forEach(key->{
                List<HeaderDTO> headerDTOList=propertyGroupListHeaderMap.get(key);
                List<HeaderDTO> deleteList=headerDTOList.stream().filter(headerDTO -> deletePropertiesFinal.contains(headerDTO.getCode()))
                        .collect(Collectors.toList());
                headerDTOList.removeAll(deleteList);

                allHeader.addAll(headerDTOList);
            });
            allHeader.addAll(dynamicHeaders);
            allHeader.addAll(partHeaders);

            //update require
            allHeader.stream().forEach(h->{

                if(mapRequiredField.containsKey(h.getCode()) && mapRequiredField.get(h.getCode())!=null){
                    h.setRequired(mapRequiredField.get(h.getCode()));
                } /*else if (Arrays.asList("size_w", "size_l", "size_h",Const.ColumnCode.tooling.PHYSICAL.IMPORT.sizeUnit).contains(h.getCode()) && mapRequiredField.containsKey("size") && mapRequiredField.get("size") != null) {
                    h.setRequired(mapRequiredField.get("size"));
                }*/ else if (Arrays.asList(Const.ColumnCode.tooling.PHYSICAL.IMPORT.weightUnit).contains(h.getCode())
                        && mapRequiredField.containsKey(Const.ColumnCode.tooling.PHYSICAL.weight) && mapRequiredField.get(Const.ColumnCode.tooling.PHYSICAL.weight) != null) {
                    h.setRequired(mapRequiredField.get(Const.ColumnCode.tooling.PHYSICAL.weight));
                } else if("engineerEmails".equals(h.getCode())&& mapRequiredField.containsKey(Const.ColumnCode.tooling.BASIC.engineers)) {
                    h.setRequired(mapRequiredField.get(Const.ColumnCode.tooling.BASIC.engineers));
                } else if ("contractedCycleTime".equals(h.getCode())&& mapRequiredField.containsKey(Const.ColumnCode.tooling.PRODUCTION.approvedCycleTime)) {
                    h.setRequired(mapRequiredField.get(Const.ColumnCode.tooling.PRODUCTION.approvedCycleTime));
                } /* else if("locationCode".equals(h.getCode()) && mapRequiredField.containsKey(Const.ColumnCode.tooling.BASIC.locationId)) {
                    h.setRequired(mapRequiredField.get(Const.ColumnCode.tooling.BASIC.locationId));
                } else if("toolMakerCompanyCode".equals(h.getCode()) && mapRequiredField.containsKey(Const.ColumnCode.tooling.PHYSICAL.toolMakerCompanyName)) {
                    h.setRequired(mapRequiredField.get(Const.ColumnCode.tooling.PHYSICAL.toolMakerCompanyName));
                } else if("supplierCompanyCode".equals(h.getCode()) && mapRequiredField.containsKey(Const.ColumnCode.tooling.SUPPLIER.supplierCompanyName)) {
                    h.setRequired(mapRequiredField.get(Const.ColumnCode.tooling.PHYSICAL.toolMakerCompanyName));
                } */
            });

            //update default value
            propertyGroupListHeaderMap.keySet().forEach(key->{
                List<HeaderDTO> headerDTOList = propertyGroupListHeaderMap.get(key);
                headerDTOList.forEach(h -> {
                    if(defaultValueMap.containsKey(h.getCode()) && defaultValueMap.get(h.getCode())!=null){
                        h.setDefaultValue(defaultValueMap.get(h.getCode()));
                    } /*else if (Arrays.asList("size_w", "size_l", "size_h",Const.ColumnCode.tooling.PHYSICAL.IMPORT.sizeUnit).contains(h.getCode()) && defaultValueMap.containsKey("size") && defaultValueMap.get("size") != null) {
                        h.setDefaultValue(defaultValueMap.get("size"));
                    } */ else if (Arrays.asList(Const.ColumnCode.tooling.PHYSICAL.IMPORT.weightUnit).contains(h.getCode())
                            && defaultValueMap.containsKey(Const.ColumnCode.tooling.PHYSICAL.weight) && defaultValueMap.get(Const.ColumnCode.tooling.PHYSICAL.weight) != null) {
                        h.setDefaultValue(defaultValueMap.get(Const.ColumnCode.tooling.PHYSICAL.weight));
                    } else if("engineerEmails".equals(h.getCode())&& defaultValueMap.containsKey(Const.ColumnCode.tooling.BASIC.engineers)) {
                        h.setDefaultValue(defaultValueMap.get(Const.ColumnCode.tooling.BASIC.engineers));
                    }
                });
            });

            //update custom field
            List<CustomField> customFieldList = new ArrayList<>();
            Map<PropertyGroup,List<CustomField>> propertyGroupListCustomFieldMap=new HashMap<>();
            customFieldList = customFieldRepository.findByObjectTypeOrderByFieldNameAsc(ObjectType.TOOLING);
            customFieldList.stream().forEach(c -> {
                List<CustomField> list = new ArrayList<>();
                if (propertyGroupListCustomFieldMap.containsKey(c.getPropertyGroup())) {
                    list = propertyGroupListCustomFieldMap.get(c.getPropertyGroup());
                } else {
                    propertyGroupListCustomFieldMap.put(c.getPropertyGroup(), list);
                }
                list.add(c);
            });


            //add custom field for list column
            propertyGroupListHeaderMap.keySet().stream().forEach(key->{
                List<HeaderDTO> headerDTOList = propertyGroupListHeaderMap.get(key);
                if(propertyGroupListCustomFieldMap.containsKey(key) && propertyGroupListCustomFieldMap.get(key)!=null && headerDTOList!=null){
                    List<CustomField> list = propertyGroupListCustomFieldMap.get(key);
                    List<HeaderDTO> headerCustom=list.stream().map(cf -> new HeaderDTO(cf)).collect(Collectors.toList());
                    headerDTOList.addAll(headerCustom);
                }
            });
            return allHeader;
        }
        return null;
    }


    public ByteArrayOutputStream exportExcelToolingDetailNew(List<Mold> molds, Integer timezoneOffsetClient) throws IOException {

        boolean isCustomServerName = "dyson".equals(this.customerServerName);
        InputStream  fileTemplate;

        fileTemplate = getFileTemplateExcel("tooling-export-template.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);
//            int startRow = 2;
            int rowGroup = 0;
            int rowHeader = 1;
            int rowData = 3;
            int index = 1;
            int indexCell = 0;
            int numberOfPart = 0;
            int maxField = 0;
            boolean assignMaxField = false;

//            Default for get style
            int indexOfBasicInformation = 1;
            int indexOfPhysicalInformationDto = 11;
            int indexOfRunnerSystem = 17;
            int indexOfDynamic = 22;
            int indexOfCostInformation = 28;
            int colOfSlDepreciation=30;
            int colOfUpDepreciation=31;
            int indexOfSupplierInformationDto = 32;
            int indexOfProduction = 37;
            int indexOfMaintenanceInformationDto = 43;
            int colOfRefurbishment = 45;
            int colOfWorkOrder = 46;
            int indexOfPart = 47;
            //style for lable
            CellStyle basicGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfBasicInformation);
            CellStyle basicColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfBasicInformation);

            CellStyle physicalGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfPhysicalInformationDto);
            CellStyle physicalColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfPhysicalInformationDto);

            CellStyle runnerSystemGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfRunnerSystem);
            CellStyle runnerSystemColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfRunnerSystem);

            CellStyle dynamicGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfDynamic);
            CellStyle dynamicColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfDynamic);

            CellStyle costGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfCostInformation);
            CellStyle costColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfCostInformation);

            CellStyle slDepreciationGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,colOfSlDepreciation);
            CellStyle slDepreciationColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,colOfSlDepreciation);

            CellStyle upDepreciationGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,colOfUpDepreciation);
            CellStyle upDepreciationColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,colOfUpDepreciation);

            CellStyle supplierGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfSupplierInformationDto);
            CellStyle supplierColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfSupplierInformationDto);

            CellStyle productionGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfProduction);
            CellStyle productionColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfProduction);

            CellStyle maintenanceGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfMaintenanceInformationDto);
            CellStyle maintenanceColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfMaintenanceInformationDto);

            CellStyle refurbishmentGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,colOfRefurbishment);
            CellStyle refurbishmentColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,colOfRefurbishment);

            CellStyle workOrderGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,colOfWorkOrder);
            CellStyle workOrderColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,colOfWorkOrder);

            CellStyle partGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfPart);
            CellStyle partColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfPart);

            CellStyle valueStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowData,indexOfBasicInformation);

            // header default
            HeaderDTO lifeYears= new HeaderDTO("lifeYears", tooling.TOOLING_BASIC.Forecasted_Tool_Life_year);
            List<HeaderDTO> basicHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("equipmentCode", tooling.TOOLING_BASIC.Tooling_ID, true)
                    ,new HeaderDTO("supplierMoldCode", "Supplier Tooling ID")
                    ,new HeaderDTO("counterId", tooling.TOOLING_BASIC.Counter_ID)
                    , new HeaderDTO("toolingLetter", tooling.TOOLING_BASIC.Tooling_Letter)
                    , new HeaderDTO("toolingType", tooling.TOOLING_BASIC.Tooling_Type)
                    , new HeaderDTO("toolingComplexity", tooling.TOOLING_BASIC.Tooling_Complexity)
                    , new HeaderDTO("designedShot", tooling.TOOLING_BASIC.Forecasted_Max_Shot, true)
                    , lifeYears
                    , new HeaderDTO("madeYear", tooling.TOOLING_BASIC.Year_of_Tool_Made)
//                    , new HeaderDTO("locationCode", tooling.TOOLING_BASIC.Location_ID_Location, true)
                    , new HeaderDTO("engineerEmails", "Engineer in Charge")
                    , new HeaderDTO("plantEngineerEmails", "Plant Engineer in Charge")
                    , new HeaderDTO("toolDescription", tooling.TOOLING_BASIC.Tool_Description)
            ));
            if(isCustomServerName){
                basicHeaders.remove(lifeYears);
            }
            List<HeaderDTO> physicalHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("size", tooling.TOOLING_PHYSICAL.Tool_Size)
                    , new HeaderDTO("weight", tooling.TOOLING_PHYSICAL.Tool_Weight)
                    , new HeaderDTO("shotSize", tooling.TOOLING_PHYSICAL.Shot_Weight)
                    , new HeaderDTO("toolMakerCompanyCode", "Toolmaker", true)
                    , new HeaderDTO("injectionMachineId", tooling.TOOLING_PHYSICAL.Injection_Molding_Machine_ID)
                    , new HeaderDTO("quotedMachineTonnage", tooling.TOOLING_PHYSICAL.Machine_Tonnage)
                    , new HeaderDTO("totalCavities", tooling.TOOLING_PHYSICAL.Total_Number_of_Cavities, true)
            ));
            List<HeaderDTO> runnerSystemHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("runnerType", tooling.TOOLING_RUNNER_SYSTEM.Type_of_Runner_System),
                    new HeaderDTO("runnerMaker", tooling.TOOLING_RUNNER_SYSTEM.Maker_of_Runner_System),
                    new HeaderDTO("weightRunner", tooling.TOOLING_RUNNER_SYSTEM.Weight_of_Runner_System),
                    new HeaderDTO("hotRunnerDrop", tooling.TOOLING_RUNNER_SYSTEM.Hot_Runner_Number_of_Drop),
                    new HeaderDTO("hotRunnerZone", tooling.TOOLING_RUNNER_SYSTEM.Hot_Runner_Zone)
            ));
            List<HeaderDTO> dynamicHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("operatingStatus", "OP"),
                    new HeaderDTO("lastShot", "Accumulated Shots"),
                    new HeaderDTO("lastShotAt", "Last Date of Shot"),
                    new HeaderDTO("utilizationRate", "Utilization Rate"),
                    new HeaderDTO("remainingPartsCount", "Remaining No. of Parts"),
                    new HeaderDTO("location", "Plant"),
                    new HeaderDTO("cycleTime", "Latest Cycle Time"),
                    new HeaderDTO("weightedAverageCycleTime", "Weighted Average Cycle Time")
            ));

            List<HeaderDTO> costHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("cost", tooling.TOOLING_COST.Cost_of_Tooling),
                    new HeaderDTO("accumulatedMaintenanceCost", "Accumulated Maintenance Cost"),
                    new HeaderDTO("salvageValue", "Salvage Value"),
                    new HeaderDTO("poDate", "PO Date"),
                    new HeaderDTO("poNumber", "PO Number"),
                    new HeaderDTO("memo", tooling.TOOLING_COST.Memo)
            ));
           List<HeaderDTO> slDepreciationHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO(Const.ColumnCode.tooling.SL_DEPRECIATION.slCurrentBookValue, "Current Book Value of Asset"),
                    new HeaderDTO(Const.ColumnCode.tooling.SL_DEPRECIATION.slDepreciation, "Depreciation"),
                    new HeaderDTO(Const.ColumnCode.tooling.SL_DEPRECIATION.slDepreciationTerm, "Depreciation Term"),
                    new HeaderDTO(Const.ColumnCode.tooling.SL_DEPRECIATION.slYearlyDepreciation, "Yearly Depreciation"),
                    new HeaderDTO(Const.ColumnCode.tooling.SL_DEPRECIATION.slLatestDepreciationPoint, "Latest Depreciation Point")
            ));
           List<HeaderDTO> upDepreciationHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO(Const.ColumnCode.tooling.UP_DEPRECIATION.upCurrentBookValue, "Current Book Value of Asset"),
                    new HeaderDTO(Const.ColumnCode.tooling.UP_DEPRECIATION.depreciationPercentage, "Depreciation"),
                    new HeaderDTO(Const.ColumnCode.tooling.UP_DEPRECIATION.upDepreciationTerm, "Depreciation Term"),
                    new HeaderDTO(Const.ColumnCode.tooling.UP_DEPRECIATION.upDepreciationPerShot, "Depreciation per Shot"),
                    new HeaderDTO(Const.ColumnCode.tooling.UP_DEPRECIATION.upLatestDepreciationPoint, "Latest Depreciation Point")
            ));

            List<HeaderDTO> supplierHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("supplierCompanyCode", tooling.TOOLING_SUPPLIER.Supplier, true),
                    new HeaderDTO("labour", tooling.TOOLING_SUPPLIER.Required_Labor),
                    new HeaderDTO("shiftsPerDay", tooling.TOOLING_SUPPLIER.Production_Hour_per_Day),
                    new HeaderDTO("productionDays", tooling.TOOLING_SUPPLIER.Production_Day_per_Week),
                    new HeaderDTO("maxCapacityPerWeek", "Maximum Capacity Per Week")
            ));

            final List<HeaderDTO> subHeaderContractedCycleTime = Arrays.asList(
                    new HeaderDTO("toolmakerContractedCycleTime", tooling.TOOLING_PRODUCTION.Toolmaker_Approved_Cycle_Time_second, 5000),
                    new HeaderDTO("contractedCycleTime", tooling.TOOLING_PRODUCTION.Supplier_Approved_Cycle_Time_second, 5000));

            List<HeaderDTO> productionHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("contractedCycleTime", tooling.TOOLING_PRODUCTION.Approved_Cycle_Time_second, true, subHeaderContractedCycleTime),
                    new HeaderDTO("cycleTimeLimit1", tooling.TOOLING_PRODUCTION.Cycle_Time_Tolerance_L1, true),
                    new HeaderDTO("cycleTimeLimit2", tooling.TOOLING_PRODUCTION.Cycle_Time_Tolerance_L2, true),
                    new HeaderDTO("uptimeTarget", "Uptime Target", true),
                    new HeaderDTO("uptimeLimitL1", "Uptime Tolerance L1", true),
                    new HeaderDTO("uptimeLimitL2", "Uptime Tolerance L2", true)
            ));
            List<HeaderDTO> maintenanceHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("preventCycle", "Maintenance Interval", true),
                    new HeaderDTO("preventUpcoming", "Upcoming Maintenance Tolerance", true)
            ));
            List<HeaderDTO> refurbishmentHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO(Const.ColumnCode.tooling.REFURBISHMENT.lastMaintenanceDate, "Last Refurbishment Date", 6000)
            ));
            List<HeaderDTO> workOrderHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO(Const.ColumnCode.tooling.WORK_ORDER.lastRefurbishmentDate, "Last Work Order Date",6000)
            ));

            List<HeaderDTO> partHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO(Const.ColumnCode.part.EXPORT.categoryName, "Category Name")
                    ,new HeaderDTO(Const.ColumnCode.part.EXPORT.projectName, "Product Name")
                    ,new HeaderDTO("partCode", "Part ID", true)
                    ,new HeaderDTO("name", "Part Name")
                    , new HeaderDTO("resinCode", "Part Resin Code")
                    , new HeaderDTO("resinGrade", "Part Resin Grade")
                    , new HeaderDTO("designRevision", "Design Revision Level")
                    , new HeaderDTO("size", "Part Volume")
                    , new HeaderDTO("weight", "Part Weight")
//                    , new HeaderDTO("cavity", "Number of Cavity", true)
                    , new HeaderDTO("cavity", "Working Cavities / Total Number of Cavities", true)
            ));
            Map<PropertyGroup,List<HeaderDTO>> propertyGroupListHeaderMap=new HashMap<>();
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_BASIC,basicHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_PHYSICAL,physicalHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_RUNNER_SYSTEM,runnerSystemHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_COST,costHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.SL_DEPRECIATION,slDepreciationHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.UP_DEPRECIATION,upDepreciationHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_SUPPLIER,supplierHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_PRODUCTION,productionHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_MAINTENANCE,maintenanceHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_REFURBISHMENT,refurbishmentHeaders);
            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_WORK_ORDER,workOrderHeaders);
            List<String> deleteProperties=new ArrayList<>();
            List<String> deletePropertiesPart=new ArrayList<>();

            //process for config field
            //add load custom properties of part
            updatePartHeader(partHeaders,deletePropertiesPart,false);
            updateConfigFieldTooling(deleteProperties,propertyGroupListHeaderMap,dynamicHeaders,partHeaders,false);
/*
            boolean configMoldEnable = OptionUtils.isEnabled(ConfigCategory.TOOLING, false);
            if(configMoldEnable){
                Map<String,Boolean> mapRequiredField = new HashMap<>();
                GeneralConfigPayload payload = new GeneralConfigPayload();
                payload.setConfigCategory(ConfigCategory.TOOLING);
                List<GeneralConfig> generalConfigList=generalConfigService.findAll(payload.getPredicate());
                generalConfigList.stream().forEach(cfg->{
                    if(!StringUtils.isEmpty(cfg.getFieldName()))
                        mapRequiredField.put(cfg.getFieldName(),cfg.getRequired());
                });

                //process for delete exists properties
                deleteProperties=generalConfigList.stream().filter(generalConfig -> generalConfig.getDeletedField()!=null
                        && generalConfig.getDeletedField()).map(g->g.getFieldName()).collect(Collectors.toList());
                //for dependent field
                if(deleteProperties.contains(Const.ColumnCode.tooling.COST.cost))
                    deleteProperties.add(Const.ColumnCode.tooling.COST.accumulatedMaintenanceCost);

                List<String> deletePropertiesFinal= deleteProperties;
                List<HeaderDTO> allHeader=new ArrayList<>();
                propertyGroupListHeaderMap.keySet().stream().forEach(key->{
                    List<HeaderDTO> headerDTOList=propertyGroupListHeaderMap.get(key);
                    List<HeaderDTO> deleteList=headerDTOList.stream().filter(headerDTO -> deletePropertiesFinal.contains(headerDTO.getCode()))
                            .collect(Collectors.toList());
                    headerDTOList.removeAll(deleteList);

                    allHeader.addAll(headerDTOList);
                });
                allHeader.addAll(dynamicHeaders);
                allHeader.addAll(partHeaders);

                //update require
                allHeader.stream().forEach(h->{
                    if(mapRequiredField.containsKey(h) && mapRequiredField.get(h)!=null){
                        h.setRequired(mapRequiredField.get(h));
                    } else if (Arrays.asList("size_w", "size_l", "size_h").contains(h.getCode()) && mapRequiredField.containsKey("size") && mapRequiredField.get("size") != null) {
                        h.setRequired(mapRequiredField.get("size"));
                    }
                });
                //update custom field
                List<CustomField> customFieldList = new ArrayList<>();
                Map<PropertyGroup,List<CustomField>> propertyGroupListCustomFieldMap=new HashMap<>();
                customFieldList = customFieldRepository.findByObjectTypeOrderByFieldNameAsc(ObjectType.TOOLING);
                customFieldList.stream().forEach(c -> {
                    List<CustomField> list = new ArrayList<>();
                    if (propertyGroupListCustomFieldMap.containsKey(c.getPropertyGroup())) {
                        list = propertyGroupListCustomFieldMap.get(c.getPropertyGroup());
                    } else {
                        propertyGroupListCustomFieldMap.put(c.getPropertyGroup(), list);
                    }
                    list.add(c);
                });
                //add custom field for list column
                propertyGroupListHeaderMap.keySet().stream().forEach(key->{
                    List<HeaderDTO> headerDTOList = propertyGroupListHeaderMap.get(key);
                    if(propertyGroupListCustomFieldMap.containsKey(key) && propertyGroupListCustomFieldMap.get(key)!=null && headerDTOList!=null){
                        List<CustomField> list = propertyGroupListCustomFieldMap.get(key);
                        List<HeaderDTO> headerCustom=list.stream().map(cf -> new HeaderDTO(cf)).collect(Collectors.toList());
                        headerDTOList.addAll(headerCustom);
                    }
                });

            }
*/

            //make column
            int colGroup=1;
            //basic
            colGroup = ExcelCommonUtils.makeHeader(colGroup,basicGroupStyle,basicColStyle,basicHeaders,"Basic Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,physicalGroupStyle,physicalColStyle,physicalHeaders,"Physical Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,runnerSystemGroupStyle,runnerSystemColStyle,runnerSystemHeaders,"Runner System Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,dynamicGroupStyle,dynamicColStyle,dynamicHeaders,"Dynamic Information",sheet, 0, 1, 3);
//            if (!isCustomServerName){
            colGroup = ExcelCommonUtils.makeHeader(colGroup, costGroupStyle, costColStyle, costHeaders, "Cost Information", sheet, 0, 1, 3);
//            }
            colGroup = ExcelCommonUtils.makeHeader(colGroup, slDepreciationGroupStyle, slDepreciationColStyle, slDepreciationHeaders, "Straight Line (S.L.) Depreciation", sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup, upDepreciationGroupStyle, upDepreciationColStyle, upDepreciationHeaders, "Units of Production (U.P.) Depreciation", sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,supplierGroupStyle,supplierColStyle,supplierHeaders,"Supplier Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,productionGroupStyle,productionColStyle,productionHeaders,"Production Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,maintenanceGroupStyle,maintenanceColStyle,maintenanceHeaders,"Maintenance Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,refurbishmentGroupStyle,refurbishmentColStyle,refurbishmentHeaders,"Refurbishment Information",sheet, 0, 1, 3);

            colGroup = ExcelCommonUtils.makeHeader(colGroup,workOrderGroupStyle,workOrderColStyle,workOrderHeaders,"Work Order Information",sheet, 0, 1, 3);
//            colGroup = ExcelCommonUtils.makeHeader(colGroup,partGroupStyle,partColStyle,partHeaders,"Part 1",sheet);
            //clear temp column
/*
            if (isCustomServerName){
                for(int i=colGroup;i<(colGroup+costHeaders.size());i++){
                    sheet.getRow(rowGroup).removeCell(ExcelCommonUtils.getOrCreateCell(sheet,rowGroup,i));
                    sheet.getRow(rowHeader).removeCell(ExcelCommonUtils.getOrCreateCell(sheet,rowHeader,i));
                }
            }
*/

            //make style cell value
            for(int r=rowData;r<5;r++){
                for (int c=0;c<colGroup;c++){
                    Cell cell=ExcelCommonUtils.getOrCreateCell(sheet,r,c);
                    cell.setCellStyle(valueStyle);
                }
/*
                if (isCustomServerName){
                    for(int i=colGroup;i<(colGroup+costHeaders.size());i++){
                        sheet.getRow(r).removeCell(ExcelCommonUtils.getOrCreateCell(sheet,r,i));
                    }
                }
*/
            }
            //put data

            BasicInformationDto basicInformationDto = new BasicInformationDto();
            PhysicalInformationDto physicalInformationDto = new PhysicalInformationDto();
            RunnerSystemInformationDto runnerSystemInformationDto = new RunnerSystemInformationDto();
            DynamicInformationDto dynamicInformationDto = new DynamicInformationDto();
            CostInformationDto costInformationDto = new CostInformationDto();
            SupplierInformationDto supplierInformationDto = new SupplierInformationDto();
            ProductionInformationDTO productionInformationDTO = new ProductionInformationDTO();
            MaintenanceInformationDto maintenanceInformationDto = new MaintenanceInformationDto();
            List<PartDto> parts;

            boolean assignPartNumber = false;
            int startRow = 3;
            //get data for custom field
            Map<Long, Map<Long,List<CustomFieldValue>>> mapValueCustomField=customFieldValueService.mapValueCustomField(ObjectType.TOOLING,molds.stream().map(m->m.getId()).collect(Collectors.toList()));
            List<Long> partIds=new ArrayList<>();
            molds.stream().forEach(m->{
                m.getParts().forEach(p->{
                    if(p.getPartId()!=null) partIds.add(p.getPartId());
                });
            });
            Map<Long, Map<Long,List<CustomFieldValue>>> mapValueCustomFieldPart=customFieldValueService.mapValueCustomField(ObjectType.PART,partIds);

            for(Mold mold : molds){
                exportService.bindData(mold, basicInformationDto);
                exportService.bindData(mold, physicalInformationDto);
                exportService.bindData(mold, runnerSystemInformationDto);
                exportService.bindData(mold, dynamicInformationDto,timezoneOffsetClient);
                exportService.bindData(mold, costInformationDto,timezoneOffsetClient);
                SlDepreciationDto slDepreciationDto =  new SlDepreciationDto(mold,timezoneOffsetClient);
                UpDepreciationDto upDepreciationDto =  new UpDepreciationDto(mold,timezoneOffsetClient);
                exportService.bindData(mold, supplierInformationDto);
                exportService.bindData(mold, productionInformationDTO);
                exportService.bindData(mold, maintenanceInformationDto);
                // Refurbishment Information
                String lastRefurbishmentDate =  DateTimeUtils.formatDateTimeWithTimeZone(mold.getLastRefurbishmentDate(), timezoneOffsetClient, DateUtils2.DatePattern.yyyy_MM_dd);
                //Work Order Information
                Optional<WorkOrder> optionalWorkOrder = BeanUtils.get(WorkOrderRepository.class).findFirstByStatusAndAssetIdOrderByCompletedOnDesc(WorkOrderStatus.COMPLETED, mold.getId());
                WorkOrderDTO workOrder = optionalWorkOrder.isPresent() ? BeanUtils.get(WorkOrderService.class).getWorkOrderDetailDTO(optionalWorkOrder.get().getId()) : null;
                Instant completedOn = workOrder != null ? workOrder.getCompletedOn() : null;
                String lastWorkOrderDate = org.apache.commons.lang3.StringUtils.defaultString(
                    DateTimeUtils.formatDateTimeWithTimeZone(completedOn, timezoneOffsetClient, DateUtils2.DatePattern.yyyy_MM_dd));

                parts = exportService.getPartDtos(mold);

                Row row = sheet.createRow(startRow);
                Cell cell = row.createCell(indexCell ++);
                cell.setCellValue(index);
                cell.setCellStyle(valueStyle);
//                Basic information
//                indexOfBasicInformation = indexCell;
                indexCell = importDataBasicInformationNew(basicInformationDto, indexCell, row, valueStyle, workbook, isCustomServerName, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,basicHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);

//                Physical Information
//                indexOfPhysicalInformationDto = indexCell;
                indexCell = importDataPhysicalInformation(physicalInformationDto, indexCell, row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,physicalHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);

//                indexOfRunnerSystem = indexCell;
                indexCell = importDataRunnerSystem(runnerSystemInformationDto, indexCell, row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,runnerSystemHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);

//                Dynamic Information
//                indexOfDynamic = indexCell;
                indexCell = importDataDynamicInformation(dynamicInformationDto, indexCell, row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,dynamicHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);
//                Cost Information
//                if(!isCustomServerName) {
//                    indexOfCostInformation = indexCell;
                indexCell = importDataCostInformation(costInformationDto, indexCell, row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,costHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);
//                }
                //SL Depreciation
                indexCell = exportDataSlDepreciation(slDepreciationDto, indexCell, row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,slDepreciationHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);
                //UP Depreciation
                indexCell = exportDataUpDepreciation(upDepreciationDto, indexCell, row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,upDepreciationHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);
//                Supplier Information
//                indexOfSupplierInformationDto = indexCell;
                indexCell = importDataSupplierInformationNew(supplierInformationDto, indexCell,row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,supplierHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);
                //production
                indexCell = importProductionInformation(productionInformationDTO, indexCell,row, valueStyle, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,productionHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);

//                Maintenance Information
//                indexOfMaintenanceInformationDto  = indexCell;
                indexCell = importDataMaintenanceInformationNew(maintenanceInformationDto, indexCell, row, valueStyle, workbook,  isCustomServerName, deleteProperties);
                indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,maintenanceHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        mold.getId(),mapValueCustomField,valueStyle,sheet);

                ExcelCommonUtils.bindDataToCell(lastRefurbishmentDate, row, indexCell++, valueStyle);
                ExcelCommonUtils.bindDataToCell(lastWorkOrderDate, row, indexCell++, valueStyle);


                indexOfPart = indexCell;
//                Count field use for create part label
                if(!assignMaxField){
                    maxField = indexCell;
                    assignMaxField = true;
                }

                for(PartDto partDto : parts) {
                    if(!assignPartNumber){
                        if(numberOfPart < parts.size()){
                            numberOfPart = parts.size();
                        }
                        assignPartNumber = true;
                    }
                    indexCell = exportDataPartInToolingNew(partDto, indexCell, row, valueStyle,deletePropertiesPart);
                    indexCell = ExcelCommonUtils.writeCellValue(startRow,indexCell,partHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                            partDto.getId(),mapValueCustomFieldPart,valueStyle,sheet);

                }
                assignPartNumber = false;
                indexCell = 0;
                startRow++;
                index++;
            }
            int startPart = maxField;
            int startLabelPart = startPart;
            int totalFieldPart = 10;
/*
            for(int count = 1; count <= numberOfPart; count++){
                createCellLabelPart(sheet, startLabelPart, styleOfLabel);
                Row row = sheet.createRow(0);
                Cell cell = row.createCell(startPart);
                cell.setCellValue("Part #" + count);
                int endRow = startPart + totalFieldPart;
                sheet.addMergedRegion(new CellRangeAddress(0,0, startPart, endRow-1));
                startLabelPart = startLabelPart + totalFieldPart;
                startPart = endRow;
            }
*/
            //make header for part
            for (int count = 1; count <= numberOfPart; count++) {
                startPart = ExcelCommonUtils.makeHeader(startPart, partGroupStyle, partColStyle, partHeaders, "Part " + count, sheet, 0, 1,3);
            }
            //make style for row less parts
            for (int r = rowData; r < (rowData + molds.size()); r++) {
                for (int col = maxField; col < (maxField + numberOfPart * partHeaders.size()); col++) {
                    Cell cell = ExcelCommonUtils.getOrCreateCell(sheet, r, col);
                    cell.setCellStyle(valueStyle);
                }
            }
            ExcelCommonUtils.removeRageCell(sheet,0,5,(maxField + numberOfPart * partHeaders.size()),70);

            workbook.write(outputStream);
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return outputStream;
    }

    private int importDataBasicInformationNew(BasicInformationDto basicInformationDto, int indexCell, Row row, CellStyle style, Workbook workbook, boolean isCustomServerName
            , List<String> deleteProperties) {
        Cell cellTooling = row.createCell(indexCell++);
        cellTooling.setCellValue(basicInformationDto.getToolingId());
        cellTooling.setCellStyle(style);

        Cell s = row.createCell(indexCell++);
        s.setCellValue(basicInformationDto.getSupplierMoldCode());
        s.setCellStyle(style);

        Cell counterID = row.createCell(indexCell++);
        counterID.setCellValue(basicInformationDto.getCounterId());
        counterID.setCellStyle(style);

        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.toolingLetter)) {
            Cell cellToolingLetter = row.createCell(indexCell++);
            cellToolingLetter.setCellValue(basicInformationDto.getToolingLetter());
            cellToolingLetter.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.toolingType)) {
            Cell toolingType = row.createCell(indexCell++);
            toolingType.setCellValue(basicInformationDto.getToolingType());
            toolingType.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.toolingComplexity)) {
            Cell toolingComplexity = row.createCell(indexCell++);
            toolingComplexity.setCellValue(basicInformationDto.getToolingComplexity());
            toolingComplexity.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.designedShot)) {
            Cell forecastedMaxShots = row.createCell(indexCell++);
            forecastedMaxShots.setCellValue(basicInformationDto.getForecastedMaxShots());
            forecastedMaxShots.setCellStyle(style);
        }
        if (!isCustomServerName) {
            if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.lifeYears)) {
                Cell forecastedToolLife = row.createCell(indexCell++);
                forecastedToolLife.setCellValue(basicInformationDto.getForecastedToolLife());
                forecastedToolLife.setCellStyle(style);
            }
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.madeYear)) {
            Cell yearOfToolMade = row.createCell(indexCell++);
            yearOfToolMade.setCellValue(basicInformationDto.getYeaOfToolMade());
            yearOfToolMade.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.engineers)) {
            Cell engineerInCharge = row.createCell(indexCell++);
            engineerInCharge.setCellValue(basicInformationDto.getEngineerInCharge());
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            cellStyle.cloneStyleFrom(style);
            cellStyle.setWrapText(true);
            engineerInCharge.setCellStyle(cellStyle);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.plantEngineers)) {
            Cell plantEngineerInCharge = row.createCell(indexCell++);
            plantEngineerInCharge.setCellValue(basicInformationDto.getPlantEngineerInCharge());
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            cellStyle.cloneStyleFrom(style);
            cellStyle.setWrapText(true);
            plantEngineerInCharge.setCellStyle(cellStyle);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.BASIC.toolDescription)) {
            Cell toolDescription = row.createCell(indexCell++);
            toolDescription.setCellValue(basicInformationDto.getToolDescription());
            toolDescription.setCellStyle(createCellStyleValueTopLeft(workbook));
        }
        return indexCell;
    }
    /*
        private int importDataCostInformationNew(CostInformationDto costInformationDto, int indexCell, Row row, CellStyle style){
            Cell costOfToolingInUSD = row.createCell(indexCell++);
            costOfToolingInUSD.setCellValue(costInformationDto.getCostOfTooling());
            costOfToolingInUSD.setCellStyle(style);

    //        Cell accumulatedMaintenanceCost = row.createCell(indexCell++);
    //        accumulatedMaintenanceCost.setCellValue(costInformationDto.getAccumulatedMaintenanceCost());
    //        accumulatedMaintenanceCost.setCellStyle(style);

            Cell memo = row.createCell(indexCell++);
            memo.setCellValue(costInformationDto.getMemo());
            memo.setCellStyle(style);

            return indexCell;
        }
    */
    private int importDataSupplierInformationNew(SupplierInformationDto supplierInformationDto, int indexCell, Row row, CellStyle style
            , List<String> deleteProperties){
        Cell supplier = row.createCell(indexCell++);
        supplier.setCellValue(supplierInformationDto.getSupplierName());
        supplier.setCellStyle(style);
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SUPPLIER.labour)) {
            Cell labour = row.createCell(indexCell++);
            labour.setCellValue(supplierInformationDto.getLabour());
            labour.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SUPPLIER.shiftsPerDay)) {

            Cell productionHoursPerDay = row.createCell(indexCell++);
            productionHoursPerDay.setCellValue(supplierInformationDto.getProductionHoursPerDays());
            productionHoursPerDay.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SUPPLIER.productionDays)) {
            Cell productionDayPerWeek = row.createCell(indexCell++);
            productionDayPerWeek.setCellValue(supplierInformationDto.getProductionDayPerWeek());
            productionDayPerWeek.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.SUPPLIER.maxCapacityPerWeek)) {
            Cell maximumCapacityPerWeek = row.createCell(indexCell++);
            maximumCapacityPerWeek.setCellValue(supplierInformationDto.getMaximumCapacityPerWeek());
            maximumCapacityPerWeek.setCellStyle(style);
        }
        return indexCell;

    }
    private int importProductionInformation(ProductionInformationDTO dto, int indexCell, Row row, CellStyle style
            , List<String> deleteProperties){

        if (!deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.toolmakerApprovedCycleTime)) {
            Cell approvedCycleTime = row.createCell(indexCell++);
            approvedCycleTime.setCellValue(dto.getToolmakerApprovedCycleTime());
            approvedCycleTime.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.approvedCycleTime)) {
            Cell approvedCycleTime = row.createCell(indexCell++);
            approvedCycleTime.setCellValue(dto.getApprovedCycleTime());
            approvedCycleTime.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.cycleTimeLimit1)) {
            Cell cycleTimeToleranceL1 = row.createCell(indexCell++);
            cycleTimeToleranceL1.setCellValue(dto.getCycleTimeToleranceL1());
            cycleTimeToleranceL1.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.cycleTimeLimit2)) {
            Cell cycleTimeToleranceL2 = row.createCell(indexCell++);
            cycleTimeToleranceL2.setCellValue(dto.getCycleTimeToleranceL2());
            cycleTimeToleranceL2.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.uptimeTarget)) {
            Cell uptimeTarget = row.createCell(indexCell++);
            uptimeTarget.setCellValue(dto.getTargetUptime());
            uptimeTarget.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.uptimeLimitL1)) {
            Cell uptimeToleranceL1 = row.createCell(indexCell++);
            uptimeToleranceL1.setCellValue(dto.getUptimeToleranceL1());
            uptimeToleranceL1.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PRODUCTION.uptimeLimitL2)) {
            Cell uptimeToleranceL2 = row.createCell(indexCell++);
            uptimeToleranceL2.setCellValue(dto.getUptimeToleranceL2());
            uptimeToleranceL2.setCellStyle(style);
        }
        return indexCell;
    }
    private int importDataMaintenanceInformationNew(MaintenanceInformationDto maintenanceInformationDto, int indexCell,
                                                    Row row, CellStyle style, Workbook workbook, boolean isCustomServerName
            , List<String> deleteProperties){
//        Cell conditionOfTooling = row.createCell(indexCell++);
//        conditionOfTooling.setCellValue(maintenanceInformationDto.getConditionOfTooling());
//        conditionOfTooling.setCellStyle(style);

        if (!deleteProperties.contains(Const.ColumnCode.tooling.MAINTENANCE.preventCycle)) {
            Cell maintenanceInterval = row.createCell(indexCell++);
            maintenanceInterval.setCellValue(maintenanceInformationDto.getMaintenanceInterval());
            maintenanceInterval.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.MAINTENANCE.preventUpcoming)) {
            Cell upcomingMaintenanceTolerance = row.createCell(indexCell++);
            upcomingMaintenanceTolerance.setCellValue(maintenanceInformationDto.getUpcomingMaintenanceTolerance());
            upcomingMaintenanceTolerance.setCellStyle(style);
        }
//        if (!deleteProperties.contains(Const.ColumnCode.tooling.MAINTENANCE.preventOverdue)) {
//            Cell overdueMaintenanceTolerance = row.createCell(indexCell++);
//            overdueMaintenanceTolerance.setCellValue(maintenanceInformationDto.getOverdueMaintenanceTolerance());
//            overdueMaintenanceTolerance.setCellStyle(style);
//        }
/*
        if(!isCustomServerName) {
            Cell status = row.createCell(indexCell++);
            status.setCellValue(maintenanceInformationDto.getStatus());
            status.setCellStyle(style);
        }
*/

/*
        Cell maintenanceDocument = row.createCell(indexCell++);
        maintenanceDocument.setCellValue(maintenanceInformationDto.getMaintenanceDocument());
        maintenanceDocument.setCellStyle(style);

        Cell instructionVideo = row.createCell(indexCell++);
        instructionVideo.setCellValue(maintenanceInformationDto.getInstructionVideo());
        instructionVideo.setCellStyle(style);
*/

        return indexCell;

    }
    private int exportDataPartInToolingNew(PartDto partDto, int indexCell, Row row, CellStyle style
            , List<String> deleteProperties){

        if (!deleteProperties.contains(Const.ColumnCode.part.category)) {
            Cell category = row.createCell(indexCell++);
            category.setCellValue(partDto.getCategory());
            category.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.EXPORT.projectName)) {
            Cell project = row.createCell(indexCell++);
            project.setCellValue(partDto.getProject());
            project.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.partCode)) {

            Cell partID = row.createCell(indexCell++);
            partID.setCellValue(partDto.getPartID());
            partID.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.name)) {
            Cell partName = row.createCell(indexCell++);
            partName.setCellValue(partDto.getPartName());
            partName.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.resinCode)) {
            Cell partResinCode = row.createCell(indexCell++);
            partResinCode.setCellValue(partDto.getPartResinCode());
            partResinCode.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.resinGrade)) {
            Cell partResinGrade = row.createCell(indexCell++);
            partResinGrade.setCellValue(partDto.getPartResinGrade());
            partResinGrade.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.designRevision)) {
            Cell designRevisionLevel = row.createCell(indexCell++);
            designRevisionLevel.setCellValue(partDto.getDesignRevisionLevel());
            designRevisionLevel.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.size)) {
            Cell partVolume = row.createCell(indexCell++);
            partVolume.setCellValue(partDto.getPartVolumn());
            partVolume.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.weight)) {
            Cell partWeight = row.createCell(indexCell++);
            partWeight.setCellValue(partDto.getPartWeight());
            partWeight.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.tooling.PART.cavity)) {
            Cell numberOfCavities = row.createCell(indexCell++);
            numberOfCavities.setCellValue(partDto.getNumberOfCavities());
            numberOfCavities.setCellStyle(style);
        }
        return indexCell;

    }

    public void updatePartHeader(final List<HeaderDTO> basicPartHeaders ,final List<String> deletePropertiesFinal,boolean isTemplate){

//            propertyGroupListHeaderMap.keySet().stream().forEach(key->{
//                allHeader.addAll(propertyGroupListHeaderMap.get(key));
//            });

		//process for config field
        if (OptionUtils.isEnabled(ConfigCategory.PART)) {
            Map<String,Boolean> mapRequiredField = new HashMap<>();
            GeneralConfigPayload payload = new GeneralConfigPayload();
            payload.setConfigCategory(ConfigCategory.PART);
            List<GeneralConfig> generalConfigList=generalConfigService.findAll(payload.getPredicate());
            generalConfigList.stream().forEach(cfg->{
                if(!StringUtils.isEmpty(cfg.getFieldName()))
                    mapRequiredField.put(cfg.getFieldName(),cfg.getRequired());
            });
            //process for delete exists properties
            List<String> deleteProperties=generalConfigList.stream().filter(generalConfig -> generalConfig.getDeletedField()!=null
                    && generalConfig.getDeletedField()).map(g->g.getFieldName()).collect(Collectors.toList());

            //default value map
            Map<String, String> defaultValueMap =  generalConfigList.stream()
                    .filter(generalConfig -> generalConfig.getDefaultInput() != null && generalConfig.getDefaultInput() && generalConfig.getDefaultInputValue() != null)
                    .collect(Collectors.toMap(GeneralConfig::getFieldName, GeneralConfig::getDefaultInputValue, (o, n) -> n));

            //for dependent field
            if(isTemplate){
                if(deleteProperties.contains(Const.ColumnCode.part.size)){
                    deleteProperties.add(Const.ColumnCode.part.IMPORT.size_w);
                    deleteProperties.add(Const.ColumnCode.part.IMPORT.size_d);
                    deleteProperties.add(Const.ColumnCode.part.IMPORT.size_h);
                    deleteProperties.add(Const.ColumnCode.part.IMPORT.sizeUnit);
                }
                if(deleteProperties.contains(Const.ColumnCode.part.weight)){
                    deleteProperties.add(Const.ColumnCode.part.IMPORT.weightUnit);
                }
                if(deleteProperties.contains(Const.ColumnCode.part.category)){
                    deleteProperties.add(Const.ColumnCode.part.quantityRequired);
                }
            }else {
                if(deleteProperties.contains(Const.ColumnCode.part.category)){
                    deleteProperties.add(Const.ColumnCode.part.EXPORT.categoryName);
                    deleteProperties.add(Const.ColumnCode.part.EXPORT.projectName);
                }

            }
            deletePropertiesFinal.addAll(deleteProperties);
            List<HeaderDTO> deleteList=basicPartHeaders.stream().filter(headerDTO -> deletePropertiesFinal.contains(headerDTO.getCode()))
                    .collect(Collectors.toList());

            basicPartHeaders.removeAll(deleteList);

            List<HeaderDTO> allHeaderPart=new ArrayList<>();
            allHeaderPart.addAll(basicPartHeaders);
            //update require
            allHeaderPart.stream().forEach(h->{
                if(mapRequiredField.containsKey(h.getCode()) && mapRequiredField.get(h.getCode())!=null){
                    h.setRequired(mapRequiredField.get(h.getCode()));
                } else if (Arrays.asList("size_w", "size_d", "size_h",Const.ColumnCode.part.IMPORT.sizeUnit).contains(h.getCode()) && mapRequiredField.containsKey("size") && mapRequiredField.get("size") != null) {
                    h.setRequired(mapRequiredField.get("size"));
                } else if (Arrays.asList(Const.ColumnCode.part.IMPORT.weightUnit).contains(h.getCode())
                        && mapRequiredField.containsKey(Const.ColumnCode.part.weight) && mapRequiredField.get(Const.ColumnCode.part.weight) != null) {
                    h.setRequired(mapRequiredField.get(Const.ColumnCode.part.weight));
                }
                if(Arrays.asList(Const.ColumnCode.part.quantityRequired,Const.ColumnCode.part.EXPORT.projectName,Const.ColumnCode.part.EXPORT.categoryName).contains(h.getCode())){
                    boolean required=false;
                    if(mapRequiredField.containsKey(Const.ColumnCode.part.category)) required = mapRequiredField.get(Const.ColumnCode.part.category);
                    h.setRequired(required);
                }
            });

            //update default value
            basicPartHeaders.forEach(h->{
                if(defaultValueMap.containsKey(h.getCode()) && defaultValueMap.get(h.getCode())!=null){
                    h.setDefaultValue(defaultValueMap.get(h.getCode()));
                } else if (Arrays.asList("size_w", "size_d", "size_h",Const.ColumnCode.part.IMPORT.sizeUnit).contains(h.getCode()) && defaultValueMap.containsKey("size") && defaultValueMap.get("size") != null) {
                    h.setDefaultValue(defaultValueMap.get("size"));
                } else if (Arrays.asList(Const.ColumnCode.part.IMPORT.weightUnit).contains(h.getCode())
                        && defaultValueMap.containsKey(Const.ColumnCode.part.weight) && defaultValueMap.get(Const.ColumnCode.part.weight) != null) {
                    h.setDefaultValue(defaultValueMap.get(Const.ColumnCode.part.weight));
                }
                if(Arrays.asList(Const.ColumnCode.part.quantityRequired,Const.ColumnCode.part.EXPORT.projectName,Const.ColumnCode.part.EXPORT.categoryName).contains(h.getCode())){
                    if(defaultValueMap.containsKey(Const.ColumnCode.part.category))
                        h.setDefaultValue(defaultValueMap.get(Const.ColumnCode.part.category));
                }
            });

            //update custom field
            List<CustomField> customFieldList = new ArrayList<>();
            customFieldList = customFieldRepository.findByObjectTypeOrderByFieldNameAsc(ObjectType.PART);

            List<HeaderDTO> headerCustom=customFieldList.stream().map(cf -> new HeaderDTO(cf)).collect(Collectors.toList());
            basicPartHeaders.addAll(headerCustom);
        }

    }
    public ByteArrayOutputStream exportExcelPartListNew(List<Part> parts, String timeRange){
        InputStream fileTemplate = getFileTemplateExcel("part-export-template.xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);

            Sheet sheet = workbook.getSheetAt(0);


            int rowGroup = 0;
            int rowHeader = 1;
            int rowData = 2;

//            Default
            int indexOfBasicInformation = 0;

            //style for lable
            CellStyle basicGroupStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowGroup,indexOfBasicInformation);
            CellStyle basicColStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowHeader,indexOfBasicInformation);
            CellStyle valueStyle= ExcelCommonUtils.cloneCellStyle(sheet,rowData,indexOfBasicInformation);

            // header default
            List<HeaderDTO> partHeaders = new ArrayList<>(Arrays.asList(
                    new HeaderDTO("SEQ", "SEQ",2000),
                    new HeaderDTO("categoryName", "Category Name"),
                    new HeaderDTO("projectName", "Product Name")
                    ,new HeaderDTO("partCode", "Part ID", true)
                    ,new HeaderDTO("name", "Part Name", true)
                    , new HeaderDTO("resinCode", "Part Resin Code")
//                    , new HeaderDTO("resinGrade", "Part Resin Grade")
                    , new HeaderDTO("designRevision", "Design Revision Level")
                    , new HeaderDTO("size", "Part Volume (W x D x H)")
                    , new HeaderDTO("weight", "Part Weight")
                    , new HeaderDTO("weeklyDemand", "Weekly Demand")
                    , new HeaderDTO("quantityRequired", "Quantity Required")
            ));

//            Map<PropertyGroup,List<HeaderDTO>> propertyGroupListHeaderMap=new HashMap<>();
//            propertyGroupListHeaderMap.put(PropertyGroup.TOOLING_BASIC,basicHeaders);
            List<String> deleteProperties=new ArrayList<>();

            updatePartHeader(partHeaders,deleteProperties,false);
            //make column
            int colGroup=0;
            //basic
            colGroup=ExcelCommonUtils.makeHeader(colGroup,basicGroupStyle,basicColStyle,partHeaders,"Part Information",sheet);
            //make style cell value
//            for(int r=rowData;r<4;r++){
//                for (int c=0;c<basicHeaders.size();c++){
//                    Cell cell=ExcelCommonUtils.getOrCreateCell(sheet,r,c);
//                    cell.setCellStyle(valueStyle);
//                }
//            }

            final int[] startRow = {2};
            final int[] index = {1};
            final int[] indexCell = {0};
/*

            CellStyle styleOfLabel = createCellStyleForLabelPart(workbook);
            CellStyle styleOfValue = createCellStyleValue(workbook);
*/

            // Create time range header
/*
            Row aRow = sheet.createRow(0);
            Cell aCell = aRow.createCell(0);
            aCell.setCellValue("Time: " + timeRange);
            aCell.setCellStyle(styleOfLabel);
*/
            //get data for custom field
            Map<Long, Map<Long,List<CustomFieldValue>>> mapValueCustomFieldPart=customFieldValueService.mapValueCustomField(ObjectType.PART,parts.stream().map(m->m.getId()).collect(Collectors.toList()));

            parts.forEach(part -> {
                Row row = sheet.createRow(startRow[0]);
                Cell cell = row.createCell(indexCell[0]++);
                cell.setCellValue(index[0]);
                cell.setCellStyle(valueStyle);

                indexCell[0] = exportDataPartNew(part, indexCell[0], row, valueStyle, workbook,deleteProperties);
                ExcelCommonUtils.writeCellValue(startRow[0],indexCell[0],partHeaders.stream().filter(h->h.isCustomField()).collect(Collectors.toList()),
                        part.getId(),mapValueCustomFieldPart,valueStyle,sheet);

                indexCell[0] = 0;
                startRow[0]++;
                index[0]++;
            });
            ExcelCommonUtils.removeRageCell(sheet,0,5,colGroup,10);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream;
    }

    private int exportDataPartNew(Part part, int indexCell, Row row, CellStyle style, Workbook workbook
            , List<String> deleteProperties){
        if (!deleteProperties.contains(Const.ColumnCode.part.category)) {
            Cell categoryName = row.createCell(indexCell++);
            categoryName.setCellValue(part.getCategoryName());
            categoryName.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.category)) {
            Cell categoryName = row.createCell(indexCell++);
            categoryName.setCellValue(part.getProjectName());
            categoryName.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.partCode)) {
            Cell partCode = row.createCell(indexCell++);
            partCode.setCellValue(part.getPartCode());
            partCode.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.name)) {
            Cell partName = row.createCell(indexCell++);
            partName.setCellValue(part.getName());
            partName.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.resinCode)) {
            Cell resinCode = row.createCell(indexCell++);
            resinCode.setCellValue(part.getResinCode());
            resinCode.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.designRevision)) {
            Cell designRevesion = row.createCell(indexCell++);
            designRevesion.setCellValue(part.getDesignRevision());
            designRevesion.setCellStyle(style);

        }
        if (!deleteProperties.contains(Const.ColumnCode.part.size)) {
            Cell partVolume = row.createCell(indexCell++);
            partVolume.setCellValue(part.getPartSize());
            partVolume.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.weight)) {
            Cell partWeight = row.createCell(indexCell++);
            partWeight.setCellValue(part.getPartWeight());
            partWeight.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.weeklyDemand)) {
            Cell weeklyDemand = row.createCell(indexCell++);
            weeklyDemand.setCellValue(part.getWeeklyDemand() != null ? part.getWeeklyDemand().toString() : "");
            weeklyDemand.setCellStyle(style);
        }
        if (!deleteProperties.contains(Const.ColumnCode.part.quantityRequired)) {
            Cell quantityRequired = row.createCell(indexCell++);
            quantityRequired.setCellValue(part.getQuantityRequired() != null ? part.getQuantityRequired().toString() : "");
            quantityRequired.setCellStyle(style);
        }
        return indexCell;
    }

    public static void main(String[] args) throws IOException {
        new ExcelUtils().readExcel();
    }

//    public ByteArrayOutputStream writeData(List<MoldDtoPDF> moldDtoPDFS) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try {
//            InputStream fileTemplate = new FileInputStream(getFileTemplateExcel("tooling-detail-template-update-cost.xlsx"));
//
//            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
//
//            Sheet sheet = workbook.getSheetAt(0);
//
////            CellStyle cs = workbook.createCellStyle();
////            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//
//            String rgbS = "F2F2F2";
//            byte[] rgbB = Hex.decodeHex(rgbS); // get byte array from hex string
//            XSSFColor color = new XSSFColor(rgbB, null); //IndexedColorMap has no usage until now. So it can be set null.
//
//            XSSFCellStyle cs = (XSSFCellStyle) workbook.createCellStyle();
//            cs.setFillForegroundColor(color);
//            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//            int startCell = 0;
//            int rowCount = 2;
//            for (MoldDtoPDF moldDtoPDF : moldDtoPDFS) {
//                Row row = sheet.createRow(rowCount++);
//                Cell toolingID = row.createCell(startCell++);
//                toolingID.setCellValue(moldDtoPDF.getToolingID());
//
//                Cell counter = row.createCell(startCell++);
//                counter.setCellValue(moldDtoPDF.getCounter());
//
//
//                Cell location = row.createCell(startCell++);
//                location.setCellValue(moldDtoPDF.getLocation());
//
//
//                Cell lastShot = row.createCell(startCell++);
//                lastShot.setCellValue(moldDtoPDF.getLastShot());
//
//
//                Cell cycleTime = row.createCell(startCell++);
//                cycleTime.setCellValue(moldDtoPDF.getCycleTime());
//
//
//                Cell op = row.createCell(startCell++);
//                op.setCellValue(moldDtoPDF.getOp());
//
//                Cell status = row.createCell(startCell++);
//                status.setCellValue(moldDtoPDF.getStatus());
//
//                if(rowCount %2 == 1) {
//                    counter.setCellStyle(cs);
//                    toolingID.setCellStyle(cs);
//                    location.setCellStyle(cs);
//                    lastShot.setCellStyle(cs);
//                    op.setCellStyle(cs);
//                    status.setCellStyle(cs);
//                    cycleTime.setCellStyle(cs);
//                }
//                startCell = 0;
//            }
////            FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/fileExcel.xlsx"));
////            workbook.write(fileOutputStream);
//            workbook.write(outputStream);
//            workbook.close();
//            return outputStream;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (DecoderException e) {
//            e.printStackTrace();
//        }
//        return outputStream;
//
//    }

}
