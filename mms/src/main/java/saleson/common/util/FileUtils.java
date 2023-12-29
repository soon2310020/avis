package saleson.common.util;

import org.springframework.data.util.Pair;
import saleson.dto.common.TwoObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
	public static String SPECIAL_CHARS_REGEX="[\\\\/:*?\"<>|]";

	public static String getFileNameWithoutExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return "";
		}

		String[] fileNameInfo = StringUtils.delimitedListToStringArray(getName(fileName), ".");

		if (fileNameInfo.length == 1) {
			return getName(fileName);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fileNameInfo.length - 1; i++) {
			sb.append(fileNameInfo[i]);
		}
		return sb.toString();
	}

	public static String getExtension(String fileName) {

		if (fileName == null || fileName.isEmpty()) {
			return "";
		}

		String[] fileNameInfo = StringUtils.delimitedListToStringArray(getName(fileName), ".");

		if (fileNameInfo.length == 1) {
			return "";
		}
		return fileNameInfo[fileNameInfo.length - 1].toLowerCase();
	}

	public static String getName(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return "";
		}

		String[] filePath = getFilePath(fileName);
		if (filePath.length == 1) {
			return fileName;
		}
		return filePath[filePath.length - 1];
	}

	public static String getParent(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return "";
		}

		boolean isStartWithRoot = false;
		if (fileName.startsWith("/")) {
			isStartWithRoot = true;
		}


		String[] filePath = getFilePath(fileName);
		if (filePath.length <= (isStartWithRoot ? 2 : 1)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		if (!isStartWithRoot) {
			sb.append(filePath[0]);
		}
		for (int i = 1; i < filePath.length - 1; i++) {
			sb.append("/" + filePath[i]);
		}
		return sb.toString();
	}



	public static String getNewFileName(String fullFileName) {
		String uploadPath = getParent(fullFileName);
		String extension = getExtension(fullFileName);
		String newFileName = getFileNameWithoutExtension(fullFileName);

		// 파일명은 어떻게?
		newFileName = DateUtils.getToday();		// yyyyMMddHHmmss 형식으로..

		boolean existsFile = true;
		int idx = 1;

		do {
			String filePath = uploadPath + File.separator + newFileName + "." + extension;
			File file = new File(filePath);
			if (file.exists()) {
				newFileName = newFileName + "(" + idx + ")";
				++idx;
				existsFile = true;
			} else {
				existsFile = false;
			}
		} while(existsFile);

		return newFileName + "." + extension;
	}


	public static String[] getFilePath(String fileName) {
		fileName = StringUtils.cleanPath(fileName);
		return StringUtils.delimitedListToStringArray(fileName, "/");
	}

	public static ByteArrayOutputStream zipMultipleByteFiles(List<Pair<String,ByteArrayOutputStream>> listDataFile) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ZipOutputStream zipOut = new ZipOutputStream(bos);
		for (Pair<String,ByteArrayOutputStream> dataFile : listDataFile) {
			String fileName=dataFile.getFirst();
			ByteArrayOutputStream fileOutputStream=dataFile.getSecond();
			ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);
			zipOut.write(fileOutputStream.toByteArray(), 0, fileOutputStream.toByteArray().length);
			fileOutputStream.close();
		}
		zipOut.close();
		return bos;
	}
	public static ByteArrayOutputStream zipMultipleFiles(List<TwoObject<String, Path>> listDataFile) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		ZipOutputStream zipOut = new ZipOutputStream(bos);
		for (TwoObject<String,Path> dataFile : listDataFile) {
			String fileName=dataFile.getLeft();
			Path fileOutputStream=dataFile.getRight();
			ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);
			zipOut.write(Files.readAllBytes(fileOutputStream));
			zipOut.flush();
		}
		zipOut.close();
		return bos;
	}

	public static String convertToValidFileName(String name) {
		if (name != null) {
			name = name.replaceAll(SPECIAL_CHARS_REGEX, "");
		}
		return name;
	}

	/**
	 * catch duplicate file name case
	 * @param name
	 * @param originalNames
	 * @return
	 */
	public static String getValidFileNameInList(String name,List<String> originalNames){
		String validName=name;
		if (name != null && originalNames != null && !originalNames.isEmpty()) {
			Map<String,List<String>> mapValidToOriginals=new HashMap<>();
			List<String> validNames=originalNames.stream().map(n->{
				List<String> originals=new ArrayList<>();
				String convertVal=convertToValidFileName(n);
				if(mapValidToOriginals.containsKey(convertVal.toLowerCase())){
					originals=mapValidToOriginals.get(convertVal.toLowerCase());
				}else {
					mapValidToOriginals.put(convertVal.toLowerCase(),originals);
				}
				originals.add(n);
				return convertVal;
			}).collect(Collectors.toList());
			validName=convertToValidFileName(name);
			if(mapValidToOriginals.containsKey(validName.toLowerCase())&&mapValidToOriginals.get(validName.toLowerCase()).size()>1){
				int indexDuplicateName=mapValidToOriginals.get(validName.toLowerCase()).indexOf(name);
				if(indexDuplicateName>0){
					int indexExtends=validName.lastIndexOf(".");
					if(indexExtends>=0){
						validName=validName.substring(0,indexExtends)+" ("+indexDuplicateName+")"+validName.substring(indexExtends);

					}else
					validName=validName+" ("+indexDuplicateName+")";
				}
			}
		}
		return validName;
	}
}
