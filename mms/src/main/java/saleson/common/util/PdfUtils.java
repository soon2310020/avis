package saleson.common.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import saleson.common.service.ExportService;
import saleson.model.Mold;
import saleson.restdocs.dto.MoldDtoPDF;
import saleson.restdocs.dto.exports.*;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class PdfUtils {
    String PAGE_NUM = "PAGE_NUM"; // Page number of pdf
    @Autowired
    private ExportService exportService;

    @Value("${customer.server.name}")
    private String customerServerName;

    public ByteArrayInputStream createPdfFile() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);
//        PdfFooterHeaderUtils event = new PdfFooterHeaderUtils();
//        writer.setPageEvent(event);
        document.open();

        PdfPTable table = new PdfPTable(10);
//        float[] widths = {70, 200, 70, 200};
//        table.setTotalWidth(widths);
        table.setTotalWidth(540);
//        int[] widths = {70, 200, 70, 200};
//        table.setWidths(widths);
        table.setLockedWidth(true);
//        table.SetWidthPercentage(4, PageSize.A4);
//        int[] widths = {40, 100, 60, 100};
//        table.setWidths(widths);

        addTableHeader(table);
        addTableLabel(table);
//        addCells(table, caseDTO);

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private Font textFont(int size, int style) {
//        final String FONT = "resources/fonts/NotoSansKR-Regular.ttf";
//        Font font = FontFactory.getFont(FONT, size, style);
        BaseFont bf = null;
        try {
            bf = BaseFont.createFont(
                    BaseFont.TIMES_ROMAN,
                    BaseFont.CP1252,
                    BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font font = new Font(bf, size, style);
        return font;
    }

    private void addTableHeader(PdfPTable table) {
        Font fontH1 = textFont(20, Font.NORMAL);
        fontH1.setColor(38, 52, 69);
        PdfPCell header = new PdfPCell(new Phrase("Tooling Report", fontH1));
        header.setMinimumHeight(45);
        header.setColspan(12);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_CENTER);
        header.setBorder(Rectangle.NO_BORDER);
        table.addCell(header);
    }

    private void addTableLabel(PdfPTable table) {
        Font font = textFont(10, Font.NORMAL);
//        font.setColor(BaseColor.WHITE);
        Font fontBold = textFont(10, Font.BOLD);
//        fontBold.setColor(BaseColor.WHITE);
        float padding = 2;

        table.addCell(createLabelCell("Tooling ID", fontBold, 2, padding));
        table.addCell(createLabelCell("Counter", fontBold, 2, padding));
        table.addCell(createLabelCell("Location", fontBold, 2, padding));
        table.addCell(createLabelCell("Last Shot", fontBold, 1, padding));
        table.addCell(createLabelCell("Cycle Time (sec)", fontBold, 1, padding));
        table.addCell(createLabelCell("OP", fontBold, 1, padding));
        table.addCell(createLabelCell("Status", fontBold, 1, padding));
    }

    private PdfPCell createLabelCell(String text, Font font, int numbCol, float padding) {
        PdfPCell label = new PdfPCell(new Phrase(text, font));
        label.setBackgroundColor(new BaseColor(38, 52, 69));
        label.setBorder(Rectangle.NO_BORDER);
        label.setMinimumHeight(18);
        if (padding == 0) {
            label.setVerticalAlignment(Element.ALIGN_MIDDLE);
        } else {
            label.setPaddingTop(padding);
        }
        label.setColspan(numbCol);
        return label;
    }

    private InputStream getFileTemplate(String fileName) throws IOException {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader.getResource("classpath:exports/pdf/" + fileName);
        InputStream fileInputStream = storeFile.getInputStream();
        return fileInputStream;
    }

    private String createDateExport(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        return simpleDateFormat.format(new Date());
    }

    public JasperPrint createJasperPrintDataMoldDetail(BasicInformationDto basicInformationDto, List<saleson.restdocs.dto.exports.PartDto> partDtos, DynamicInformationDto dynamicInformationDto,
                                                       PhysicalInformationDto physicalInformationDto, MaintenanceInformationDto maintenanceInformationDto,
                                                       SupplierInformationDto supplierInformationDto, RunnerSystemInformationDto runnerSystemInformationDto,
                                                       CostInformationDto costInformationDto, String toolingID, String fileName) throws IOException, JRException {
//        InputStream input = new URL("https://eshotlink-dev.s3-ap-southea  st-1.amazonaws.com/resources/exports/pdf/tooling-detail-template.jrxml").openStream();
        JasperReport jasperReport;
        boolean isCustomServerName = "dyson".equals(this.customerServerName);
        if(!isCustomServerName) {
//            jasperReport = JasperCompileManager.compileReport(getFileTemplate("tooling-detail-template.jrxml"));
            jasperReport = JasperCompileManager.compileReport(getFileTemplate("tooling-detail-template-update-cost.jrxml"));

        }else{
            jasperReport = JasperCompileManager.compileReport(getFileTemplate("tooling-detail-template-dyson.jrxml"));
        }
        //        JasperReport jasperReport = JasperCompileManager.compileReport(input);
        Map<String, Object> parameter = new HashMap<>();
//            Label
        parameter.put("fileName", fileName);
        parameter.put("toolingId", "Tooling ID: " +  (toolingID == null ? "" : toolingID));
//        parameter.put("poweredBy", "Powered by eMoldino");
//        parameter.put("dateExported", "Date exported: " + createDateExport());
        parameter.put("pageNum", PAGE_NUM);
        parameter.putAll(pushAllLabel(isCustomServerName));
//            Dữ liệu của các trường trong pdf
        parameter.putAll(pushAllDataValue(basicInformationDto, partDtos, dynamicInformationDto, physicalInformationDto,
                maintenanceInformationDto, supplierInformationDto, runnerSystemInformationDto, costInformationDto, isCustomServerName));
        return JasperFillManager.fillReport(jasperReport, parameter, new JREmptyDataSource());
    }

    //    Mark page num for pdf
    private List<JasperPrint> markPageNum(List<JasperPrint> jasperPrints) {
        int pageNum = 1;
        int totalPage = 0;
        List<JasperPrint> prints = new ArrayList<>();
        if (jasperPrints == null) return prints;
        for (JasperPrint jasperPrint : jasperPrints) {
            totalPage += jasperPrint.getPages().size();
        }

        for (JasperPrint jasperPrint : jasperPrints) {
            List<JRPrintPage> pages = jasperPrint.getPages();
            for (JRPrintPage page : pages) {
                List<JRPrintElement> elements = page.getElements();
                for (JRPrintElement element : elements) {
                    if (element instanceof JRPrintText) {
                        JRPrintText text = (JRPrintText) element;
                        if (PAGE_NUM.equals(text.getValue())) {
                            text.setText("page " + pageNum + " of " + totalPage);
                        }
                    }
                }
                pageNum++;
            }
            prints.add(jasperPrint);
        }
        return prints;
    }

    public ByteArrayOutputStream mergJasperPrints(List<JasperPrint> jasperPrints){
        JRPdfExporter jrPdfExporter = new JRPdfExporter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        jrPdfExporter.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jasperPrints);
        jrPdfExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
        try {
            jrPdfExporter.exportReport();
        } catch (JRException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    private Map<String, Object> pushParameterLabelToBasicInformation() {
        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("basicInformation", "Basic Information (Static)");
        parameter.put("toolingIdLabel", "Tooling ID");
        parameter.put("toolingLetterLabel", "Tooling Letter");
        parameter.put("toolingTypeLabel", "Tooling Type");
        parameter.put("toolingComplexityLabel", "Tooling Complexity");
        parameter.put("counterIdLabel", "Sensor ID");
        parameter.put("familyToolLabel", "Family Tool");
        parameter.put("forecastedMaxShotsLabel", "Forecasted Max Shot");
        parameter.put("forecastedToolLifeLabel", "Forecasted Tool Life");
        parameter.put("yeaOfToolMadeLabel", "Year of Tool Made");
        parameter.put("approvedCycleTimeLabel", "Approved Cycle Time");
        parameter.put("toolDescriptionLabel", "Tool Description");
        return parameter;
    }

    private Map<String, Object> pushLabelOfPhysicalInformation() {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("physicalInformationLabel", "Physical Information");
        parameter.put("toolSizeLabel", "Tool Size");
        parameter.put("toolWeightLabel", "Tool Weight");
        parameter.put("shotSizeLabel", "Shot Weight");
        parameter.put("toolingMakerLabel", "Toolmaker");
        parameter.put("injectionMoldingMachineIDLabel","Injection Molding Machine ID");
        parameter.put("machineTonnageQuoteLabel","Machine Tonnage");
        parameter.put("conditionOfToolingLabel", "Tooling Picture");
//        parameter.put("machineTonnageCurrentProductionLabel","Machine Tonnage-current production");
        return parameter;
    }

    private Map<String, Object> pushLabelOfDynamicInformation() {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("dynamicInformationLabel", "Dynamic Information");
        parameter.put("opLabel", "OP");
        parameter.put("noOfShotsLabel", "Number of Shots");
        parameter.put("lastDateOfShotsLabel", "Last Date of Shots");
        parameter.put("utilisationRateLabel", "Utilisation Rate");
        parameter.put("locationLabel", "Location");
        parameter.put("cycleTimeLabel", "Cycle Time");
        return parameter;
    }

    private Map<String, Object> pushLabelOfCost(){
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("costInformation", "Cost Information");
        parameter.put("costOfToolingLabel", "Cost of Tooling");
        parameter.put("accumulatedMaintenanceCostLabel", "Accumulated Maintenance Cost");
        parameter.put("memoLabel", "Memo");
        return parameter;
    }

    private Map<String, Object> pushDataOfCost(CostInformationDto costInformationDto){
//        Fake data
        costInformationDto = new CostInformationDto();
        costInformationDto.setCostOfTooling("Input method: free entry");
        costInformationDto.setAccumulatedMaintenanceCost("Calculated using the maintenance cost input by the supplier in Corrective Maintenance feature (see task #182)");
        costInformationDto.setMemo("Input method: free entry ");
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("costOfToolingInUSDValue", costInformationDto.getCostOfTooling());
        parameter.put("accumulatedMaintenanceCostValue", costInformationDto.getAccumulatedMaintenanceCost());
        parameter.put("memoValue", costInformationDto.getMemo());
        return parameter;
    }

    private Map<String, Object> pushLabelOfMaintenanceInformation() {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("maintenanceInformationLabel", "Maintenance Information");
        parameter.put("maintenanceIntervalLabel", "Maintenance Interval");
//        parameter.put("maintenanceToleranceLabel", "Maintenance Tolerance");
        parameter.put("upcomingMaintenanceToleranceLabel", "Upcoming Maintenance Tolerance");
        parameter.put("overdueMaintenanceToleranceLabel", "Overdue Maintenance Tolerance");
        parameter.put("cycleTimeToleranceL1Label", "Cycle Time Tolerance L1");
        parameter.put("cycleTimeToleranceL2Label", "Cycle Time Tolerance L2");
        parameter.put("statusMaintenanceLabel", "Status");
        parameter.put("engineerInChargeLabel","Engineer in Charge");
        parameter.put("maintenanceDocumentLabel", "Maintenance Document");
        parameter.put("instructionVideoLabel", "Instruction Video");
        return parameter;
    }

    private Map<String, Object> pushLabelOfCostInformation() {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("costInformationLabel", "Cost Information");
        parameter.put("costOfToolingLabel", "Cost of Tooling (USD)");
        parameter.put("accumulatedMaintenanceCostLabel", "Accumulated Maintenance Cost (USD)");
        return parameter;
    }

    private Map<String, Object> pushLabelOfSupplierInformation() {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("supplierInformationLabel", "Supplier Information");
        parameter.put("targetUptimeLabel", "Uptime Target");
        parameter.put("supplierLabel", "Supplier");
        parameter.put("uptimeToleranceL1Label", "Uptime Tolerance L1");
        parameter.put("uptimeToleranceL2Label", "Uptime Tolerance L2");
        parameter.put("labourLabel", "Required Labor");
        parameter.put("supplierNameLabel", "Supplier Name");
        parameter.put("supplierCodeLabel", "Supplier Code");
        parameter.put("productionHoursPerDaysLabel", "Production Hour Per Day");
        parameter.put("productionDayPerWeekLabel", "Production Day per Week");
        parameter.put("maximumCapacityPerWeekLabel", "Maximum Capacity per Week");
        return parameter;
    }

    private Map<String, Object> pushLabelOfPart(int partNumber) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("partLabel_" + partNumber, "Part #" + partNumber);
        parameter.put("categoryLabel_" + partNumber, "Category");
        parameter.put("projectLabel_" + partNumber, "Project");
        parameter.put("partIDLabel_" + partNumber, "Part ID");
        parameter.put("partNameLabel_" + partNumber, "Part Name");
        parameter.put("partResinCodeLabel_" + partNumber, "Part Resin Code");
        parameter.put("partResinGradeLabel_" + partNumber, "Part Resin Grade");
        parameter.put("designRevisionLevelLabel_" + partNumber, "Design Revision Level");
        parameter.put("partVolumnLabel_" + partNumber, "Part Volume");
        parameter.put("partWeightLabel_" + partNumber, "Part Weight");
        parameter.put("numberOfCavitiesLabel_" + partNumber, "Number of Cavities");
        return parameter;
    }

    private Map<String, Object> pushLabelOfRunnerSystemInformation(){
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("runnerSystemInformationLabel", "Runner System Information");
        parameter.put("typeOfRunnerSystemLabel", "Type of Runner System");
        parameter.put("makerOfRunnerSystemLabel", "Maker of Runner System");
        parameter.put("weightOfRunnerSystemLabel", "Weight of Runner System");
        parameter.put("hotRunnerNumberOfDropLabel", "Hot Runner Number of Drop");
        parameter.put("hotRunnerZoneLabel", "Hot Runner Zone");

        return parameter;
    }

    private Map<String, Object> pushDataOfRunnerSystemInformation(RunnerSystemInformationDto runnerSystemInformationDto){
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("typeOfRunnerSystemValue", runnerSystemInformationDto.getTypeOfRunnerSystem());
        parameter.put("makerOfRunnerSystemValue", runnerSystemInformationDto.getMakerOfRunnerSystem());
        parameter.put("weightOfRunnerSystemValue", runnerSystemInformationDto.getWeightOfRunnerSystem());
        parameter.put("hotRunnerNumberOfDropValue", runnerSystemInformationDto.getHotRunnerNumberOfDrop());
        parameter.put("hotRunnerZoneValue", runnerSystemInformationDto.getHotRunnerZone());
        return parameter;
    }

    private Map<String, Object> pushDataOfBasicInformation(BasicInformationDto basicInformationDto) {
        Map<String, Object> parameter = new HashMap<>();
        if (basicInformationDto == null) return parameter;
        parameter.put("toolingLetterValue", basicInformationDto.getToolingLetter());
        parameter.put("toolingIdValue", basicInformationDto.getToolingId());
        parameter.put("toolingTypeValue", basicInformationDto.getToolingType());
        parameter.put("toolingComplexityValue", basicInformationDto.getToolingComplexity());
        parameter.put("counterIdValue", basicInformationDto.getCounterId());
        parameter.put("familyToolValue", basicInformationDto.getFamilyTool());
        parameter.put("forecastedMaxShotsValue", basicInformationDto.getForecastedMaxShots());
        parameter.put("forecastedToolLifeValue", basicInformationDto.getForecastedToolLife());
        parameter.put("yeaOfToolMadeValue", basicInformationDto.getYeaOfToolMade());
        parameter.put("approvedCycleTimeValue", basicInformationDto.getApprovedCycleTime());
        parameter.put("toolDescriptionValue", basicInformationDto.getToolDescription());
        return parameter;
    }

    private Map<String, Object> pushDataOfPart(saleson.restdocs.dto.exports.PartDto partDto, int partNumber) {
        Map<String, Object> parameter = new HashMap<>();
        if (partDto == null) return parameter;
        parameter.put("categoryValue_" + partNumber, partDto.getCategory());
        parameter.put("projectValue_" + partNumber, partDto.getProject());
        parameter.put("partIDValue_" + partNumber, partDto.getPartID());
        parameter.put("partNameValue_" + partNumber, partDto.getPartName());
        parameter.put("partResinCodeValue_" + partNumber, partDto.getPartResinCode());
        parameter.put("partResinGradeValue_" + partNumber, partDto.getPartResinGrade());
        parameter.put("designRevisionLevelValue_" + partNumber, partDto.getDesignRevisionLevel());
        parameter.put("partVolumnValue_" + partNumber, partDto.getPartVolumn());
        parameter.put("partWeightValue_" + partNumber, partDto.getPartWeight());
        parameter.put("numberOfCavities_" + partNumber, partDto.getNumberOfCavities());
        return parameter;
    }

    private Map<String, Object> processPartLabelAndValue(List<saleson.restdocs.dto.exports.PartDto> partDtos) {
        Map<String, Object> parameter = new HashMap<>();
        if (partDtos != null && partDtos.size() > 0) {
            int countPart = 1;
            for (saleson.restdocs.dto.exports.PartDto partDto : partDtos) {
                parameter.putAll(pushLabelOfPart(countPart));
                parameter.putAll(pushDataOfPart(partDto, countPart++));
            }
        }
        return parameter;
    }

    private Map<String, Object> pushDataOfPhysicalInformation(PhysicalInformationDto physicalInformationDto) {
        Map<String, Object> parameter = new HashMap<>();
        if (physicalInformationDto == null) return parameter;
        parameter.put("toolSizeValue", physicalInformationDto.getToolSize());
        parameter.put("toolWeightValue", physicalInformationDto.getToolWeight());
        parameter.put("shotSizeValue", physicalInformationDto.getShotSize());
//        parameter.put("typeOfRunnerSystemValue", physicalInformationDto.getTypeOfRunnerSystem());
//        parameter.put("makerOfRunnerSystemValue", physicalInformationDto.getMakerOfRunnerSystem());
//        parameter.put("weightOfRunnerSystemValue", physicalInformationDto.getWeightOfRunnerSystem());
        parameter.put("toolingMakerValue", physicalInformationDto.getToolingMaker());
        parameter.put("injectionMoldingMachineIDValue", physicalInformationDto.getInjectionMoldingMachineID());
        parameter.put("machineTonnageQuoteValue", physicalInformationDto.getMachineTonnageQuote());
        parameter.put("machineTonnageCurrentProductionValue", physicalInformationDto.getMachineTonnageCurrentProduction());
//        parameter.put("yearOfToolMadeValue", physicalInformationDto.getYearOfToolMade());
        return parameter;
    }

    private Map<String, Object> pushDataOfDynamicInformation(DynamicInformationDto dynamicInformationDto) {
        Map<String, Object> parameter = new HashMap<>();
        if (dynamicInformationDto == null) return parameter;
        parameter.put("opValue", dynamicInformationDto.getOp());
        parameter.put("noOfShotsValue", dynamicInformationDto.getNoOfShots());
        parameter.put("lastDateOfShotsValue", dynamicInformationDto.getLastDateOfShots());
        parameter.put("utilisationRateValue", dynamicInformationDto.getUtilisationRate() != null ? dynamicInformationDto.getUtilisationRate().replaceAll("\\(", " (") : "");
        parameter.put("locationValue", dynamicInformationDto.getLocation());
        parameter.put("cycleTimeValue", dynamicInformationDto.getCycleTime());
        parameter.put("weightedAverageCycleTime", dynamicInformationDto.getWeightedAverageCycleTime());
        return parameter;
    }

    private Map<String, Object> pushDataOfMaintenanceInformation(MaintenanceInformationDto maintenanceInformationDto) {
        Map<String, Object> parameter = new HashMap<>();
        if (maintenanceInformationDto == null) return parameter;
        parameter.put("conditionOfToolingValue", maintenanceInformationDto.getConditionOfTooling());
        parameter.put("maintenanceIntervalValue", maintenanceInformationDto.getMaintenanceInterval());
        parameter.put("upcomingMaintenanceToleranceValue", maintenanceInformationDto.getUpcomingMaintenanceTolerance());
        parameter.put("overdueMaintenanceToleranceValue", maintenanceInformationDto.getOverdueMaintenanceTolerance());
        parameter.put("cycleTimeToleranceL1Value", maintenanceInformationDto.getCycleTimeToleranceL1());
        parameter.put("cycleTimeToleranceL2Value", maintenanceInformationDto.getCycleTimeToleranceL2());
        parameter.put("statusMaintenanceValue", maintenanceInformationDto.getStatus());
        parameter.put("engineerInChargeValue", maintenanceInformationDto.getEngineerInCharge());
        parameter.put("maintenanceDocumentValue", maintenanceInformationDto.getMaintenanceDocument());
        parameter.put("instructionVideoValue", maintenanceInformationDto.getInstructionVideo());
        return parameter;
    }

    private Map<String, Object> pushDataOfCostInformation(CostInformationDto costInformationDto) {
        Map<String, Object> parameter = new HashMap<>();
        if (costInformationDto == null) return parameter;
        parameter.put("costOfToolingValue", costInformationDto.getCostOfTooling());
        parameter.put("accumulatedMaintenanceCostValue", costInformationDto.getAccumulatedMaintenanceCost());
        parameter.put("memoValue", costInformationDto.getMemo());
        return parameter;
    }

    private Map<String, Object> pushDataOfSupplierInformation(SupplierInformationDto supplierInformationDto) {
        Map<String, Object> parameter = new HashMap<>();
        if (supplierInformationDto == null) return parameter;
        parameter.put("targetUptimeValue", supplierInformationDto.getTargetUptime());
        parameter.put("supplierValue", supplierInformationDto.getSupplierName());
        parameter.put("uptimeToleranceL1Value", supplierInformationDto.getUptimeToleranceL1());
        parameter.put("uptimeToleranceL2Value", supplierInformationDto.getUptimeToleranceL2());
        parameter.put("labourValue", supplierInformationDto.getLabour());
        parameter.put("productionHoursPerDaysValue", supplierInformationDto.getProductionHoursPerDays());
        parameter.put("productionDayPerWeekValue", supplierInformationDto.getProductionDayPerWeek());
        parameter.put("maximumCapacityPerWeekValue", supplierInformationDto.getMaximumCapacityPerWeek());
        return parameter;
    }

    private Map<String, Object> pushParameterIcon() {
        Map<String, Object> parameter = new HashMap<>();
        try {
//            File logo = new File("resources/exports/icon/four-stripe.png");
            String url = "https://user-images.githubusercontent.com/49054779/79721360-6674d380-830c-11ea-9049-e49fab13f39c.png";
            parameter.put("iconLogo", "https://user-images.githubusercontent.com/49054779/79730958-cf177c80-831b-11ea-915c-d07ab7ae324a.png");
//            File label = ResourceUtils.getFile("classpath:exports/icons/four-stripe.png");
            parameter.put("iconLabel", url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameter;
    }

    private Map<String, Object> pushAllLabel(boolean isCustomServerName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.putAll(pushParameterLabelToBasicInformation());
        parameters.putAll(pushLabelOfDynamicInformation());
        if(!isCustomServerName) {
            parameters.putAll(pushLabelOfCost());
        }
        parameters.putAll(pushLabelOfPhysicalInformation());
        parameters.putAll(pushLabelOfMaintenanceInformation());
//        parameters.putAll(pushLabelOfCostInformation());
        parameters.putAll(pushLabelOfSupplierInformation());
        parameters.putAll(pushParameterIcon());
        parameters.putAll(pushLabelOfRunnerSystemInformation());
        return parameters;
    }

    private Map<String, Object> pushAllDataValue(BasicInformationDto basicInformationDto, List<saleson.restdocs.dto.exports.PartDto> partDtos, DynamicInformationDto dynamicInformationDto,
                                                 PhysicalInformationDto physicalInformationDto, MaintenanceInformationDto maintenanceInformationDto,
                                                 SupplierInformationDto supplierInformationDto, RunnerSystemInformationDto runnerSystemInformationDto, CostInformationDto costInformationDto,
                                                 boolean isCustomServerName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.putAll(pushDataOfBasicInformation(basicInformationDto));
        parameters.putAll(processPartLabelAndValue(partDtos));
        parameters.putAll(pushDataOfDynamicInformation(dynamicInformationDto));
        if(!isCustomServerName) {
            parameters.putAll(pushDataOfCostInformation(costInformationDto));
        }
        parameters.putAll(pushDataOfMaintenanceInformation(maintenanceInformationDto));
        parameters.putAll(pushDataOfSupplierInformation(supplierInformationDto));
        parameters.putAll(pushDataOfPhysicalInformation(physicalInformationDto));
        parameters.putAll(pushDataOfRunnerSystemInformation(runnerSystemInformationDto));
        return parameters;
    }


    public byte[] exportPDFDataMold(List<MoldDtoPDF> moldDtoPDFS) {
//		File file =new File("C:/Users/Admin/JaspersoftWorkspace/MMS/mss/tooling-pdf-template.jrxml");
        String fileName = "tooling-pdf-template.jrxm";
        InputStream file = null;
        try {
//            file = ResourceUtils.getFile("classpath:exports/pdf/tooling-pdf-template.jrxml");
            file = getFileTemplate(fileName);
//            if (file.exists()) {
            JasperReport jasperReport = JasperCompileManager.compileReport(file);
            Map parameter = new HashMap();
            parameter.put("labelTooling", "Tooling Report");
            JRBeanCollectionDataSource dataTable = new JRBeanCollectionDataSource(moldDtoPDFS);
//			    Insert list mold on
            parameter.put("dataListMold", dataTable);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameter, new JREmptyDataSource());
//			    JasperExportManager.exportReportToPdfFile(jasperPrint, "D:/file.pdf");
            return JasperExportManager.exportReportToPdf(jasperPrint);
//            } else {
//                log.error("File pdf tooling not exits !");
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JRException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] exportListMoldDetail(List<Mold> molds, String fileName){
        if(molds == null)  return new byte[0];
        List<JasperPrint> jasperPrints = new ArrayList<>();
        BasicInformationDto basicInformationDto = new BasicInformationDto();
        DynamicInformationDto dynamicInformationDto = new DynamicInformationDto();
        MaintenanceInformationDto maintenanceInformationDto = new MaintenanceInformationDto();
        List<PartDto> partDtos;
        PhysicalInformationDto physicalInformationDto = new PhysicalInformationDto();
        SupplierInformationDto supplierInformationDto = new SupplierInformationDto();
        RunnerSystemInformationDto runnerSystemInformationDto = new RunnerSystemInformationDto();
        CostInformationDto costInformationDto = new CostInformationDto();
        for (Mold mold : molds) {
            exportService.bindData(mold, basicInformationDto);
            exportService.bindData(mold, dynamicInformationDto,null);
            exportService.bindData(mold, maintenanceInformationDto);
            exportService.bindData(mold, physicalInformationDto);
            exportService.bindData(mold, supplierInformationDto);
            exportService.bindData(mold, runnerSystemInformationDto);
            exportService.bindData(mold, costInformationDto,null);
            partDtos = exportService.getPartDtos(mold);
            try {
                jasperPrints.add(createJasperPrintDataMoldDetail(basicInformationDto, partDtos, dynamicInformationDto,
                        physicalInformationDto, maintenanceInformationDto, supplierInformationDto,
                        runnerSystemInformationDto, costInformationDto, mold.getEquipmentCode(), fileName));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JRException e) {
                e.printStackTrace();
            }
        }
        return mergJasperPrints(markPageNum(jasperPrints)).toByteArray();
    }

}
