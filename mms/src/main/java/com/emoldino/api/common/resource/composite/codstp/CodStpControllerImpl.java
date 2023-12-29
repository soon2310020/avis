package com.emoldino.api.common.resource.composite.codstp;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.code.service.data.CodeDataService;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpDataItem;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpGroupItem;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpGroupItemIn;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class CodStpControllerImpl implements CodStpController {

	@Autowired
	private CodeDataService service;

	@Override
	public Page<CodStpDataItem> get(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CodStpDataItem get(Long id) {
		return service.get(id);
	}

	@Override
	public SuccessOut post(CodStpDataItem data) {
		service.post(data);
		return null;
	}

	@Override
	public SuccessOut put(Long id, CodStpDataItem data) {
		data.setId(id);
		service.put(id, data);
		return null;
	}

	@Override
	public SuccessOut disableList(List<Long> id) {
		service.disableList(id);
		return null;
	}

	@Override
	public SuccessOut enableList(List<Long> id) {
		service.enableList(id);
		return null;
	}

	@Override
	public List<CodStpGroupItem> getGroupItemList(String codeType, CodStpGroupItemIn input) {				
		return service.getGroupItemList(codeType, input);		
	}

}