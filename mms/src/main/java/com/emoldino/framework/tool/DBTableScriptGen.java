package com.emoldino.framework.tool;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.ClassUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.framework.enumeration.EmColumnDataType;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DBTableScriptGen extends AbstractTool {

//	private static final List<String> PKGS = Arrays.asList("com.emoldino.api");
//	private static final List<String> MODULES = Arrays.asList("asset");
//	private static final List<String> MODELS = Arrays.asList();

	private static final Map<String, String> MODEL_MAP = new MapBuilder<String, String>()//
//			.put("Area", "com.emoldino.api.common.resource.base.location.repository.area.Area")//
//			.put("LoginHist", "com.emoldino.api.common.resource.base.accesscontrol.repository.loginhist.LoginHist")//
//			.put("MoldProcChg", "com.emoldino.api.analysis.resource.base.data.repository.moldprocchg.MoldProcChg")//
//			.put("MoldPmPlan", "com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.MoldPmPlan")//
//			.put("Noti", "com.emoldino.api.common.resource.base.noti.repository.noti.Noti")//
//			.put("NotiContent", "com.emoldino.api.common.resource.base.noti.repository.noticontent.NotiContent")//
//			.put("NotiRecipient", "com.emoldino.api.common.resource.base.noti.repository.notirecipient.NotiRecipient")//
//			.put("NotiRecipientConfig", "com.emoldino.api.common.resource.base.noti.repository.notirecipientconfig.NotiRecipientConfig")//
//			.put("OptionFieldValue", "com.emoldino.api.common.resource.base.option.repository.optionfieldvalue.OptionFieldValue")//
//			.put("ProdMoldStat", "com.emoldino.api.supplychain.resource.base.product.repository.prodmoldstat.ProdMoldStat")//
			.build();

	public static void main(String[] args) {
		try {
			gen(args);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Good-bye!");
		}
	}

	private static final String RN = "\r\n";
	private static final String RNT = RN + "\t";

	private static void gen(String[] args) throws Exception {

//		List<String> pkgs = scanAll("package");
//		List<String> pkgs = new ArrayList<>();
//		if (pkgs == null) {
//			return;
//		} else if (pkgs.isEmpty()) {
//			pkgs = PKGS;
//		}

//		List<String> modules = scanAll("module");
//		List<String> modules = new ArrayList<>();
//		if (modules == null) {
//			return;
//		} else if (modules.isEmpty()) {
//			modules = MODULES;
//		}

		List<String> models = scanAll("model");
//		List<String> models = new ArrayList<>();
		if (models == null) {
			return;
		} else if (models.isEmpty()) {
//			models = MODELS;
			models = new ArrayList<>(MODEL_MAP.keySet());
		}

//		List<String> modelExclusions = scanAll("model-exclusion");
//		if (modelExclusions == null) {
//			return;
//		}

//		List<String> dbTypes = Arrays.asList("mysql");

		if (models.isEmpty()) {
			return;
		}

		boolean genScript;
		{
			String value = scan("gen script?");
			if (value == null) {
				return;
			}
			genScript = ValueUtils.toBoolean(value, false);
		}
		boolean genClass;
		{
			String value = scan("gen class?");
			if (value == null) {
				return;
			}
			genClass = ValueUtils.toBoolean(value, false);
		}

		Map<String, Field> columns = collectDictionary(null);

		for (String model : models) {
			if (!MODEL_MAP.containsKey(model)) {
				throw new BizException("UNKNOWN_MODEL", "Unknown Model: " + model);
			}

			String className = MODEL_MAP.get(model);
			Class<?> clazz = ClassUtils.getClass(className);
			List<String> imports = new ArrayList<String>();
			StringBuilder annBuf = new StringBuilder();

			Tab tab = new Tab();

			// 1. Class Annotation
			{
				Map<Class<?>, Annotation> anns = new LinkedHashMap<>();
				if (!ObjectUtils.isEmpty(clazz.getAnnotations())) {
					Arrays.asList(clazz.getAnnotations()).forEach(ann -> anns.put(ann.annotationType(), ann));
				}

				// 1.1 @Data
				append(annBuf, anns, Data.class, imports, RN, true);

				// 1.2 @NoArgsConstructor
				append(annBuf, anns, NoArgsConstructor.class, imports, RN, true);

				// 1.3 @AllArgsConstructor
				append(annBuf, anns, AllArgsConstructor.class, imports, RN, false);

				// 1.4 @Entity
				{
					SrcGenUtils.addImports(imports, Entity.class);
					annBuf.append(RN).append("@").append(Entity.class.getSimpleName());
					Entity ann = (Entity) anns.remove(Entity.class);
					if (ann != null) {
						if (!ObjectUtils.isEmpty(ann.name())) {
							tab.name = ann.name().toUpperCase();
						}

						int i = 0;
						i = append(annBuf, "name", ann.name(), null, i);
						if (i != 0) {
							annBuf.append(")");
						}
					}
				}

				// 1.5 @EntityListeners
				SrcGenUtils.addImports(imports, EntityListeners.class, AuditingEntityListener.class);
				annBuf.append(RN).append("@EntityListeners(AuditingEntityListener.class)");
				anns.remove(EntityListeners.class);

				// 1.6 @Table
				if (anns.containsKey(Table.class)) {
					SrcGenUtils.addImports(imports, Table.class);
					Table ann = (Table) anns.remove(Table.class);
					StringBuilder buf = new StringBuilder("@").append(Table.class.getSimpleName());

					if (!ObjectUtils.isEmpty(ann.name())) {
						tab.name = ann.name().toUpperCase();
					}

					tab.uniqueConstraints = ann.uniqueConstraints();
					tab.indexes = ann.indexes();

					int i = 0;
					i = append(buf, "name", ann.name(), null, i);
					if (!ObjectUtils.isEmpty(ann.uniqueConstraints())) {
						SrcGenUtils.addImports(imports, UniqueConstraint.class);
						int len = ann.uniqueConstraints().length;
						buf.append(i++ == 0 ? "(" : ", ").append("uniqueConstraints = ");
						if (len > 1) {
							buf.append("{ ");
						}
						int j = 0;
						for (UniqueConstraint uc : ann.uniqueConstraints()) {
							buf.append(j++ == 0 ? "" : ", ").append("@UniqueConstraint");
							int k = 0;
							k = append(buf, "name", uc.name(), null, k);
							k = append(buf, "columnNames", uc.columnNames(), k);
							if (k != 0) {
								buf.append(")");
							}
						}
						if (len > 1) {
							buf.append(" }");
						}
					}
					if (!ObjectUtils.isEmpty(ann.indexes())) {
						SrcGenUtils.addImports(imports, Index.class);
						int len = ann.indexes().length;
						buf.append(i++ == 0 ? "(" : ", ").append("indexes = ");
						if (len > 1) {
							buf.append("{ ");
						}
						int j = 0;
						for (Index index : ann.indexes()) {
							buf.append(j++ == 0 ? "" : ", ").append("@Index");
							int k = 0;
							k = append(buf, "name", index.name(), null, k);
							k = append(buf, "columnList", index.columnList(), null, k);
							if (index.unique()) {
								k = append(buf, "unique", index.unique(), false, k);
							}
							if (k != 0) {
								buf.append(")");
							}
						}
						if (len > 1) {
							buf.append(" }");
						}
					}
					if (i != 0) {
						buf.append(")");
					}
					annBuf.append(RN).append(buf.toString());
				}

				// 1.7 @DynamicUpdate
				append(annBuf, anns, DynamicUpdate.class, imports, RN, true);

				anns.forEach((annClass, ann) -> {
					throw new BizException("UNSUPPORTED_ANNOTATION", "Unsupported Annotation: " + annClass.getName());
				});
			}

			if (ObjectUtils.isEmpty(tab.name)) {
				tab.name = ValueUtils.toDelimited(clazz.getSimpleName(), '_', true).toUpperCase();
			}

			// 2 Field Annotations
			StringBuilder srcBuf = new StringBuilder();
			boolean rnFlag = false;
			for (Field fieldCandidate : ReflectionUtils.getFieldList(clazz, false)) {
				String fieldName = fieldCandidate.getName();
				if (!columns.containsKey(fieldName)) {
					if (!ValueUtils.isPrimitiveType(fieldCandidate.getType())) {
						continue;
					}
					Column ann = fieldCandidate.getAnnotation(Column.class);
					if (ann != null && !ann.insertable() && !ann.updatable()) {
						continue;
					}
					throw new BizException("UNDECLARED_FIELD", "Undeclared Field: " + fieldName);
				}

				Col col = new Col();

				if (rnFlag) {
					srcBuf.append(RN);
					rnFlag = false;
				}

				Field field = columns.get(fieldName);

				Map<Class<?>, Annotation> anns = new LinkedHashMap<>();
				if (!ObjectUtils.isEmpty(field.getAnnotations())) {
					Arrays.asList(field.getAnnotations()).forEach(ann -> anns.put(ann.annotationType(), ann));
				}

				col.pk = anns.containsKey(Id.class);

				// 2.1 @Id
				col.notNull = anns.containsKey(Id.class);
				rnFlag = append(srcBuf, anns, Id.class, imports, RNT, false) || rnFlag;

				// 2.2 @GeneratedValue
				if (anns.containsKey(GeneratedValue.class)) {
					GeneratedValue ann = (GeneratedValue) anns.remove(GeneratedValue.class);
					SrcGenUtils.addImports(imports, GeneratedValue.class);
					srcBuf.append(RNT).append("@GeneratedValue");
					int i = 0;
					col.autoIncrement = GenerationType.IDENTITY.equals(ann.strategy());
					i = append(srcBuf, "strategy", ann.strategy(), GenerationType.AUTO, imports, i);
					i = append(srcBuf, "generator", ann.generator(), null, i);
					if (i != 0) {
						srcBuf.append(")");
					}
					rnFlag = true;
				}

				// 2.3 @CreatedBy
				append(srcBuf, anns, CreatedBy.class, imports, RNT, false);

				// 2.4 @CreatedDate
				append(srcBuf, anns, CreatedDate.class, imports, RNT, false);

				// 2.5 @LastModifiedBy
				append(srcBuf, anns, LastModifiedBy.class, imports, RNT, false);

				// 2.6 @LastModifiedDate
				append(srcBuf, anns, LastModifiedDate.class, imports, RNT, false);

				// 2.7 @Lob
				col.lob = anns.containsKey(Lob.class);
				append(srcBuf, anns, Lob.class, imports, RNT, false);

				// 2.8 @Enumerated
				if (anns.containsKey(Enumerated.class)) {
					Enumerated ann = (Enumerated) anns.remove(Enumerated.class);
					SrcGenUtils.addImports(imports, Enumerated.class);
					srcBuf.append(RNT).append("@Enumerated");
					int i = 0;
					i = append(srcBuf, null, ann.value(), EnumType.ORDINAL, imports, i);
					if (i != 0) {
						srcBuf.append(")");
					}
				}

				// 2.9 @Convert
				if (anns.containsKey(Convert.class)) {
					Convert ann = (Convert) anns.remove(Convert.class);
					SrcGenUtils.addImports(imports, Convert.class);
					srcBuf.append(RNT).append("@Convert");
					int i = 0;
					i = append(srcBuf, "converter", ann.converter(), void.class, imports, i);
					i = append(srcBuf, "attributeName", ann.attributeName(), null, i);
					i = append(srcBuf, "disableConversion", ann.disableConversion(), false, i);
					if (i != 0) {
						srcBuf.append(")");
					}
				}

				// 2.10 @Column
				if (anns.containsKey(Column.class)) {
					Column ann = (Column) anns.remove(Column.class);
					SrcGenUtils.addImports(imports, Column.class);
					StringBuilder abuf = new StringBuilder();
					abuf.append(RNT).append("@Column");

					if (!ObjectUtils.isEmpty(ann.name())) {
						col.name = ann.name().toUpperCase();
					}

					int i = 0;
					i = append(abuf, "name", ann.name(), null, i);
					i = append(abuf, "unique", ann.unique(), false, i);
//					i = append(abuf, "nullable", ann.nullable(), true, i);
					i = append(abuf, "insertable", ann.insertable(), true, i);
					i = append(abuf, "updatable", ann.updatable(), true, i);
					i = append(abuf, "columnDefinition", ann.columnDefinition(), null, i);
					i = append(abuf, "table", ann.table(), null, i);
//					i = append(abuf, "length", ann.length(), 255, i);
//					i = append(abuf, "precision", ann.precision(), 0, i);
//					i = append(abuf, "scale", ann.scale(), 0, i);
					if (i != 0) {
						abuf.append(")");
						srcBuf.append(abuf);
					}

					col.length = ann.length() == 255 ? 0 : ann.length();
					col.scale = ann.scale();
				}

				anns.forEach((annClass, ann) -> {
					throw new BizException("UNSUPPORTED_ANNOTATION", "Unsupported Annotation: " + annClass.getName() + " at " + clazz.getName());
				});

				Class<?> type = field.getType();
				String typeName = type.getName();
				if (typeName.contains(".") && !typeName.startsWith("java.lang.")) {
					SrcGenUtils.addImports(imports, type);
				}
				srcBuf.append(RNT).append("private ").append(type.getSimpleName()).append(" ").append(fieldName).append(";");

				if (String.class.equals(type)) {
					col.dataType = col.lob ? EmColumnDataType.LONGTEXT : EmColumnDataType.VARCHAR;
				} else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
					col.dataType = EmColumnDataType.VARCHAR;
					if (col.length == 0) {
						col.length = 1;
					}
				} else if (Integer.class.equals(type) || int.class.equals(type)) {
					col.dataType = EmColumnDataType.INTEGER;
				} else if (Long.class.equals(type) || int.class.equals(type)) {
					col.dataType = EmColumnDataType.BIGINT;
				} else if (Double.class.equals(type) || double.class.equals(type)) {
					col.dataType = EmColumnDataType.DOUBLE;
				} else if (Instant.class.equals(type)) {
					col.dataType = EmColumnDataType.DATETIME;
				} else if (type.isEnum()) {
					col.dataType = EmColumnDataType.VARCHAR;
				} else {
					throw new BizException("UNSUPORTED_DATA_TYPE", "Unsupported Data Type: " + type.getName());
				}

				if (col.name == null) {
					col.name = ValueUtils.toDelimited(fieldName, '_', true).toUpperCase();
				}
				if (col.pk) {
					tab.pk = col;
				}
				tab.cols.put(fieldName, col);
			}

			if (genScript) {
				StringBuilder buf = new StringBuilder();
				buf.append("BEGIN;");
				buf.append(RN);
				buf.append(RN).append("DROP TABLE IF EXISTS ").append(escape(tab.name)).append(";");
				buf.append(RN);
				buf.append(RN).append("CREATE TABLE ").append(escape(tab.name)).append(" (");
				for (Col col : tab.cols.values()) {
					buf.append(RNT).append(escape(col.name));
					buf.append(" ").append(col.dataType.name().toLowerCase());
					if (col.length > 0) {
						buf.append("(").append(col.length).append(")");
					}
					if (col.notNull) {
						buf.append(" not null");
					}
					if (col.autoIncrement) {
						buf.append(" auto_increment");
					}
					buf.append(",");
				}
				if (tab.pk != null) {
					buf.append(RNT).append("PRIMARY KEY (").append(escape(tab.pk.name)).append(")");
				}
				buf.append(RN).append(") DEFAULT CHARSET=UTF8;");

				for (UniqueConstraint uc : tab.uniqueConstraints) {
					appendUx(buf, tab, uc.name(), uc.columnNames());
				}

				for (Index ix : tab.indexes) {
					if (ObjectUtils.isEmpty(ix.columnList())) {
						continue;
					}
					if (ix.unique()) {
						appendUx(buf, tab, ix.name(), StringUtils.commaDelimitedListToStringArray(ix.columnList()));
					} else {
						appendIx(buf, tab, ix.name(), StringUtils.commaDelimitedListToStringArray(ix.columnList()));
					}
				}

				buf.append(RN).append(RN).append("COMMIT;");
				System.out.println();
				System.out.println(buf);
			}

			if (genClass) {
				String src = SrcGenUtils.toClassSrc(className, false, null, null, imports, annBuf.toString(), srcBuf.toString());
				System.out.println();
				System.out.println(src);
			}
		}

		System.out.println();
	}

	private static void appendUx(StringBuilder buf, Tab tab, String name, String[] cols) {
		buf.append(RN);
		buf.append(RN).append("ALTER TABLE ").append(escape(tab.name));
		buf.append(RNT).append("ADD CONSTRAINT");
		if (!ObjectUtils.isEmpty(name)) {
			buf.append(" ").append(escape(name));
		}
		buf.append(" UNIQUE (");
		int i = 0;
		for (String colName : cols) {
			Col col = tab.getCols().get(colName);
			if (col == null) {
				throw new BizException("UNKNOWN_FIELD", "Unknown Field: " + colName);
			}
			buf.append(i++ == 0 ? "" : ", ").append(escape(col.name));
		}
		buf.append(");");
	}

	private static void appendIx(StringBuilder buf, Tab tab, String name, String[] cols) {
		buf.append(RN);
		buf.append(RN).append("ALTER TABLE ").append(escape(tab.name));
		buf.append(RNT).append("ADD INDEX");
		if (!ObjectUtils.isEmpty(name)) {
			buf.append(" ").append(name);
		}
		buf.append(" (");
		int i = 0;
		for (String colName : cols) {
			Col col = tab.getCols().get(colName);
			if (col == null) {
				throw new BizException("UNKNOWN_FIELD", "Unknown Field: " + colName);
			}
			buf.append(i++ == 0 ? "" : ", ").append(escape(col.name));
		}
		buf.append(");");
	}

	@Data
	private static class Tab {
		String name;
		Col pk;
		Map<String, Col> cols = new LinkedHashMap<>();
		UniqueConstraint[] uniqueConstraints = {};
		Index[] indexes = {};
	}

	@Data
	private static class Col {
		String name;
		boolean pk;
		boolean notNull;
		boolean autoIncrement;
		EmColumnDataType dataType;
		boolean lob;
		int length;
		int scale;
//		String comment;
//		List<String> tags = new ArrayList<>();
	}

	private static String escape(String name) {
		return "`" + name + "`";
	}

	private static <T extends Annotation> boolean append(StringBuilder buf, Map<Class<?>, Annotation> anns, Class<T> annClass, List<String> imports, String rnt, boolean required) {
		if (!anns.containsKey(annClass)) {
			if (!required) {
				return false;
			}
		} else {
			anns.remove(annClass);
		}

		SrcGenUtils.addImports(imports, annClass);
		buf.append(rnt == null ? "" : rnt).append("@").append(annClass.getSimpleName());
		return true;
	}

	private static int append(StringBuilder buf, String field, String value, String defaultValue, int i) {
		if (ObjectUtils.isEmpty(value) || (defaultValue != null && defaultValue.equals(value))) {
			return i;
		}
		buf.append(i++ == 0 ? "(" : ", ");
		if (!ObjectUtils.isEmpty(field)) {
			buf.append(field).append(" = ");
		}
		buf.append("\"").append(value).append("\"");
		return i;
	}

	private static int append(StringBuilder buf, String field, Class<?> value, Class<?> defaultValue, List<String> imports, int i) {
		if (ObjectUtils.isEmpty(value) || (defaultValue != null && defaultValue.equals(value))) {
			return i;
		}
		SrcGenUtils.addImports(imports, value);
		buf.append(i++ == 0 ? "(" : ", ");
		if (!ObjectUtils.isEmpty(field)) {
			buf.append(field).append(" = ");
		}
		buf.append(value.getSimpleName()).append(".class");
		return i;
	}

	private static int append(StringBuilder buf, String field, Enum<?> value, Enum<?> defaultValue, List<String> imports, int i) {
		if (ObjectUtils.isEmpty(value) || (defaultValue != null && defaultValue.equals(value))) {
			return i;
		}
		Class<?> clazz = value.getDeclaringClass();
		SrcGenUtils.addImports(imports, clazz);
		buf.append(i++ == 0 ? "(" : ", ");
		if (!ObjectUtils.isEmpty(field)) {
			buf.append(field).append(" = ");
		}
		buf.append(clazz.getSimpleName()).append(".").append(value);
		return i;
	}

	private static int append(StringBuilder buf, String field, boolean value, boolean defaultValue, int i) {
		if (value == defaultValue) {
			return i;
		}
		buf.append(i++ == 0 ? "(" : ", ");
		if (!ObjectUtils.isEmpty(field)) {
			buf.append(field).append(" = ");
		}
		buf.append(value);
		return i;
	}

