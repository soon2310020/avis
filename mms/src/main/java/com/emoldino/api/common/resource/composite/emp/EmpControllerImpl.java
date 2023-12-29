package com.emoldino.api.common.resource.composite.emp;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.exception.SysException;
import com.emoldino.framework.util.Property;

@RestController
public class EmpControllerImpl implements EmpController {

	@Override
	public Map<String, Object> get() {
		Map<String, Object> output = new LinkedHashMap<>();

		// Test Sources Here

//		output = (Map<String, Object>) ValueUtils.fromJsonStr(ValueUtils.toJsonStr(data), Map.class);

		return output;
	}

	@Override
	public Map<String, Object> getBizException() {
		throw new BizException("BIZ_ERROR_TEST", "This is log message of BizException Test.", new Property("name", "BizException"));
	}

	@Override
	public Map<String, Object> getLogicException() {
		throw new LogicException("LOGIC_ERROR_TEST", "This is log message of LogicException Test.", new Property("name", "LogicException"));
	}

	@Override
	public Map<String, Object> getSysException() {
		throw new SysException("SYS_ERROR_TEST", "This is log message of SysException Test.", new Property("name", "SysException"));
	}

}
