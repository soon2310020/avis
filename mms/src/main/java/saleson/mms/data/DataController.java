package saleson.mms.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.data.DataRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data.QData;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DataController {

	private final DataRepository dataRepository;
	private final DataCounterRepository dataCounterRepository;
	private final DataAccelerationRepository dataAccelerationRepository;

	@GetMapping("/mms/data/rawdata")
	public String rawdata(DataCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
		BooleanBuilder predicate = new BooleanBuilder();

		if (criteria.getQuery() != null && !criteria.getQuery().isEmpty()) {
			predicate.and(QData.data.terminalId.contains(criteria.getQuery()));
		}

		Page<Data> pageContent = dataRepository.findAll(predicate, pageable);

		model.addAttribute("criteria", criteria);
		model.addAttribute("pageContent", pageContent);
		model.addAttribute("pagination", new DataPagination(pageContent));

		return "data/rawdata";
	}

	@GetMapping("/mms/data/cdata")
	public String cdata(DataCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
		BooleanBuilder predicate = new BooleanBuilder();

		if (criteria.getQuery() != null && !criteria.getQuery().isEmpty()) {
			predicate.and(QDataCounter.dataCounter.counterId.contains(criteria.getQuery()));
		}

		Page<DataCounter> pageContent = dataCounterRepository.findAll(predicate, pageable);

		model.addAttribute("criteria", criteria);
		model.addAttribute("pageContent", pageContent);
		model.addAttribute("pagination", new DataPagination(pageContent));

		return "data/cdata";
	}

	@GetMapping("/mms/data/adata")
	public String adata(DataCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
		BooleanBuilder predicate = new BooleanBuilder();

		if (criteria.getQuery() != null && !criteria.getQuery().isEmpty()) {
			predicate.and(QDataAcceleration.dataAcceleration.counterId.contains(criteria.getQuery()));
		}

		Page<DataAcceleration> pageContent = dataAccelerationRepository.findAll(predicate, pageable);

		model.addAttribute("criteria", criteria);
		model.addAttribute("pageContent", pageContent);
		model.addAttribute("pagination", new DataPagination(pageContent));

		return "data/adata";
	}

}
