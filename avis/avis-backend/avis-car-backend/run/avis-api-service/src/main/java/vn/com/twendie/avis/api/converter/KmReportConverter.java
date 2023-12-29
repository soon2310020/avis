package vn.com.twendie.avis.api.converter;

import org.javatuples.Pair;
import org.modelmapper.AbstractConverter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.model.export.KmReport;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.model.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.DRIVER_ID;
import static vn.com.twendie.avis.data.enumtype.MappingFieldCodeFrontendEnum.VEHICLE_ID;

@Component
public class KmReportConverter extends AbstractConverter<Pair<Contract, Timestamp>, KmReport> {

    private final ContractService contractService;

    public KmReportConverter(@Lazy ContractService contractService) {
        this.contractService = contractService;
    }

    @Override
    protected KmReport convert(Pair<Contract, Timestamp> contractTimestampPair) {

        Contract contract = contractTimestampPair.getValue0();
        Timestamp timestamp = contractTimestampPair.getValue1();

        Long driverId = contractService.getContractValueAtTime(contract, DRIVER_ID.getName(), timestamp, Long.class);
        Long vehicleId = contractService.getContractValueAtTime(contract, VEHICLE_ID.getName(), timestamp, Long.class);

        BigDecimal totalKm = contract.getJourneyDiaryDailies()
                .stream()
                .map(JourneyDiaryDaily::getTotalKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);

        BigDecimal emptyKm = contract.getJourneyDiaryDailies()
                .stream()
                .map(JourneyDiaryDaily::getEmptyKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);

        BigDecimal usedKm = contract.getJourneyDiaryDailies()
                .stream()
                .map(JourneyDiaryDaily::getUsedKm)
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);

        return KmReport.builder()
                .customerName(contract.getCustomer().getName())
                .vehicleId(vehicleId)
                .driverId(driverId)
                .totalKm(totalKm)
                .emptyKm(emptyKm)
                .usedKm(usedKm)
                .build();
    }

}
