package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.NumberUtils;
import vn.com.twendie.avis.api.mapping.DayOfWeekMapping;
import vn.com.twendie.avis.api.model.response.CustomerJourneyDiaryDailyDTO;
import vn.com.twendie.avis.api.model.response.CustomerPaymentRequestWrapperDTO;
import vn.com.twendie.avis.api.model.response.JourneyDiaryDailyDTO;
import vn.com.twendie.avis.api.model.response.PaymentRequestItemDTO;
import vn.com.twendie.avis.api.service.ExportPdfService;
import vn.com.twendie.avis.api.service.HtmlFactory;
import vn.com.twendie.avis.api.service.JourneyDiarySignatureService;
import vn.com.twendie.avis.api.util.BuildExportJourneyDiaryUtils;
import vn.com.twendie.avis.api.util.StringUtils;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class ExportPaymentRequestImpl extends ExportPdfService {

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    NumberUtils numberUtils;

    @Autowired
    private DayOfWeekMapping dayOfWeekMapping;

    @Autowired
    private JourneyDiarySignatureService journeyDiarySignatureService;

    @Override
    public void buildXhtml(String xhtml) {
        super.setXhtml(xhtml);
    }

    public void buildHtml(CustomerPaymentRequestWrapperDTO customerPaymentRequestWrapperDTO) throws IOException {
        InputStream file = new ClassPathResource("templates/pdf/payment-request-customer.html").getInputStream();
        String templateFile = IOUtils.toString(file, "UTF-8");
        HtmlFactory.HtmlBuilder htmlBuilder = new HtmlFactory.HtmlBuilder().load(templateFile);
        if (customerPaymentRequestWrapperDTO != null) {
            htmlBuilder.setVariable("customerName", customerPaymentRequestWrapperDTO.getCustomerName());
            htmlBuilder.setVariable("adminName", customerPaymentRequestWrapperDTO.getAdminName());
            String nameUses = StringUtils.convertListToString(customerPaymentRequestWrapperDTO.getNameFinds());
            if(nameUses.length() == 0){
                nameUses = "Tất cả người sử dụng";
            }

            String dateTimeFrom = dateUtils.format(customerPaymentRequestWrapperDTO.getFromDate(), DateUtils.SHORT_PATTERN);
            htmlBuilder.setVariable("userName", nameUses);
            htmlBuilder.setVariable("customerAddress", customerPaymentRequestWrapperDTO.getCustomerAddress());
            htmlBuilder.setVariable("from", dateTimeFrom);
            htmlBuilder.setVariable("to", dateUtils.format(customerPaymentRequestWrapperDTO.getToDate(), DateUtils.SHORT_PATTERN));
            htmlBuilder.setVariable("driverName", customerPaymentRequestWrapperDTO.getDriverName());
            htmlBuilder.setVariable("vehicleNumberPlate", customerPaymentRequestWrapperDTO.getVehicleNumberPlate());
            String [] dateTimeArr = dateTimeFrom.split("/");
            if(dateTimeArr.length == 3){
                htmlBuilder.setVariable("month", dateUtils.convertMonthToString(Integer.parseInt(dateTimeArr[1])));
                htmlBuilder.setVariable("year", dateTimeArr[2]);
            }
            htmlBuilder.setVariable("dataValue", buildContentData(customerPaymentRequestWrapperDTO));
            htmlBuilder.setVariable("dataFooter", buildFooter(customerPaymentRequestWrapperDTO.getPaymentRequestItems()));

            calcTotal(customerPaymentRequestWrapperDTO.getCustomerJourneyDiaryDailies(), htmlBuilder);
        }


        buildXhtml(htmlBuilder.build().getHtml());
    }

    private void calcTotal(List<CustomerJourneyDiaryDailyDTO> dtos, HtmlFactory.HtmlBuilder htmlBuilder){
        int totalOvertime = 0;
        int totalTollsFee = 0;
        int totalParkingFee = 0;
        int totalOvernight = 0;
        int totalOverKmSelfDrive = 0;
        int totalKmUse = 0;
        int totalOverKm = 0;

//        if(dtos.size() > 0){
//            CustomerJourneyDiaryDailyDTO j = dtos.get(dtos.size() -1);
        for(CustomerJourneyDiaryDailyDTO j : dtos){
            if(j.getDate() == null){
                totalOverKm += j.getOverKm() != null ? j.getOverKm().intValue() : 0;
                totalOvertime += j.getOverTime() != null ? j.getOverTime() : 0;
                totalOvernight += j.getOvernight() != null ? j.getOvernight() : 0;
                totalParkingFee += j.getParkingFee() != null ? j.getParkingFee().intValue() : 0;
                totalTollsFee += j.getTollsFee() != null ? j.getTollsFee().intValue() : 0;
            }
            totalKmUse += j.getUsedKm() != null ? j.getUsedKm().intValue() : 0;
            totalOverKmSelfDrive += j.getOverKmSelfDrive() != null ? j.getOverKmSelfDrive().intValue() : 0;
        }
        htmlBuilder.setVariable("totalKmUse", String.valueOf(totalKmUse));
        htmlBuilder.setVariable("totalKmSelf", String.valueOf(totalOverKmSelfDrive));
        htmlBuilder.setVariable("totalOverTime", dateUtils.convertMinuteToHour(totalOvertime));
        htmlBuilder.setVariable("totalOverKm", String.valueOf(totalOverKm));
        htmlBuilder.setVariable("totalOverKmSelf", String.valueOf(totalOverKmSelfDrive));
        htmlBuilder.setVariable("totalOvernight", String.valueOf(totalOvernight));
        htmlBuilder.setVariable("totalParkingFee", numberUtils.format(totalParkingFee));
        htmlBuilder.setVariable("totalTollsFee", numberUtils.format(totalTollsFee));
    }

    private String buildFooter(List<PaymentRequestItemDTO> paymentRequestItems) {
        if (paymentRequestItems == null || paymentRequestItems.size() == 0) return "";

        StringBuilder tableFooter = new StringBuilder();
        for (PaymentRequestItemDTO item : paymentRequestItems) {
            tableFooter.append("<tr>")
                    .append("<td class='data-text-left'>")
                    .append(item.getName() != null ? item.getName() : "")
                    .append("</td>")

                    .append("<td class='data-text-right'>")
                    .append(item.getPrice() != null ? numberUtils.format(item.getPrice()) : "")
                    .append("</td>");

            String countStr = "-";
            if (item.getCount() != null && item.getCount().doubleValue() > 0) {
                countStr = numberUtils.removeBeforeDotIfGt0(item.getCount());
            }

            tableFooter.append("<td class='data'>")
                    .append(countStr)
                    .append("</td>")
                    .append("<td class='data'>")
                    .append(item.getUnit() != null ? item.getUnit() : "")
                    .append("</td>")

                    .append("<td class='data-text-right'>")
                    .append(item.getTotalPrice() != null ? numberUtils.format(item.getTotalPrice()) : "")
                    .append("</td>")

                    .append("</tr>");
        }
        return tableFooter.toString();
    }

    private String buildContentData(CustomerPaymentRequestWrapperDTO customerPaymentRequestWrapperDTO) {
        StringBuffer table = new StringBuffer();
        if (customerPaymentRequestWrapperDTO.getCustomerJourneyDiaryDailies() == null || customerPaymentRequestWrapperDTO.getCustomerJourneyDiaryDailies().size() == 0) {
            return table.toString();
        }
        int index = 1;
        for (CustomerJourneyDiaryDailyDTO j : customerPaymentRequestWrapperDTO.getCustomerJourneyDiaryDailies()) {
            if(j.getDate() == null) continue;
            String customerName = BuildExportJourneyDiaryUtils.convert(Arrays.asList(j.getCustomerNameUsed()));
            String departmentName = BuildExportJourneyDiaryUtils.convert(Arrays.asList(j.getCustomerDepartment()));
            JourneyDiarySignature journeyDiarySignature = journeyDiarySignatureService.findByJourneyDiaryId(j.getId());
            if (journeyDiarySignature != null && journeyDiarySignature.getMemberCustomer() != null) {
                customerName = journeyDiarySignature.getMemberCustomer().getName();
                departmentName = journeyDiarySignature.getMemberCustomer().getDepartment();
            }

            table.append("<tr>")
                    .append("<td class='data'>")
                    .append(index++)
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getDate() != null ? dayOfWeekMapping.map(j.getDate()) : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getDate() != null ? dateUtils.format(j.getDate(), DateUtils.SHORT_PATTERN) : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(customerName)
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(departmentName)
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(BuildExportJourneyDiaryUtils.convert(Arrays.asList(j.getTripItinerary())))
                    .append("</td>")


                    .append("<td class='data'>")
                    .append(j.getUsedKm() != null ? j.getUsedKm().longValue() : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getUsedKmSelfDrive() != null ? j.getUsedKmSelfDrive().longValue() : "")
                    .append("</td>")


                    .append("<td class='data'>")
                    .append(dateUtils.convertTime(j.getWorkingTimeGpsFrom(), j.getWorkingTimeGpsTo()))
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getOverTime() != null ? dateUtils.convertMinuteToHour(j.getOverTime()) : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getOverKm() != null ? j.getOverKm().longValue() : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getOverKmSelfDrive() != null ? j.getOverKmSelfDrive().longValue() : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getOvernight() != null ? j.getOvernight() : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getIsHoliday() != null && j.getIsHoliday() ? "x" : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getIsWeekend() != null && j.getIsWeekend() ? "x" : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append(j.getBoolWithDriver() != null && j.getBoolWithDriver() ? "x" : "")
                    .append("</td>")

                    .append("<td class='data'>")
                    .append((j.getIsSelfDrive() != null && j.getIsSelfDrive()) ? "x" : "")
                    .append("</td>")


//                    .append("<td class='data'>")
//                    .append(j.getDriverName() != null ? BuildExportJourneyDiaryUtils.convert(Arrays.asList(j.getDriverName())) : "")
//                    .append("</td>")
//
//                    .append("<td class='data'>")
//                    .append(j.getVehicleNumberPlate() != null ? BuildExportJourneyDiaryUtils.convert(Arrays.asList(j.getVehicleNumberPlate())) : "")
//                    .append("</td>")

                    .append("<td class='data-price'>")
                    .append(j.getParkingFee() != null ? numberUtils.format(j.getParkingFee().longValue()) : "")
                    .append("</td>")

                    .append("<td class='data-price'>")
                    .append(j.getTollsFee() != null ? numberUtils.format(j.getTollsFee().longValue()) : "")
                    .append("</td>")
                    .append("</tr>");

        }
        return table.toString();
    }
}
