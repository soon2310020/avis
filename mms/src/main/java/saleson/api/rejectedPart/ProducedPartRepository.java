package saleson.api.rejectedPart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.RejectedRateStatus;
import saleson.model.rejectedPartRate.ProducedPart;

import java.util.List;
import java.util.Optional;

public interface ProducedPartRepository extends JpaRepository<ProducedPart, Long>, QuerydslPredicateExecutor<ProducedPart>, ProducedPartRepositoryCustom {
    List<ProducedPart> findByMoldIdAndPartIdAndHourAndFrequent(Long moldId, Long partId, String hour, Frequent frequent);
    List<ProducedPart> findByMoldIdAndPartIdAndDayAndFrequent(Long moldId, Long partId, String day, Frequent frequent);
    List<ProducedPart> findByMoldIdAndPartIdAndWeekAndFrequent(Long moldId, Long partId, String week, Frequent frequent);
    List<ProducedPart> findByMoldIdAndPartIdAndMonthAndFrequent(Long moldId, Long partId, String month, Frequent frequent);

    List<ProducedPart> findByMoldIdInAndFrequentAndDayBetween(List<Long> ids, Frequent frequent, String startDate, String endDate);

    Long countByMoldIdAndPartIdAndRejectedRateStatusAndFrequent(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);

    Optional<ProducedPart> findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByHourDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);
    Optional<ProducedPart> findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByDayDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);
    Optional<ProducedPart> findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByWeekDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);
    Optional<ProducedPart> findFirstByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByMonthDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);
    List<ProducedPart> findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByHourDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);

    List<ProducedPart> findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByDayDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);
    List<ProducedPart> findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByWeekDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);
    List<ProducedPart> findTop2ByMoldIdAndPartIdAndRejectedRateStatusAndFrequentOrderByMonthDesc(Long moldId, Long partId, RejectedRateStatus status, Frequent frequent);

    ProducedPart findFirstByMoldIdAndFrequentAndHour(Long id, Frequent frequent, String hour);
    List<ProducedPart> findByMoldIdInAndFrequentAndHour(List<Long> ids, Frequent frequent, String hour);
    List<ProducedPart> findByMoldIdInAndFrequentAndDay(List<Long> ids, Frequent frequent, String day);
    List<ProducedPart> findByMoldIdInAndFrequentAndWeek(List<Long> ids, Frequent frequent, String week);
    List<ProducedPart> findByMoldIdInAndFrequentAndMonth(List<Long> ids, Frequent frequent, String month);

    List<ProducedPart> findByDayAndFrequent(String day, Frequent frequent);
    List<ProducedPart> findByWeekAndFrequent(String week, Frequent frequent);
    List<ProducedPart> findByMonthAndFrequent(String month, Frequent frequent);
}
