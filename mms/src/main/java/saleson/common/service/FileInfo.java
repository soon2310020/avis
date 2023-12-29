package saleson.common.service;

import org.springframework.web.multipart.MultipartFile;
import saleson.common.enumeration.StorageType;
import saleson.model.FileStorage;

public class FileInfo {
	private StorageType storageType;
	private Long refId;
	private Long refId2;
	private MultipartFile[] files;


	public FileInfo() {

	}

	public FileInfo(StorageType storageType, Long refId) {
		this.storageType = storageType;
		this.refId = refId;
	}

	public FileInfo(StorageType storageType, Long refId, MultipartFile[] files) {
		this(storageType, refId);
		this.files = files;
	}

	public FileInfo(StorageType storageType, Long refId, MultipartFile file) {
		this(storageType, refId);
		MultipartFile[] multipartFiles = new MultipartFile[1];
		multipartFiles[0] = file;
		this.files = multipartFiles;
	}
	public FileInfo(StorageType storageType, Long refId, Long refId2, MultipartFile file) {
		this(storageType, refId);
		this.refId2 = refId2;
		MultipartFile[] multipartFiles = new MultipartFile[1];
		multipartFiles[0] = file;
		this.files = multipartFiles;
	}

	public FileInfo(StorageType storageType, Long refId, Long refId2, MultipartFile[] files) {
		this(storageType, refId);
		this.refId2 = refId2;
		this.files = files;
	}


	public StorageType getStorageType() {
		return storageType;
	}

	public void setStorageType(StorageType storageType) {
		this.storageType = storageType;
	}

	public Long getRefId() {
		return refId;
	}

	public void setRefId(Long refId) {
		this.refId = refId;
	}

	public Long getRefId2() {
		return refId2;
	}

	public void setRefId2(Long refId2) {
		this.refId2 = refId2;
	}

	public MultipartFile[] getFiles() {
		return files;
	}

	public void setFiles(MultipartFile[] files) {
		this.files = files;
	}

	public FileStorage getFileStorage() {
		FileStorage fileStorage = new FileStorage();
		fileStorage.setStorageType(getStorageType());
		fileStorage.setRefId(getRefId());

		if (getRefId2() != null) {
			fileStorage.setRefId2(getRefId2());
		}
		return fileStorage;
	}
}
