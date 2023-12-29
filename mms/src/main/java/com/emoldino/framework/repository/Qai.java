package com.emoldino.framework.repository;

import com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature.QAiMoldFeature;
import com.emoldino.api.integration.resource.base.ai.repository.aipqresult.QAiPqResult;
import com.emoldino.api.integration.resource.base.ai.repository.aitsdresult.QAiTsdResult;

public class Qai {
	public static final QAiPqResult pqResult = QAiPqResult.aiPqResult;
	public static final QAiTsdResult tsdResult = QAiTsdResult.aiTsdResult;
	public static final QAiMoldFeature moldFeature = QAiMoldFeature.aiMoldFeature;
}
