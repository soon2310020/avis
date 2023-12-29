//package unused.resource.ppachart.dto;
//
//import com.querydsl.core.annotations.QueryProjection;
//
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//public class ChartDto {
//	private Long id;
//	private String indicator;
//	private String ppaStatus;
//	private Long count;
//	private Long goodCount;
//	private Long badCount;
//
//	@QueryProjection
//	public ChartDto(Long id, String indicator, String ppaStatus, Long count) {
//		this.id = id;
//		this.indicator = indicator;
//		this.ppaStatus = ppaStatus;
//		this.count = count;
//	}
//
//	@QueryProjection
//	public ChartDto(Long id, String indicator, Long goodCount, Long badCount) {
//		this.id = id;
//		this.indicator = indicator;
//		this.goodCount = goodCount;
//		this.badCount = badCount;
//	}
//
//	@QueryProjection
//	public ChartDto(String ppaStatus, Long count) {
//		this.ppaStatus = ppaStatus;
//		this.count = count;
//	}
//}
