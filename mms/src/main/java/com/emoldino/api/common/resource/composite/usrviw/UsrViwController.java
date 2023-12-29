package com.emoldino.api.common.resource.composite.usrviw;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.usrviw.dto.UsrViwUserItem;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Deprecated
@Api(protocols = "http, https", tags = "@Deprecated Common / User Selector: use Common / Filters Instead")
@RequestMapping("api/common/usr-viw")
public interface UsrViwController {

	@ApiOperation("Get Users")
	@GetMapping("/users")
	Page<UsrViwUserItem> getUsers(Pageable pageable);

}
