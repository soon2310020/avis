package com.stg.service.impl;

import com.stg.utils.Constants;
import com.stg.entity.InsuranceContract;
import com.stg.entity.InsurancePackage;
import com.stg.repository.*;
import com.stg.service.DashboardService;
import com.stg.service.dto.dashboard.DashboardCustomerListDto;
import com.stg.service.dto.dashboard.DashboardOverviewDto;
import com.stg.service.dto.dashboard.DashboardPackageDto;
import com.stg.service.dto.dashboard.ReleaseQuantityDto;
import com.stg.utils.DayOfWeek;
import com.stg.utils.InsuranceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.stg.service.dto.external.PackageNameEnum.UR_STYLE;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class DashboardImpl implements DashboardService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardImpl.class);

    private final IllustrationTableRepository illustrationTableRepository;
    private final InsuranceRequestRepository insuranceRequestRepository;
    private final InsurancePaymentRepository insurancePaymentRepository;
    private final InsuranceContractRepository insuranceContractRepository;
    private final InsurancePackageRepository insurancePackageRepository;

    @Override
    public DashboardOverviewDto dashboardOverview(Long userId) {
        LOGGER.info("Starting dashboard Overview with userId=" + userId);
        DashboardOverviewDto overviewDto = new DashboardOverviewDto();
        overviewDto.setIllustrationNumber(illustrationTableRepository.totalIllustrationCurrentWeek());
        long insuranceRequestCurrentWeek = insuranceRequestRepository.totalInsuranceRequestCurrentWeek();
        overviewDto.setInsuranceRequestNumber(insuranceRequestCurrentWeek);
        overviewDto.setPaymentNumber(insurancePaymentRepository.totalPaymentCurrentWeekByStatus("PAID"));
        overviewDto.setContractNumber(insuranceContractRepository.totalInsuranceContractCurrentWeek());

        long insuranceRequestToday = insuranceRequestRepository.totalInsuranceRequestToday();
        DecimalFormat f = new DecimalFormat("##.0");
        String percentIR = "0";
        String percentContract = "0";
        String percentPayment = "0";
        if (insuranceRequestCurrentWeek != 0 && insuranceRequestToday != 0) {
            double percentIRDouble = (double) insuranceRequestToday * 100 / insuranceRequestCurrentWeek;
            percentIR = formatPercent(f, percentIRDouble);
        }
        LOGGER.debug("% YCBH = " + percentIR);

        if (overviewDto.getPaymentNumber() != 0 && overviewDto.getContractNumber() != 0) {
            double percentContractDouble = (double) overviewDto.getContractNumber() * 100 / overviewDto.getPaymentNumber();
            percentContract = formatPercent(f, percentContractDouble);
        }
        LOGGER.debug("% HĐBH = " + percentContract);

        long totalPaymentCurrentWeek = insurancePaymentRepository.totalPaymentCurrentWeek();
        if (totalPaymentCurrentWeek != 0 && overviewDto.getPaymentNumber() != 0) {
            double percentPaymentDouble = (double) overviewDto.getPaymentNumber() * 100 / totalPaymentCurrentWeek;
            percentPayment = formatPercent(f, percentPaymentDouble);
        }
        LOGGER.debug("% giao dịch thành công = " + percentPayment);

        overviewDto.setInsuranceRequestPercent(percent100(percentIR));
        overviewDto.setContractPercent(percent100(percentContract));
        overviewDto.setPaymentPercent(percent100(percentPayment));
        return overviewDto;
    }

    private static String formatPercent(DecimalFormat f, double percentIRDouble) {
        String percentIR;
        if (percentIRDouble == (int) percentIRDouble) {
            percentIR = String.valueOf((int) percentIRDouble);
        } else {
            percentIR = f.format(percentIRDouble);
        }
        return percentIR;
    }

    public String percent100(String percent) {
        if ("100.00".equals(percent)) {
            percent = String.valueOf(100);
        }
        return percent;
    }

    @Override
    public ReleaseQuantityDto releaseQuantity(Long userId) {
        LOGGER.info("Starting dashboard releaseQuantity with userId=" + userId);

        ReleaseQuantityDto releaseQuantityDto = new ReleaseQuantityDto();
        ReleaseQuantityDto.Quantity illustration = new ReleaseQuantityDto.Quantity();
        releaseQuantityDto.setIllustration(illustration);
        illustration.setMonday(illustrationByDay(DayOfWeek.MONDAY.getDayNum()));
        illustration.setTuesday(illustrationByDay(DayOfWeek.TUESDAY.getDayNum()));
        illustration.setWednesday(illustrationByDay(DayOfWeek.WEDNESDAY.getDayNum()));
        illustration.setThursday(illustrationByDay(DayOfWeek.THURSDAY.getDayNum()));
        illustration.setFriday(illustrationByDay(DayOfWeek.FRIDAY.getDayNum()));
        illustration.setSaturday(illustrationByDay(DayOfWeek.SATURDAY.getDayNum()));
        illustration.setSunday(illustrationByDay(DayOfWeek.SUNDAY.getDayNum()));

        ReleaseQuantityDto.Quantity contract = new ReleaseQuantityDto.Quantity();
        releaseQuantityDto.setContract(contract);
        contract.setMonday(contractByDay(DayOfWeek.MONDAY.getDayNum()));
        contract.setTuesday(contractByDay(DayOfWeek.TUESDAY.getDayNum()));
        contract.setWednesday(contractByDay(DayOfWeek.WEDNESDAY.getDayNum()));
        contract.setThursday(contractByDay(DayOfWeek.THURSDAY.getDayNum()));
        contract.setFriday(contractByDay(DayOfWeek.FRIDAY.getDayNum()));
        contract.setSaturday(contractByDay(DayOfWeek.SATURDAY.getDayNum()));
        contract.setSunday(contractByDay(DayOfWeek.SUNDAY.getDayNum()));
        return releaseQuantityDto;
    }

    public long illustrationByDay (long dayNum){
        return illustrationTableRepository.totalIllustrationCurrentWeekByDayOfWeek(dayNum);
    }

    public long contractByDay (long dayNum){
        return insuranceContractRepository.totalContractCurrentWeekByDayOfWeek(dayNum);
    }

    @Override
    public List<DashboardPackageDto> insurancePackages(Long userId, String category) {
        LOGGER.info("Starting dashboard Packages with userId=" + userId);
        List<InsuranceContract> contractsFixCombo = insuranceContractRepository.listContractCurrentWeekByPackage(Constants.PackageType.FIX_COMBO.name(), category);
        List<InsuranceContract> contractsFreeStyle = insuranceContractRepository.listContractCurrentWeekByPackage(Constants.PackageType.FREE_STYLE.name(), category);
        contractsFixCombo.addAll(contractsFreeStyle);

        Map<InsurancePackage, Long> packageTypeMap = contractsFixCombo.stream().collect(Collectors.groupingBy(e -> e.getInsurancePackage(), Collectors.counting()));

        List<DashboardPackageDto> dashboardPackageDto = new ArrayList<>();
        Set<InsurancePackage> packageSet = packageTypeMap.keySet();
        for (InsurancePackage insurancePackage : packageSet) {
            DashboardPackageDto packageDto = new DashboardPackageDto();
            packageDto.setPackageName(insurancePackage.getPackageName());
            packageDto.setCount(packageTypeMap.get(insurancePackage));
            dashboardPackageDto.add(packageDto);
        }

        return dashboardPackageDto;
    }

    private List<InsuranceContract> filterFixComboByCategory(List<InsuranceContract> contractsFixCombo, String category) {
        return contractsFixCombo.stream().filter(ic ->
                ic.getInsurancePackage() != null && category.equals(ic.getInsurancePackage().getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DashboardCustomerListDto> customerHealthy(Long userId) {
        LOGGER.info("Starting dashboard customerHealthy with userId=" + userId);
        List<String> packageTypes = new ArrayList<>();
        packageTypes.add(Constants.PackageType.FIX_COMBO.name());
        packageTypes.add(Constants.PackageType.FLEXIBLE.name());

        List<InsuranceContract> insuranceContracts = insuranceContractRepository.listContractOrderByCreationTime(packageTypes);
        List<DashboardCustomerListDto> customerListDtos = new ArrayList<>();
        for(InsuranceContract contract : insuranceContracts) {
            DashboardCustomerListDto customerListDto = new DashboardCustomerListDto();
            customerListDto.setId(contract.getCustomer().getMbId());
            customerListDto.setFullName(contract.getCustomer().fullNameOrDefaultT24());
            String packageName = getPackageName(contract.getInsurancePackage());
            LOGGER.debug("package name = " + packageName);
            customerListDto.setPackageName(packageName);

            if (contract.getInsurancePackage() != null) {
                customerListDto.setCategory(contract.getInsurancePackage().getCategory());
            }
            customerListDto.setMbalFeePaymentTime(contract.getMbalFeePaymentTime());

            customerListDto.setTotalFee(contract.getStrInsuranceFee());

            customerListDtos.add(customerListDto);
        }

        return customerListDtos;
    }

    private String getPackageName(InsurancePackage insurancePackage) {
        String packageName = UR_STYLE.getVal();
        if (insurancePackage != null) {
            packageName = insurancePackage.getPackageName();
        }
        return packageName;
    }
}
