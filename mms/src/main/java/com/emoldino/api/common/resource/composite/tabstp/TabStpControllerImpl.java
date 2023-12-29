package com.emoldino.api.common.resource.composite.tabstp;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpData;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpGetIn;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpItem;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpPostData;
import com.emoldino.api.common.resource.composite.tabstp.service.TabStpService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

import saleson.common.enumeration.ObjectType;

@RestController
public class TabStpControllerImpl implements TabStpController {

	@Override
	public ListOut<TabStpItem> get(ObjectType objectType, TabStpGetIn input) {
		return BeanUtils.get(TabStpService.class).get(objectType, input);
	}

	@Override
	public TabStpData get(ObjectType objectType, Long id) {
		return BeanUtils.get(TabStpService.class).get(objectType, id);
	}

	@Override
	public TabStpData post(ObjectType objectType, TabStpPostData data) {
		return BeanUtils.get(TabStpService.class).post(objectType, data);
	}

	@Override
	public TabStpData put(ObjectType objectType, Long id, TabStpPostData data) {
		return BeanUtils.get(TabStpService.class).put(objectType, id, data);
	}

	@Override
	public SuccessOut hidden(ObjectType objectType, Long id) {
		BeanUtils.get(TabStpService.class).hidden(objectType, id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut shown(ObjectType objectType, Long id) {
		BeanUtils.get(TabStpService.class).shown(objectType, id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut hiddenByName(ObjectType objectType, String name) {
		BeanUtils.get(TabStpService.class).hiddenByName(objectType, name);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut shownByName(ObjectType objectType, String name) {
		BeanUtils.get(TabStpService.class).shownByName(objectType, name);
		return SuccessOut.getDefault();
	}

	@Override
	public void delete(ObjectType objectType, Long id) {
		BeanUtils.get(TabStpService.class).delete(objectType, id);
	}

}
