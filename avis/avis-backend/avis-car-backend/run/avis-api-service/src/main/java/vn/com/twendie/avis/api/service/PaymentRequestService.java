package vn.com.twendie.avis.api.service;

import org.javatuples.Triplet;
import vn.com.twendie.avis.api.model.projection.PRLogContractCostProjection;
import vn.com.twendie.avis.api.model.response.PaymentRequestItemDTO;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;
import vn.com.twendie.avis.data.model.LogContractNormList;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface PaymentRequestService {

    PaymentRequestItemDTO getRentalPrice(Timestamp to, Contract contract, double diff);

    List<PaymentRequestItemDTO> getDriverFee(Contract contract, Timestamp from, Timestamp to, List<JourneyDiaryDaily> diaries, Map<String, List<PRLogContractCostProjection>> map, Timestamp firstDayOD, Double firstDayODPriceDiff);

    List<PaymentRequestItemDTO> getOvernightFee(List<JourneyDiaryDaily> diaries, Timestamp from, Timestamp to, Contract contract, Map<String, List<PRLogContractCostProjection>> map);

    PaymentRequestItemDTO getTollFee(List<JourneyDiaryDaily> diaries);

    PaymentRequestItemDTO getOtherFee();

    PaymentRequestItemDTO getOverKMContractWithOutDriver(Timestamp from, Timestamp to, Contract contract, List<JourneyDiaryDaily> dailyJourneyDiaries);

    double calculateOverKM(LogContractNormList overKMQuota, Contract contract, Timestamp from, Timestamp to);

    Map<String, List<PRLogContractCostProjection>> costTypeMap(Long contractId, Timestamp from, Timestamp to);

    Triplet<List<JourneyDiaryDaily>, Timestamp, Double> checkAndConvertDiariesIfOverDay(Contract contract, Timestamp toDate, List<JourneyDiaryDaily> dailyJourneyDiaries, double diff);

    double calculateDaysDiff(Timestamp from, Timestamp to);
}
