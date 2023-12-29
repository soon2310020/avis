package saleson.model;

import saleson.common.enumeration.StorageType;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Entity
/*@Table(indexes = {
		@Index(name="IDX_STORAGETYPE_REFID", columnList = "STORAGE_TYPE, REF_ID")
})*/
public class FileStorage extends UserDateAudit {

	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 50)
	@Enumerated(EnumType.STRING)
	private StorageType storageType;

	private Long refId;
	private Long refId2;

	private String fileName;
	private String saveLocation;
	private Long fileSize;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRefId2() {
		return refId2;
	}

	public void setRefId2(Long refId2) {
		this.refId2 = refId2;
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



	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSaveLocation() {
		return saveLocation;
	}

	public void setSaveLocation(String saveLocation) {
		this.saveLocation = saveLocation;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
}
