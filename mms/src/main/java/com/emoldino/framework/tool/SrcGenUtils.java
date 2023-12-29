package com.emoldino.framework.tool;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SrcGenUtils {
	public static final String TAB = "\t";

	public static String toClassSrc(String name, boolean infFlag, String extName, List<String> infNames, List<String> imports, List<String> annotations, String source) {
		StringBuilder buf = new StringBuilder();
		if (!ObjectUtils.isEmpty(annotations)) {
			annotations.forEach(ann -> {
				if (!ann.startsWith("\r\n") && !ann.startsWith("\n")) {
					buf.append("\r\n");
				}
				buf.append(ann);
			});
		}
		return toClassSrc(name, infFlag, extName, infNames, imports, buf.toString(), source);
	}

	public static String toClassSrc(String name, boolean infFlag, String extName, List<String> infNames, List<String> imports, String annotations, String source) {
		int index = name.lastIndexOf('.');
		String pkg = name.substring(0, index);
		String simpleName = name.substring(index + 1);

		if (imports == null) {
			imports = new ArrayList<String>();
		}

		boolean serializable = false;
		if (extName != null && extName.indexOf('.') > 0) {
			String extClassName = extName.contains("<") ? extName.substring(0, extName.indexOf("<")) : extName;
			try {
				Class<?> extendsClass = ClassUtils.getClass(extClassName);
				serializable = Serializable.class.isAssignableFrom(extendsClass);
			} catch (Exception e) {
			}
			int lastIndex = extName.lastIndexOf('.');
			String extPkg = extName.substring(0, lastIndex);
			String extSimpleName = extName.substring(lastIndex + 1);
			if (!simpleName.equals(extSimpleName)) {
				if (!extPkg.equals(pkg)) {
					addImports(imports, extClassName);
				}
				extName = extSimpleName;
			}
		}

		if (!ObjectUtils.isEmpty(infNames)) {
			List<String> _infNames = new ArrayList<>(infNames.size());
			for (String infName : infNames) {
				int lastIndex = infName.lastIndexOf('.');
				String infPkg = infName.substring(0, lastIndex);
				String infSimpleName = infName.substring(lastIndex + 1);
				if (simpleName.equals(infSimpleName)) {
					_infNames.add(infName);
				} else {
					if (!infPkg.equals(pkg)) {
						addImports(imports, infSimpleName);
					}
					_infNames.add(infSimpleName);
				}
				infNames = _infNames;
			}
		}

		Map<String, String> importMap = new TreeMap<>();
		if (!ObjectUtils.isEmpty(imports)) {
			for (String imp : imports) {
				importMap.put(imp, imp);
			}
		}

		StringBuilder buf = new StringBuilder("package ").append(pkg).append(";");

		int i = 0;
		String prevPrefix = null;
		for (String key : importMap.keySet()) {
			if (key.indexOf('.') == -1 || !key.startsWith("java.") || key.startsWith("java.lang.")) {
				continue;
			}
			prevPrefix = "java";
			buf.append(i == 0 ? "\r\n" : "").append("\r\nimport ").append(key).append(";");
			i++;
		}
		for (String key : importMap.keySet()) {
			if (key.indexOf('.') == -1 || key.startsWith("java.") || pkg.equals(key.substring(0, key.lastIndexOf('.')))) {
				continue;
			}
			String prefix = key.substring(0, key.indexOf('.'));
			if (prevPrefix != null && !prevPrefix.endsWith(prefix)) {
				buf.append("\r\n");
			}
			prevPrefix = prefix;
			buf.append(i == 0 ? "\r\n" : "").append("\r\nimport ").append(key).append(";");
			i++;
		}

		buf.append("\r\n");

		if (!ObjectUtils.isEmpty(annotations)) {
			buf.append(annotations);
		}
		if (serializable) {
			buf.append("\r\n@SuppressWarnings(\"serial\")");
		}
		buf.append("\r\npublic ").append(infFlag ? "interface " : "class ").append(simpleName);
		if (extName != null && extName.length() > 0) {
			buf.append(" extends ").append(extName);
		}
		if (!ObjectUtils.isEmpty(infNames)) {
			buf.append(" implements ");
			int j = 0;
			for (String infName : infNames) {
				buf.append(j++ == 0 ? "" : ", ").append(infName);
			}
		}
		buf.append(" {");
		if (source == null || source.trim().length() == 0) {
			buf.append("\r\n");
		} else {
			if (!source.startsWith("\r\n") && !source.startsWith("\n")) {
				buf.append("\r\n");
			}
			buf.append(source);
		}
		buf.append("\r\n}\r\n");

		return buf.toString();
	}

	public static void addImports(List<String> imports, Object... classes) {
		if (classes == null || classes.length == 0) {
			return;
		}
		for (Object clazz : classes) {
			String key = clazz instanceof Class ? ((Class<?>) clazz).getName() : clazz.toString();
			if (imports.contains(key)) {
				continue;
			}
			imports.add(key);
		}
	}

	public static void addImports(String src, List<String> imports, Object... classes) {
		if (src == null || classes == null || classes.length == 0) {
			return;
		}

		for (Object clazz : classes) {
			if (clazz == null) {
				continue;
			}

			String key = clazz instanceof Class ? ((Class<?>) clazz).getName() : clazz.toString();
			if (imports.contains(key)) {
				continue;
			}

			String checkKey = key.contains(".") ? key.substring(key.lastIndexOf(".") + 1) : key;
			if (!containsWord(src, checkKey)) {
				continue;
			}

			imports.add(key);
		}
	}

	private static boolean containsWord(String sentence, String word) {
		Pattern p = Pattern.compile("\\b" + word + "\\b", Pattern.MULTILINE);
		Matcher m = p.matcher(sentence);
		return m.find();
	}

	public static void appendComment(StringBuilder buf, String author) {
		buf.append("/**");
		try {
			String user;
			if (ObjectUtils.isEmpty(author)) {
				user = System.getProperty("user.name");
			} else {
				user = author;
			}

			if (!ObjectUtils.isEmpty(user)) {
				buf.append("\r\n * @author ").append(user);
			}
		} catch (Exception e) {
		}
		buf.append("\r\n */");
	}

	public static SrcItem toSrcItem(ClassDef clazz) throws Exception {
		if (clazz == null) {
			return null;
		}
		String path = "src/main/java/" + StringUtils.replace(clazz.getPkg(), ".", "/");
		path += "/" + clazz.getSimpleName() + ".java";
		SrcItem item = new SrcItem("java", path, clazz.getSrc());
		return item;
	}

	protected static void write(ClassDef clazz, boolean overwrite) throws Exception {
		if (clazz == null) {
			return;
		}
		SrcItem item = toSrcItem(clazz);
		SrcGenUtils.write(item.getPath(), item.getText(), overwrite);
	}

	public static void write(String path, String str, boolean overwrite) throws Exception {
		path = StringUtils.replace(path, "\\", "/");
		if (!SrcGenUtils.checkGenFilePath(path)) {
			return;
		}

		File file = new File(path);
		if (!overwrite && file.exists()) {
			System.out.println("File: " + file.getPath() + " already exists!!");
			return;
		}

		String dirPath = path.substring(0, path.lastIndexOf('/'));
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		} else if (!dir.isDirectory()) {
			System.out.println("Path: " + dir.getPath() + " is not a directory!!");
			return;
		}

		file = new File(path);
		FileUtils.writeStringToFile(file, str, "UTF-8", false);
	}

	private static boolean checkGenFilePath(String filePath) {
		if (!filePath.contains("src/main/")) {
			System.out.println("Invalid file path : " + filePath);
			return false;
		}
		return true;
	}

	@Data
	public static class ClassDef {
		private String pkg;
		private String simpleName;
		private String src;
		private Class<?> clazz;

		public ClassDef(String pkg, String simpleName) {
			this.pkg = pkg;
			this.simpleName = simpleName;
		}

		public ClassDef(String pkg, String simpleName, String src) {
			this.pkg = pkg;
			this.simpleName = simpleName;
			this.src = src;
		}

		public String toString() {
			return pkg + "." + simpleName;
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SrcItem {
		private String type;
		private String path;
		private String text;
	}

}
