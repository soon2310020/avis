package com.emoldino.api.analysis.resource.base.data.repository.dataacceleration;

import java.util.List;

public interface DataAccelerationRepositoryCustom {
	List<DataAcceleration> findAllByMoldIdAndMeasurementTime(Long moldId, String fromDateStr, String toDateStr);
}
