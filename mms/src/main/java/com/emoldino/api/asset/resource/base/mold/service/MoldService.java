package com.emoldino.api.asset.resource.base.mold.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummary;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingUtilizationSummary;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingUtilizationSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.base.object.repository.customfield.CustomFieldRepository;
import com.emoldino.api.common.resource.base.object.repository.customfieldvalue.CustomFieldValueRepository;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldStandardValueRepository;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.ObjectType;
import saleson.model.MoldStandardValue;
import saleson.model.customField.CustomField;
import saleson.model.customField.CustomFieldValue;

@Service("moldService2")
@Transactional
public class MoldService {

	@Autowired
	private com.emoldino.api.asset.resource.base.mold.repository.MoldRepository repo;

	public ToolingStatusSummary getStatusSummary(ToolingStatusSummaryGetIn input) {
		return repo.findAllStatusSummary(input);
	}

	public ToolingUtilizationSummary getUtilizationSummary(ToolingUtilizationSummaryGetIn input) {
		return repo.findAllUtilizationSummary(input);
	}

	public Optional<MoldStandardValue> getStandardValue(Long moldId, String month, int periodMonths, int minCdataCount, int maxCdataCount) {
		return BeanUtils.get(MoldStandardValueRepository.class).findOneByMoldIdAndMonthAndPeriodMonthsAndMinCdataCountAndMaxCdataCount(moldId, month, periodMonths, minCdataCount,
				maxCdataCount);
	}

	public void saveStandardValue(MoldStandardValue data) {
		BeanUtils.get(MoldStandardValueRepository.class).save(data);
	}

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void adjustBatch() {
		JobUtils.runIfNotRunning("mold.adjustBatch", new JobOptions().setClustered(true), () -> {

			for (CustomField field : BeanUtils.get(CustomFieldRepository.class)//
					.findAll(Qs.customField.objectType.eq(ObjectType.TOOLING).and(Qs.customField.fieldName.in("Supplier Tooling ID", "Supplier tool number")))) {
				DataUtils.runBatch(MoldRepository.class, //
						new BooleanBuilder(), //
						Sort.by("id"), 100, true, mold -> {
							if (!ObjectUtils.isEmpty(mold.getSupplierMoldCode())) {
								return;
							}

							TranUtils.doNewTran(() -> {
								for (CustomFieldValue value : BeanUtils.get(CustomFieldValueRepository.class)//
										.findAll(Qs.customFieldValue.objectId.eq(mold.getId()).and(Qs.customFieldValue.customField.eq(field)))) {
									if (ObjectUtils.isEmpty(value.getValue())) {
										continue;
									}
									mold.setSupplierMoldCode(value.getValue());
									BeanUtils.get(MoldRepository.class).save(mold);
									break;
								}
							});
						});
				break;
			}

			DataUtils.runBatch(MoldRepository.class, //
					Q.mold.equipmentStatus.eq(EquipmentStatus.DISCARDED), //
					Sort.by("id"), 100, false, mold -> {
						TranUtils.doNewTran(() -> {
							mold.setToolingStatus(ToolingStatus.DISPOSED);
							mold.setEquipmentStatus(EquipmentStatus.DISPOSED);
							if (!mold.isEnabled()) {
								mold.setEnabled(true);
							}
							BeanUtils.get(MoldRepository.class).save(mold);
						});
					});

			DataUtils.runBatch(MoldRepository.class, //
					Q.mold.equipmentStatus.eq(EquipmentStatus.DISPOSED).and(Q.mold.toolingStatus.eq(ToolingStatus.UNKNOWN)), //
					Sort.by("id"), 100, false, mold -> {
						TranUtils.doNewTran(() -> {
							mold.setToolingStatus(ToolingStatus.DISPOSED);
							if (!mold.isEnabled()) {
								mold.setEnabled(true);
							}
							BeanUtils.get(MoldRepository.class).save(mold);
						});
					});

			DataUtils.runBatch(MoldRepository.class, //
					Q.mold.enabled.isFalse().and(Q.mold.toolingStatus.eq(ToolingStatus.DISPOSED)), //
					Sort.by("id"), 100, false, mold -> {
						TranUtils.doNewTran(() -> {
							mold.setEnabled(true);
							BeanUtils.get(MoldRepository.class).save(mold);
						});
					});

			DataUtils.runBatch(MoldRepository.class, //
					Q.mold.lastShot.gt(0).and(Q.mold.designedShot.gt(0))//
							.and(Q.mold.utilizationRate.isNull()//
									.or(Q.mold.lastShot.floatValue().divide(Q.mold.designedShot.floatValue()).multiply(1000d).round().divide(10d)//
											.ne(Q.mold.utilizationRate.floatValue()))), //
					Sort.by("id"), 100, false, mold -> {
						TranUtils.doNewTran(() -> {
							mold.setLastShot(mold.getLastShot());
							BeanUtils.get(MoldRepository.class).save(mold);
						});
					});

//			DataUtils.runBatch(MoldRepository.class, //
//					Q.mold.toolingStatus.isNull(), //
//					Sort.by("id"), 100, false, mold -> {
//						TranUtils.doNewTran(() -> {
//							mold.setToolingStatus(MoldUtils.toToolingStatus(mold));
//							BeanUtils.get(MoldRepository.class).save(mold);
//						});
//					});

//			DataUtils.runBatch(MoldRepository.class, null, Sort.by("id"), 100, true, mold -> {
//				TranUtils.doNewTran(() -> {
//					boolean changed = false;
//					if (ValueUtils.equals(mold.isEnabled(), mold.isDeleted())) {
//						changed = true;
//						mold.setEnabled(!mold.isDeleted());
//					}
//
//					if (!ObjectUtils.isEmpty(mold.getCompanyId())//
//							&& !ObjectUtils.isEmpty(mold.getSupplierCompanyId())//
//							&& !ValueUtils.equals(mold.getCompanyId(), mold.getSupplierCompanyId())) {
//						changed = true;
//						mold.setCompanyId(mold.getCompanyId());
//					}
//			
//					if (changed) {
//						BeanUtils.get(MoldRepository.class).save(mold);
//					}
//				});
//			});

		});
	}

}
