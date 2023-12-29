package saleson.restdocs;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import saleson.api.chart.payload.ChartPayload;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartService;
import saleson.api.part.payload.PartPayload;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.DateViewType;
import saleson.model.Mold;
import saleson.model.Part;
import saleson.model.data.ChartData;
import saleson.restdocs.dto.MoldDto;
import saleson.restdocs.dto.PageDto;
import saleson.restdocs.dto.PartChartDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/apis")
public class ApisController {

	@Lazy
	@Autowired
	MoldService moldService;

	@Autowired
	MoldRepository moldRepository;

	@Autowired
	PartService partService;

	@Autowired
	ModelMapper modelMapper;

	@GetMapping("/molds")
	public ResponseEntity getMolds(@RequestBody MoldPayload payload,
								   Pageable pageable) {

//		moldService.loadTreeCompanyForPayLoad(payload);
		Page<MoldDto> pageContent = moldService.findAll(payload.getPredicate(), pageable)
				.map(mold -> modelMapper.map(mold, MoldDto.class));



		return ResponseEntity.ok(modelMapper.map(pageContent, PageDto.class));
	}


	@GetMapping("/molds/{id}")
	public ResponseEntity getMolds(@PathVariable("id") Long id) {

		Mold mold = moldService.findById(id);

		if (mold == null) {
			return ResponseEntity.badRequest().body("ID error");
		}

		return ResponseEntity.ok(modelMapper.map(mold, MoldDto.class));
	}

	@GetMapping("/molds/report")
	public List<ChartData> getMoldChartData(@RequestBody ChartPayload payload) {
		if (payload.getMoldId() == null) {
			return new ArrayList<>();
		}

		if (payload.getYear() == null) {
			payload.setYear("" + LocalDate.now().getYear());
		}

		if (payload.getDateViewType() == null) {
			payload.setDateViewType(DateViewType.WEEK);
		}

		if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.CYCLE_TIME)) {
			List<ChartData> dataList = moldService.findCycleTimeData(payload);

			dataList.stream().filter(d -> d.getMoldCode() != null).forEach(d -> System.out.println(d));
			return dataList.stream().limit(5L).collect(Collectors.toList());

		} else {			// QUANTITY, UPTIME
			return moldRepository.findChartData(payload).stream()
					.limit(5L).collect(Collectors.toList());

		}
	}


	@GetMapping("/parts")
	public ResponseEntity getParts(@RequestBody PartPayload payload, Pageable pageable, Model model) {

		Page<Part> pageContent = partService.findAllWithStatisticsPart(payload, pageable);

		return ResponseEntity.ok(modelMapper.map(pageContent, PageDto.class));
	}


	@GetMapping("/parts/{id}")
	public ResponseEntity getPart(@PathVariable("id") Long id) {

		Part part = partService.findById(id);

		if (part == null) {
			return ResponseEntity.badRequest().body("ID error");
		}

		return ResponseEntity.ok(part);
	}


	@GetMapping("/parts/report")
	public List<PartChartDto> getChartData(@RequestBody ChartPayload payload) {

		if (payload.getYear() == null) {
			payload.setYear("" + LocalDate.now().getYear());
		}

		if (payload.getDateViewType() == null) {
			payload.setDateViewType(DateViewType.WEEK);
		}
		return moldRepository.findChartData(payload).stream().limit(5L)
				.map(c -> modelMapper.map(c, PartChartDto.class))
				.collect(Collectors.toList());
	}





}