//	private static int append(StringBuilder buf, String field, int value, int defaultValue, int i) {
//		if (value == defaultValue) {
//			return i;
//		}
//		buf.append(i++ == 0 ? "(" : ", ");
//		if (!ObjectUtils.isEmpty(field)) {
//			buf.append(field).append(" = ");
//		}
//		buf.append(value);
//		return i;
//	}

	private static int append(StringBuilder buf, String field, String[] values, int i) {
		if (ObjectUtils.isEmpty(values)) {
			return i;
		} else if (values.length == 1) {
			return append(buf, field, values[0], null, i);
		}
		buf.append(i++ == 0 ? "(" : ", ");
		if (!ObjectUtils.isEmpty(field)) {
			buf.append(field).append(" = ");
		}
		buf.append("{ ");
		int j = 0;
		for (String value : values) {
			buf.append(j++ == 0 ? "" : ", ").append("\"").append(value).append("\"");
		}
		buf.append(" }");
		return i;
	}

	private static Map<String, Field> collectDictionary(StringBuilder dupBuf) throws Exception {
		Map<String, Field> dict = new TreeMap<>();
		Map<String, Class<?>> classes = new HashMap<>();

		String pattern = "classpath*:com/emoldino/framework/terminology/dictionary/*";
		collectDictinary(pattern, dict, classes, dupBuf);

		return dict;
	}

	private static void collectDictinary(String pattern, Map<String, Field> dict, Map<String, Class<?>> classes, StringBuilder dupBuf) throws Exception {
		List<Class<?>> classList;
		try {
			classList = ReflectionUtils.getClassList(pattern, "simpleName");
		} catch (IllegalArgumentException e) {
			return;
		}

		for (Class<?> clazz : classList) {
			for (Field field : ReflectionUtils.getFieldList(clazz, false)) {
				if (dict.containsKey(field.getName())) {
					if (dupBuf != null) {
						dupBuf.append("\r\n" + field.getName() + " > (");
						dupBuf.append(classes.get(field.getName()).getName() + "." + field.getName());
						dupBuf.append(", ");
						dupBuf.append(clazz.getName() + "." + field.getName() + ")");
					}
					continue;
				}
				dict.put(field.getName(), field);
				classes.put(field.getName(), clazz);
			}
		}
	}
}
