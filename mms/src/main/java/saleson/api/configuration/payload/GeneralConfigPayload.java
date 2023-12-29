package saleson.api.configuration.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.ConfigCategory;
import saleson.model.config.GeneralConfig;
import saleson.model.config.QGeneralConfig;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GeneralConfigPayload extends SearchParam {
	private ConfigCategory configCategory;
	private String fieldName;
	private Boolean required;
	private Boolean defaultInput;
	private String defaultInputValue;
	private Boolean deletedField;

	public GeneralConfig getModel(GeneralConfig generalConfig) {
		if (generalConfig == null)
			generalConfig = new GeneralConfig();
		bindData(generalConfig);
		return generalConfig;
	}

	private void bindData(GeneralConfig generalConfig) {
		generalConfig.setConfigCategory(configCategory);
		generalConfig.setFieldName(fieldName);
		generalConfig.setRequired(required);
		generalConfig.setDefaultInput(defaultInput);
		generalConfig.setDefaultInputValue(defaultInputValue);
		if (deletedField != null)
			generalConfig.setDeletedField(deletedField);
	}

	public Predicate getPredicate() {
		QGeneralConfig generalConfig = QGeneralConfig.generalConfig;

		BooleanBuilder builder = new BooleanBuilder();

		if (getConfigCategory() != null)
			builder.and(generalConfig.configCategory.eq(configCategory));
		if (getFieldName() != null)
			builder.and(generalConfig.fieldName.eq(fieldName));
		if (deletedField != null)
			builder.and(generalConfig.deletedField.eq(deletedField));
		if (required != null)
			builder.and(generalConfig.required.eq(required));

		return builder;
	}
}
