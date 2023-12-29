package com.emoldino.framework.repository;

import com.emoldino.api.analysis.resource.base.data.repository.data.QData;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.QTransferIndex;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.QTransferResult;
import com.emoldino.api.common.resource.base.file.repository.filegroup.QFileGroup;
import com.emoldino.api.common.resource.base.file.repository.fileitem.QFileItem;
import com.emoldino.api.common.resource.base.log.repository.accesssummarylog.QAccessSummaryLog;
import com.emoldino.api.common.resource.base.log.repository.errorlog.QErrorLog;
import com.emoldino.api.common.resource.base.log.repository.errorsummarylog.QErrorSummaryLog;
import com.emoldino.framework.repository.cachedata.QCacheData;

import saleson.model.QLogTransfer;
import saleson.model.QTabTable;
import saleson.model.QTabTableData;
import saleson.model.customField.QCustomField;
import saleson.model.customField.QCustomFieldValue;

public class Qs {

	public static final QData3Collected data3Collected = QData3Collected.data3Collected;
	public static final QData data = QData.data;
	public static final QDataCounter dataCounter = QDataCounter.dataCounter;
	public static final QDataAcceleration dataAcceleration = QDataAcceleration.dataAcceleration;

	public static final QLogTransfer logTransfer = QLogTransfer.logTransfer;
	public static final QTransferResult transferResult = QTransferResult.transferResult;
	public static final QTransferIndex transferIndex = QTransferIndex.transferIndex;

	public static final QFileGroup fileGroup = QFileGroup.fileGroup;
	public static final QFileItem fileItem = QFileItem.fileItem;

	public static final QCustomField customField = QCustomField.customField;
	public static final QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;

	public static final QTabTable tabTable = QTabTable.tabTable;
	public static final QTabTableData tabTableData = QTabTableData.tabTableData;

	public static final QCacheData cacheData = QCacheData.cacheData;

	public static final QAccessSummaryLog accessSummaryLog = QAccessSummaryLog.accessSummaryLog;
	public static final QErrorLog errorLog = QErrorLog.errorLog;
	public static final QErrorSummaryLog errorSummaryLog = QErrorSummaryLog.errorSummaryLog;

}
