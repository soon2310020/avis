//package unused.resource.ppachart;
//
//import java.util.List;
//
//import org.springframework.web.bind.annotation.RestController;
//
//import com.emoldino.framework.util.BeanUtils;
//
//import unused.resource.ppachart.dto.ChartDataOut;
//import unused.resource.ppachart.dto.FilterDto;
//import unused.resource.ppachart.dto.PpaChartFilterIn;
//import unused.resource.ppachart.dto.PpaChartIn;
//import unused.resource.ppachart.service.PpaChartService;
//import unused.resource.ppachart.service.PpaChartSimpleFilterService;
//
//@RestController
//public class PtdChartControllerImpl implements PpaChartController {
//
//	@Override
//	public ChartDataOut get(PpaChartIn chartIn) {
//		return BeanUtils.get(PpaChartService.class).get(chartIn);
//	}
//
//	// --
//	// Filter TOBE : PpaChartSimpleFilterService -> PpaChartService
//	@Override
//	public List<FilterDto> getPartList(PpaChartFilterIn filterIn) {
//		return BeanUtils.get(PpaChartSimpleFilterService.class).getPartList(filterIn);
//	}
//
//	@Override
//	public List<FilterDto> getCompanyList(PpaChartFilterIn filterIn) {
//		return BeanUtils.get(PpaChartSimpleFilterService.class).getCompanyList(filterIn);
//	}
//
//	@Override
//	public List<FilterDto> getMoldList(PpaChartFilterIn filterIn) {
//		return BeanUtils.get(PpaChartSimpleFilterService.class).getMoldList(filterIn);
//	}
//
//	@Override
//	public List<FilterDto> getCategoryList(PpaChartFilterIn filterIn) {
//		return BeanUtils.get(PpaChartSimpleFilterService.class).getCategoryList(filterIn);
//	}
//
//	@Override
//	public void test() {
//		BeanUtils.get(PpaChartService.class).testRandomData();
//	}
//}
