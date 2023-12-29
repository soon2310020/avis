package com.emoldino.api.common.resource.composite.tabstp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpData;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpGetIn;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpItem;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpPostData;
import com.emoldino.api.common.resource.composite.tabstp.repository.TabStpRepository;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;

import saleson.common.enumeration.ObjectType;

@Service
@Transactional
public class TabStpService {

	public ListOut<TabStpItem> get(ObjectType objectType, TabStpGetIn input) {
		return BeanUtils.get(TabStpRepository.class).findAll(objectType, input);
	}

	public TabStpData get(ObjectType objectType, Long id) {
		return BeanUtils.get(TabStpRepository.class).findById(objectType, id);
	}

	public TabStpData post(ObjectType objectType, TabStpPostData data) {
		return BeanUtils.get(TabStpRepository.class).save(objectType, data);
	}

	public TabStpData put(ObjectType objectType, Long id, TabStpPostData data) {
		return BeanUtils.get(TabStpRepository.class).save(objectType, id, data);
	}

	public void hidden(ObjectType objectType, Long id) {
		BeanUtils.get(TabStpRepository.class).hidden(objectType, id);
	}

	public void shown(ObjectType objectType, Long id) {
		BeanUtils.get(TabStpRepository.class).shown(objectType, id);
	}

	public void hiddenByName(ObjectType objectType, String name) {
		BeanUtils.get(TabStpRepository.class).hiddenByName(objectType, name);
	}

	public void shownByName(ObjectType objectType, String name) {
		BeanUtils.get(TabStpRepository.class).shownByName(objectType, name);
	}

	public void delete(ObjectType objectType, Long id) {
		BeanUtils.get(TabStpRepository.class).delete(objectType, id);
	}

}
