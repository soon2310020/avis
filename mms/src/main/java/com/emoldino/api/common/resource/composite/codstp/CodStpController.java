package com.emoldino.api.common.resource.composite.codstp;

import java.awt.print.Pageable;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.composite.codstp.dto.CodStpDataItem;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpGroupItem;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpGroupItemIn;
import com.emoldino.framework.dto.SuccessOut;


@RequestMapping("api/common/cod-stp")
public interface CodStpController {

	
    @GetMapping
    Page<CodStpDataItem> get(Pageable pageable);
	
	@GetMapping("/{id}")
	CodStpDataItem get(@PathVariable("id") Long id);
	
	@GetMapping("/drop-down/{codeType}")
	List<CodStpGroupItem> getGroupItemList(@PathVariable("codeType") String codeType, CodStpGroupItemIn input);

	@PostMapping("/one")
	SuccessOut post(@RequestBody CodStpDataItem data);

	@PutMapping("/{id}")
	SuccessOut put(@PathVariable("id") Long id, @RequestBody CodStpDataItem data);

	@PutMapping("/disable")
	SuccessOut disableList(@RequestParam List<Long> id);

	@PutMapping("/enable")
	SuccessOut enableList(@RequestParam List<Long> id);

}