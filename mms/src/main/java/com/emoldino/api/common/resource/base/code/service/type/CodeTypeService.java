package com.emoldino.api.common.resource.base.code.service.type;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.code.dto.CodeField;
import com.emoldino.api.common.resource.base.code.dto.CodeType;
import com.emoldino.framework.dto.ListOut;

@Service
public class CodeTypeService {

	private static final Map<String, CodeType> TABLE;
	static {
		TABLE = new TreeMap<>();

		// MACHINE_DOWNTIME_REASON
		CodeType mainCategory = new CodeType("MACHINE_DOWNTIME_REASON_GROUP1", "machine_downtime_reason.group1");
		CodeType subCategory = new CodeType("MACHINE_DOWNTIME_REASON_GROUP2", "machine_downtime_reason.group2");
		CodeType codeType = new CodeType("MACHINE_DOWNTIME_REASON", "machine_downtime_reason", new CodeField(mainCategory), new CodeField(subCategory));
		TABLE.put(mainCategory.getCodeType(), mainCategory);
		TABLE.put(subCategory.getCodeType(), subCategory);
		TABLE.put(codeType.getCodeType(), codeType);

	}

	public ListOut<CodeType> getList() {
		ListOut<CodeType> output = new ListOut<>();
		output.setContent(new ArrayList<>(TABLE.values()));
		return output;
	}

	public CodeType get(String codeType) {
		return TABLE.get(codeType);
	}

}