package saleson.api.statistics.payload;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDaily {
	private String year;
	private String month;
	private String day;
	private String week;
	private Long moldId;
	private String ci;
	private long uptimeSeconds;
	private double ct;
	private double ctVal;
	private long shotCount;
	private long shotCountVal;
}
