package vn.com.twendie.avis.api.adapter;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.response.JourneyDiaryDailyDTO;
import vn.com.twendie.avis.api.model.response.PRWithoutDriverDiary;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.Vehicle;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static vn.com.twendie.avis.api.core.util.DateUtils.SHORT_PATTERN;

@AllArgsConstructor
public class PRWithoutDriverDiaryAdapter implements Function<List<JourneyDiaryDaily>, List<PRWithoutDriverDiary>> {

    private final DateUtils dateUtils;
    private final JourneyDiaryDailyDTO totalRow;

    @Override
    public List<PRWithoutDriverDiary> apply(List<JourneyDiaryDaily> journeyDiaryDailies) {
        List<PRWithoutDriverDiary> result = new ArrayList<>();
        PRWithoutDriverDiary temp = null;
        String startDate = null;

        for (int i = journeyDiaryDailies.size() - 1; i >= 0; i--) {
            if (Objects.nonNull(journeyDiaryDailies.get(i).getKmStart())) {
                if (Objects.nonNull(journeyDiaryDailies.get(i).getKmEnd())) {
                    String date = dateUtils.format(journeyDiaryDailies.get(i).getDate(), SHORT_PATTERN);
                    result.add(PRWithoutDriverDiary.builder()
                            .kmStart(journeyDiaryDailies.get(i).getKmStart())
                            .kmEnd(journeyDiaryDailies.get(i).getKmEnd())
                            .usedKm(journeyDiaryDailies.get(i).getUsedKm())
                            .date(String.format("%s - %s", date, date))
                            .startDate(journeyDiaryDailies.get(i).getDate())
                            .driverName(getDriverName(journeyDiaryDailies.get(i)))
                            .vehicleNumberPlate(getVehicleNumberPlate(journeyDiaryDailies.get(i)))
                            .note(getNote(journeyDiaryDailies.get(i)))
                            .build());
                } else {
                    startDate = dateUtils.format(journeyDiaryDailies.get(i).getDate(), SHORT_PATTERN);
                    temp = PRWithoutDriverDiary.builder()
                            .kmStart(journeyDiaryDailies.get(i).getKmStart())
                            .usedKm(journeyDiaryDailies.get(i).getUsedKm())
                            .startDate(journeyDiaryDailies.get(i).getDate())
                            .driverName(getDriverName(journeyDiaryDailies.get(i)))
                            .vehicleNumberPlate(getVehicleNumberPlate(journeyDiaryDailies.get(i)))
                            .note(getNote(journeyDiaryDailies.get(i)))
                            .build();
                }
            } else if (Objects.nonNull(temp)) {

                if (Objects.nonNull(journeyDiaryDailies.get(i).getNote())
                        && !temp.getNote().contains(journeyDiaryDailies.get(i).getNote())) {
                    temp.getNote().addAll(getNote(journeyDiaryDailies.get(i)));
                }

                if (Objects.nonNull(journeyDiaryDailies.get(i).getKmEnd())) {
                    temp.setKmEnd(journeyDiaryDailies.get(i).getKmEnd());
                    temp.setDate(String.format("%s - %s", startDate, dateUtils.format(journeyDiaryDailies.get(i).getDate(), SHORT_PATTERN)));
                    result.add(temp);
                }
            }
        }

        result.sort(Comparator.comparing(PRWithoutDriverDiary::getStartDate));
        result.add(transformTotalRow(totalRow));
        return result;
    }

    private List<String> getNote(JourneyDiaryDaily journeyDiaryDaily) {
        if (Objects.nonNull(journeyDiaryDaily.getNote())) {
            return new ArrayList<String>(){{
                add(journeyDiaryDaily.getNote());
            }};
        } else if (CollectionUtils.isNotEmpty(journeyDiaryDaily.getChildren())) {
            return journeyDiaryDaily.getChildren().stream()
                    .map(JourneyDiaryDaily::getNote)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private List<String> getVehicleNumberPlate(JourneyDiaryDaily journeyDiaryDaily) {
        if (Objects.nonNull(journeyDiaryDaily.getVehicle())) {
            return Collections.singletonList(journeyDiaryDaily.getVehicle().getNumberPlate());
        } else if (CollectionUtils.isNotEmpty(journeyDiaryDaily.getChildren())) {
            return journeyDiaryDaily.getChildren().stream()
                    .map(JourneyDiaryDaily::getVehicle)
                    .filter(Objects::nonNull)
                    .map(Vehicle::getNumberPlate)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> getDriverName(JourneyDiaryDaily journeyDiaryDaily) {
        if (Objects.nonNull(journeyDiaryDaily.getDriver())) {
            return Collections.singletonList(journeyDiaryDaily.getDriver().getName());
        } else if (CollectionUtils.isNotEmpty(journeyDiaryDaily.getChildren())) {
            return journeyDiaryDaily.getChildren().stream()
                    .map(JourneyDiaryDaily::getDriver)
                    .filter(Objects::nonNull)
                    .map(User::getName)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private PRWithoutDriverDiary transformTotalRow(JourneyDiaryDailyDTO totalRow) {
        return PRWithoutDriverDiary.builder()
                .usedKm(totalRow.getUsedKm())
                .overKm(totalRow.getOverKm())
                .build();
    }
}
