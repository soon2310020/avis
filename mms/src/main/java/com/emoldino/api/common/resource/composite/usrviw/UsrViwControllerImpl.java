package com.emoldino.api.common.resource.composite.usrviw;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.usrviw.dto.UsrViwUserItem;
import com.emoldino.framework.util.BeanUtils;

import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.model.User;

@Deprecated
@RestController
public class UsrViwControllerImpl implements UsrViwController {

	@Override
	public Page<UsrViwUserItem> getUsers(Pageable pageable) {
		// 1. Get ActiveUsers
		// Company is auto filtered
		Page<User> page;
		{
			UserParam param = new UserParam();
			param.setEnabled(true);
			if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
				pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("name"));
			}
			page = BeanUtils.get(UserService.class).findAll(param, pageable);
		}

		// 2. Convert DTO
		return new PageImpl<>(page.getContent().stream()
				.map(item -> item.getCompany() == null ? new UsrViwUserItem(item.getId(), item.getName(), item.getCompanyId())
						: new UsrViwUserItem(item.getId(), item.getName(), item.getCompanyId(), item.getCompany().getCompanyType(), item.getCompany().getCompanyCode(),
								item.getCompany().getName()))
				.collect(Collectors.toList()), page.getPageable(), page.getTotalElements());
	}

}
