package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.ListUtils;
import vn.com.twendie.avis.api.core.util.NumberUtils;
import vn.com.twendie.avis.api.core.util.StrUtils;
import vn.com.twendie.avis.api.mapping.DayOfWeekMapping;
import vn.com.twendie.avis.api.model.response.ExportDiariesAdditionDataWrapper;
import vn.com.twendie.avis.api.model.response.JourneyDiaryDailyDTO;
import vn.com.twendie.avis.api.model.response.PaymentRequestItemDTO;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.api.util.BuildExportJourneyDiaryUtils;
import vn.com.twendie.avis.api.util.StringUtils;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum;
import vn.com.twendie.avis.data.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ExportJourneyDiaryDailyServiceImpl extends ExportPdfService {

    @Autowired
    private DayOfWeekMapping dayOfWeekMapping;
    @Autowired
    private ListUtils listUtils;
    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private StrUtils strUtils;

    @Autowired
    private NumberUtils numberUtils;

    @Autowired
    private JourneyDiarySignatureService journeyDiarySignatureService;

    @Autowired
    private JourneyDiaryDailyService journeyDiaryDailyService;

    @Override
    public void buildXhtml(String xhtml) {
        super.setXhtml(xhtml);
    }

    public void buildHtmlJourneyDiaryDailies(ExportDiariesAdditionDataWrapper dataWrapper, List<JourneyDiaryDaily> journeyDiaryDailies, Long totalKm) throws IOException {
        InputStream file = new ClassPathResource("templates/pdf/nkht-customer.html").getInputStream();
        String templateFile = IOUtils.toString(file, "UTF-8");
        HtmlFactory.HtmlBuilder htmlBuilder = new HtmlFactory.HtmlBuilder().load(templateFile);
        if (dataWrapper != null) {

            String nameUses = StringUtils.convertListToString(dataWrapper.getNameFinds());
            if(nameUses.length() == 0){
                nameUses = "Tất cả người sử dụng";
            }

            htmlBuilder.setVariable("userName", nameUses);
            htmlBuilder.setVariable("fromDate", dateUtils.format(dataWrapper.getDiariesFrom(), DateUtils.SHORT_PATTERN));
            htmlBuilder.setVariable("driverName", dataWrapper.getDriverName());
            htmlBuilder.setVariable("toDate", dateUtils.format(dataWrapper.getDiariesTo(), DateUtils.SHORT_PATTERN));
            htmlBuilder.setVariable("licensePlate", dataWrapper.getVehicleNumberPlate());
            htmlBuilder.setVariable("branch", dataWrapper.getBranchName());
            htmlBuilder.setVariable("timeWorkNormal", dataWrapper.getWorkingTime());
            htmlBuilder.setVariable("zoneDrive", dataWrapper.getVehicleWorkingArea());
            htmlBuilder.setVariable("timeWorkHoliday", dataWrapper.getWorkingTimeHoliday());
            htmlBuilder.setVariable("contractCode", dataWrapper.getContractCode());
            htmlBuilder.setVariable("numberOfDayInMonth", String.valueOf(dataWrapper.getDateStatistic() != null ? dataWrapper.getDateStatistic().getDatesOfMonth() : ""));
            htmlBuilder.setVariable("numberWorkByContract", String.valueOf(dataWrapper.getDateStatistic() != null ? dataWrapper.getDateStatistic().getContractWorkingDays() : ""));
            htmlBuilder.setVariable("customerName", dataWrapper.getCustomerName());
            htmlBuilder.setVariable("adminName", dataWrapper.getAdminName());
            htmlBuilder.setVariable("numberWorkReality", String.valueOf(dataWrapper.getDateStatistic() != null ? dataWrapper.getDateStatistic().getRealWorkingDays() : ""));
            htmlBuilder.setVariable("status", dataWrapper.getContractStatus());
            htmlBuilder.setVariable("dateFirst", dateUtils.format(dataWrapper.getContractFromDate(), DateUtils.SHORT_PATTERN));
            htmlBuilder.setVariable("timeStartContract", dateUtils.format(dataWrapper.getContractFromDate(), DateUtils.SHORT_PATTERN));
            htmlBuilder.setVariable("timeEndContact", dateUtils.format(dataWrapper.getContractToDate(), DateUtils.SHORT_PATTERN));
            htmlBuilder.setVariable("contractAbout", dataWrapper.getContractPeriod());
        }
        if (journeyDiaryDailies != null) {
            htmlBuilder.setVariable("dataValue", dataValue(journeyDiaryDailies, totalKm, true));
        }
        buildXhtml(htmlBuilder.build().getHtml());
    }

    @Deprecated
    public void buildHtmlPaymentRequest(List<JourneyDiaryDaily> journeyDiaryDailies, List<PaymentRequestItemDTO> dtos, Contract contract, Long totalKm) throws IOException {
        InputStream file = new ClassPathResource("templates/pdf/payment-request.html").getInputStream();
        String templateFile = IOUtils.toString(file, "UTF-8");
        HtmlFactory.HtmlBuilder htmlBuilder = new HtmlFactory.HtmlBuilder().load(templateFile);
        if (contract != null) {
            htmlBuilder.setVariable("customerName", contract.getCustomer() != null ? contract.getCustomer().getName() : "");
            htmlBuilder.setVariable("adminName", contract.getMemberCustomer() != null ? contract.getMemberCustomer().getName() : "");
            htmlBuilder.setVariable("customerAddress", contract.getCustomer() != null ? contract.getCustomer().getAddress() : "");
            htmlBuilder.setVariable("from", contract.getFromDatetime() != null ? dateUtils.format(contract.getFromDatetime(), DateUtils.SHORT_PATTERN) : "");
            htmlBuilder.setVariable("to", contract.getToDatetime() != null ? dateUtils.format(contract.getToDatetime(), DateUtils.SHORT_PATTERN) : "");
            htmlBuilder.setVariable("driverName", contract.getDriver() != null ? contract.getDriver().getName() : "");
            htmlBuilder.setVariable("vehicleNumberPlate", contract.getVehicle() != null && contract.getVehicle().getNumberPlate() != null ? contract.getVehicle().getNumberPlate() : "");
            htmlBuilder.setVariable("vehicleModel", contract.getVehicle() != null && contract.getVehicle().getModel() != null ? contract.getVehicle().getModel() : "");
            long totalPrice = dtos.size() > 0 ? dtos.get(0).getTotalPrice() : 0;
            final String totalPriceInWords = org.apache.commons.lang3.StringUtils.capitalize(strUtils.convertNumberToWords(totalPrice) + " (VNĐ)");

            htmlBuilder.setVariable("totalPriceInWorks", totalPriceInWords);
        }
        if (journeyDiaryDailies != null) {
            htmlBuilder.setVariable("dataValue", dataValue(journeyDiaryDailies, totalKm, false));
        }
        htmlBuilder.setVariable("dataFooter", buildFooterPaymentRequest(dtos));
        buildXhtml(htmlBuilder.build().getHtml());
    }


    @Deprecated
    private String buildFooterPaymentRequest(List<PaymentRequestItemDTO> dtos) {
        StringBuffer footer = new StringBuffer();
        String style = " class='title-data'";
        if (dtos != null && dtos.size() > 0) {
            dtos.forEach(e -> {
                footer.append("<tr>")
                        .append("<td");
                if (e.getId() == 19) {
                    footer.append(style);
                }
                footer.append(">")
                        .append(e.getName() != null ? e.getName() : "")
                        .append("</td>")
                        .append("<td");
                if (e.getId() == 19) {
                    footer.append(style);
                }
                footer.append(">")
                        .append(e.getPrice() != null ? e.getPrice() : "")
                        .append("</td>")
                        .append("<td");
                if (e.getId() == 19) {
                    footer.append(style);
                }
                footer.append(">")
                        .append(e.getCount() != null ? e.getCount() : "")
                        .append("</td>")
                        .append("<td");
                if (e.getId() == 19) {
                    footer.append(style);
                }
                footer.append(">")
                        .append(e.getUnit() != null ? e.getUnit() : "")
                        .append("</td>")
                        .append("<td");
                if (e.getId() == 19) {
                    footer.append(style);
                }
                footer.append(">")
                        .append(e.getTotalPrice() != null ? e.getTotalPrice() : "")
                        .append("</td>")
                        .append("</tr>");
            });
        }
        return footer.toString();
    }

    private String dataValue(List<JourneyDiaryDaily> journeyDiaryDailies, Long totalKm, boolean isJourneyDiary) {

        List<JourneyDiaryDailyDTO> dtos = listUtils.mapAll(journeyDiaryDailies, JourneyDiaryDailyDTO.class);

        dtos.forEach(x -> {
            x.setDayOfWeek(dayOfWeekMapping.map(x.getDate()));

            JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findByJourneyDiaryId(x.getJourneyDiaryId());
            if(journeyDiarySignature != null && journeyDiarySignature.getMemberCustomer() != null){
                MemberCustomer memberCustomer = journeyDiarySignature.getMemberCustomer();
                x.setCustomerNameUsed(memberCustomer.getName());
                x.setCustomerDepartment(memberCustomer.getDepartment());
            }
        });

        journeyDiaryDailyService.fetchDataSignature(dtos);

        StringBuilder data = new StringBuilder();

        data.append(buildContent(dtos, isJourneyDiary));

        data.append(buildFooter(totalKm, dtos, isJourneyDiary));
        return data.toString();
    }

    private String buildContent(List<JourneyDiaryDailyDTO> dtos, boolean isJourneyDiary) {
        StringBuffer data = new StringBuffer();
        int index = 1;
        for (JourneyDiaryDailyDTO j : dtos) {


            data.append("<tr>");
            data.append("<td class='data'>");
            data.append(index++);
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getDayOfWeek() != null ? j.getDayOfWeek() : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getDate() != null ? dateUtils.format(j.getDate(), DateUtils.SHORT_PATTERN) : "");
            data.append("</td>");

            data.append("<td class='data'>");
            String customerNameUsed = BuildExportJourneyDiaryUtils.convert(j.getCustomerNameUsed());
            customerNameUsed += BuildExportJourneyDiaryUtils.convert(getDataCustomerNameUsedNotDuplicate(j.getChildren()));
            data.append(customerNameUsed);
            data.append("</td>");

            data.append("<td class='data'>");
            String customerDepartment = BuildExportJourneyDiaryUtils.convert(j.getCustomerDepartment());
            customerDepartment += BuildExportJourneyDiaryUtils.convert(getDataCustomerNameUsedNotDuplicate(j.getChildren()));
            data.append(customerDepartment);
            data.append("</td>");

            data.append("<td class='data'>");
            String tripItinerary = BuildExportJourneyDiaryUtils.convert(j.getTripItinerary());
            tripItinerary += BuildExportJourneyDiaryUtils.convert(getDataTripItinerary(j.getChildren()));
            data.append(tripItinerary);
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getUsedKm() != null ? j.getUsedKm() : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getUsedKmSelfDrive() != null ? j.getUsedKmSelfDrive() : "");
            data.append("</td>");

            String gps = dateUtils.convertTime(j.getWorkingTimeGpsFrom(), j.getWorkingTimeGpsTo());

            data.append("<td class='data'>");
            data.append(gps);
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(dateUtils.convertMinuteToHour(j.getOverTime()));
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getOverKm() != null ? j.getOverKm() : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getOverKmSelfDrive() != null ? j.getOverKmSelfDrive() : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getOvernight() != null ? j.getOvernight() : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getIsHoliday() && j.getIsHoliday() != null ? "x" : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getIsWeekend() != null && j.getIsWeekend() ? "x" : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getIsSelfDrive() != null && j.getIsSelfDrive() ? "" : "x");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(j.getIsSelfDrive() != null && j.getIsSelfDrive() ? "x" : "");
            data.append("</td>");

            data.append("<td class='data'>");
            data.append(BuildExportJourneyDiaryUtils.convert(j.getDriverName()));
            data.append("</td>");

            data.append("<td class='data'>");
            String vehicleNumberPlates = BuildExportJourneyDiaryUtils.convert(j.getVehicleNumberPlate());
            data.append(vehicleNumberPlates);
            data.append("</td>");

            data.append("<td class='data'>");
            Integer costCT008 = BuildExportJourneyDiaryUtils.getCostByType(JourneyDiaryCostTypeEnum.PARKING_FEE.code(), j.getJourneyDiaryDailyCostTypes());
            data.append(costCT008 != null ? numberUtils.format(costCT008) : "");
            data.append("</td>");

            data.append("<td class='data'>");
            Integer costToolFee = BuildExportJourneyDiaryUtils.getCostByType(JourneyDiaryCostTypeEnum.TOLLS_FEE.code(), j.getJourneyDiaryDailyCostTypes());
            data.append(costToolFee != null ? numberUtils.format(costToolFee) : "");
            data.append("</td>");

            if (isJourneyDiary) {
                data.append("<td class='data'>");
                Integer costCT011 = BuildExportJourneyDiaryUtils.getCostByType(JourneyDiaryCostTypeEnum.NIGHT_STORAGE_FEE.code(), j.getJourneyDiaryDailyCostTypes());
                data.append(costCT011 != null ? numberUtils.format(costCT011) : "");
                data.append("</td>");
            }

            data.append("<td class='data'>")
                    .append(j.getSignatureStatus() != null && j.getSignatureStatus() ? "Đã ký" : "Chưa ký")
                    .append("</td>")
                    .append("<td class='data'>")
                    .append(j.getSignatureComment() != null ? BuildExportJourneyDiaryUtils.convert(j.getSignatureComment()) : "")
                    .append("</td>");

            data.append("<td class='data'>");
            String notes = BuildExportJourneyDiaryUtils.convert(j.getNote());
            data.append(notes);
            data.append("</td>");
            data.append("</tr>");
        }
        return data.toString();
    }

    private String buildFooter(Long totalHKm, List<JourneyDiaryDailyDTO> dtos, boolean isJourneyDiary) {
        int totalOvertime = 0;
        int totalCT007 = 0;
        int totalCT008 = 0;
        int totalCT0011 = 0;
        int totalOvernight = 0;
        int totalOverKmSelfDrive = 0;

        for (JourneyDiaryDailyDTO j : dtos) {
            totalOvertime += j.getOverTime() != null ? j.getOverTime() : 0;
            totalOverKmSelfDrive += j.getOverKmSelfDrive() != null ? j.getOverKmSelfDrive().intValue() : 0;
            totalOvernight += j.getOvernight() != null ? j.getOvernight() : 0;
            Integer costCT008 = BuildExportJourneyDiaryUtils.getCostByType(JourneyDiaryCostTypeEnum.PARKING_FEE.code(), j.getJourneyDiaryDailyCostTypes());
            totalCT008 += costCT008 != null ? costCT008 : 0;
            Integer costToolFee = BuildExportJourneyDiaryUtils.getCostByType(JourneyDiaryCostTypeEnum.TOLLS_FEE.code(), j.getJourneyDiaryDailyCostTypes());
            totalCT007 += costToolFee != null ? costToolFee : 0;
            Integer costCT011 = BuildExportJourneyDiaryUtils.getCostByType(JourneyDiaryCostTypeEnum.NIGHT_STORAGE_FEE.code(), j.getJourneyDiaryDailyCostTypes());
            totalCT0011 += costCT011 != null ? costCT011 : 0;
        }

        StringBuilder footerDataTable = new StringBuilder();
        footerDataTable.append("<tr>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'><b>Total</b></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'><b>")
                .append(dateUtils.convertMinuteToHour(totalOvertime))
                .append("</b></td>")
                .append("<td class='title-data'><b>")
                .append(totalHKm)
                .append("</b></td>")
                .append("<td class='title-data'><b>").append(totalOverKmSelfDrive).append("</b></td>")
                .append("<td class='title-data'><b>").append(totalOvernight).append("</b></td>")

                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")

                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>");
        footerDataTable.append("<td class='title-data'><b>")
                .append(numberUtils.format(totalCT008))
                .append("</b></td>");
        footerDataTable.append("<td class='title-data'><b>")
                .append(numberUtils.format(totalCT007))
                .append("</b></td>");

        footerDataTable.append("<td class='title-data'><b>")
                .append(numberUtils.format(totalCT0011))
                .append("</b></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
                .append("<td class='title-data'></td>")
        .append("</tr>");

        return footerDataTable.toString();
    }

    private Set<String> getDataCustomerNameUsedNotDuplicate(List<JourneyDiaryDailyDTO> journeyDiaryDailyDTOS) {
        Set<String> data = new HashSet<>();
        journeyDiaryDailyDTOS.forEach(e -> {
            data.addAll(e.getCustomerNameUsed());
        });
        return data;
    }

    private List<String> getDataTripItinerary(List<JourneyDiaryDailyDTO> journeyDiaryDailyDTOS) {
        List<String> tripItineraryList = new ArrayList<>();
        journeyDiaryDailyDTOS.forEach(j -> {
            tripItineraryList.addAll(j.getTripItinerary());
        });
        return tripItineraryList;
    }
}
