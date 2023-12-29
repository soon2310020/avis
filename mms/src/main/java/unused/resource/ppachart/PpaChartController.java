//package unused.resource.ppachart;
//
//import java.util.List;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import unused.resource.ppachart.dto.ChartDataOut;
//import unused.resource.ppachart.dto.FilterDto;
//import unused.resource.ppachart.dto.PpaChartFilterIn;
//import unused.resource.ppachart.dto.PpaChartIn;
//
//@Api
//@RequestMapping("/api/analysis/ppa/chart")
//public interface PpaChartController {
//
//	@ApiOperation(value = "chartData get")
//	@GetMapping
//	ChartDataOut get(PpaChartIn chartIn);
//
//	@ApiOperation(value = "chart filter part")
//	@GetMapping("/filter/part")
//	List<FilterDto> getPartList(PpaChartFilterIn chartIn);
//
//	@ApiOperation(value = "chart filter company")
//	@GetMapping("/filter/supplier")
//	List<FilterDto> getCompanyList(PpaChartFilterIn chartIn);
//
//	@ApiOperation(value = "chart filter mold")
//	@GetMapping("/filter/mold")
//	List<FilterDto> getMoldList(PpaChartFilterIn chartIn);
//
//	@ApiOperation(value = "chart filter category")
//	@GetMapping("/filter/product")
//	List<FilterDto> getCategoryList(PpaChartFilterIn chartIn);
//
//	@ApiOperation(value = "chart filter category")
//	@GetMapping("/testData")
//	void test();
//}