package com.emoldino.api.common.resource.base.id.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.emoldino.api.common.resource.base.id.service.idrule.IdRuleDataRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.id.dto.IdGenIn;
import com.emoldino.api.common.resource.base.id.enumeration.IdPartType;
import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;
import com.emoldino.api.common.resource.base.id.repository.idruleseq.IdRuleSeq;
import com.emoldino.api.common.resource.base.id.repository.idruleseq.IdRuleSeqRepository;
import com.emoldino.api.common.resource.base.id.service.idrule.IdRuleWorkOrder;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
public class IdService {
	@Data
	@AllArgsConstructor
	public static class IdRule {
		private List<IdPart> parts;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class IdPart {
		private IdPartType type;
		private String expression;
		private Integer minLength;
		private Integer maxLength;

		public IdPart(IdPartType type, String expression) {
			this(type, expression, null, null);
		}

		public IdPart(IdPartType type, Integer length) {
			this(type, null, length, length);
		}
	}

	private static final Map<IdRuleCode, IdRule> RULES;
	static {
		RULES = new LinkedHashMap<>();
		RULES.put(IdRuleCode.WORK_ORDER, new IdRuleWorkOrder());
		RULES.put(IdRuleCode.DATA_REQUEST, new IdRuleDataRequest());
	}

	private static final String DELI_OPEN = "{";
	private static final String DELI_CLOSE = "}";

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String gen(IdGenIn input) {
		IdRuleCode code = input.getIdRuleCode();
		LogicUtils.assertNotNull(code, "idRuleCode");

		// Get ID Rule Parts
		List<IdPart> parts;
		{
			IdRule rule = RULES.get(code);
			if (rule == null) {
				return null;
			}

			parts = rule.getParts();
			if (parts.isEmpty()) {
				return "";
			}
		}

		// Create Pattern
		String pattern;
		List<IdPart> seqParts = null;
		{
			StringBuilder buf = new StringBuilder();
			int i = 0;
			int seqIndex = 0;
			for (IdPart part : parts) {
				LogicUtils.assertNotNull(part.getType(), "idRule.part[" + i++ + "].type");

				IdPartType type = part.getType();

				if (IdPartType.CONSTANT.equals(type)) {
					String value = part.getExpression();
					buf.append(value);
				} else if (IdPartType.SEQUENCE.equals(type)) {
					if (seqParts == null) {
						seqParts = new ArrayList<>();
					}
					seqParts.add(part);
					buf.append(DELI_OPEN).append(seqIndex++).append(DELI_CLOSE);
				}
			}
			pattern = buf.toString();
		}

		String id = pattern;

		// Replace Sequence Index Values to Real Sequence Value
		if (seqParts != null) {
			int size = seqParts.size();

			String valuePattern = (input.isTest() ? "@@TEST," : "") + pattern;
			IdRuleSeq seq = BeanUtils.get(IdRuleSeqRepository.class).findByIdRuleCodeAndValuePattern(code, valuePattern).orElse(null);
			if (seq == null) {
				SyncCtrlUtils.lock("ID_RULE_SEQ::" + code + "," + valuePattern);
				seq = BeanUtils.get(IdRuleSeqRepository.class).findByIdRuleCodeAndValuePattern(code, valuePattern).orElse(null);
				if (seq == null) {
					seq = new IdRuleSeq();
					seq.setIdRuleCode(code);
					seq.setValuePattern(valuePattern);
					BeanUtils.get(IdRuleSeqRepository.class).save(seq);
				}
			}
			if (ObjectUtils.isEmpty(seq.getLastValues())) {
				StringBuilder buf = new StringBuilder();
				int i = 0;
				for (IdPart col : seqParts) {
					String value;
					if (ObjectUtils.isEmpty(col.getExpression())) {
						value = "0";
					} else {
						String expr = col.getExpression();
						if (expr.contains(",")) {
							expr = expr.substring(0, expr.indexOf(",")).trim();
						}
						if (!ValueUtils.isNumber(expr)) {
							value = "0";
						} else {
							long initVal = ValueUtils.toLong(expr, 0L);
							if (i == size - 1) {
								initVal -= 1L;
							}
							value = initVal + "";
						}
					}
					buf.append(i++ == 0 ? "" : ",").append(value);
				}
				seq.setLastValues(buf.toString());
			}

			List<String> lastSeqs;
			if (size == 1) {
				lastSeqs = Arrays.asList(seq.getLastValues());
			} else {
				lastSeqs = new ArrayList<>(size);
				for (String item : StringUtils.tokenizeToStringArray(seq.getLastValues(), ",")) {
					lastSeqs.add(item);
				}
			}

			List<String> lastSeqStrs = new ArrayList<>(lastSeqs.size());
			boolean increase = true;
			// i starts at size (5,4,3,2,1)
			for (int i = size; i > 0;) {
				i--;
				IdPart part = seqParts.get(i);

				// Get the last sequence value of current index(i) sequence
				long lastSeq;
				{
					String lastSeqStr = lastSeqs.size() <= i ? 0 + "" : lastSeqs.get(i);
					lastSeq = ValueUtils.toLong(lastSeqStr, 0L);
				}

				if (increase) {
					lastSeq++;
					increase = false;
				}

				String value;
				{
					value = lastSeq + "";
					int minLength = ValueUtils.toInteger(part.getMinLength(), 0);
					int maxLength = ValueUtils.toInteger(part.getMaxLength(), 0);
					if (minLength > 0) {
						value = ValueUtils.pad(value, minLength, "left", "0");
					}
					if (maxLength > 0 && value.length() > maxLength) {
						// If i is 0, it is the last index
						if (i == 0) {
							throw new BizException("ID_RULE_SEQ_OVER", new Property("id_rule_code", code), new Property("part_index", size - i));
						}
						value = value.substring(1);
						increase = true;
					}
				}

				id = StringUtils.replace(id, DELI_OPEN + i + DELI_CLOSE, value);
				lastSeqStrs.add(0, value);
			}

			String lastValues = StringUtils.collectionToCommaDelimitedString(lastSeqStrs);
			seq.setLastValues(lastValues);
			BeanUtils.get(IdRuleSeqRepository.class).save(seq);
		}

		return id;
	}
}
