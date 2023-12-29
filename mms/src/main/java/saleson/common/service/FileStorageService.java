package saleson.common.service;

import com.emoldino.framework.dto.SuccessOut;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import saleson.api.filestorage.payload.FileStoragePayload;
import saleson.api.filestorage.payload.FileUploadPost;
import saleson.common.enumeration.StorageType;
import saleson.common.util.DataUtils;
import saleson.common.util.FileUtils;
import saleson.model.FileStorage;
import saleson.service.s3storage.S3ServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileStorageService {
	@Value("${file.storage.location}")
	private String fileStorageLocation;

	@Value("${file.upload.dir}")
	private String fileUploadDir;

	@Value("${system.storage.type}")
	private String systemStorageType;

	@Autowired
	private FileStorageRepository fileStorageRepository;

	@Autowired
	private S3ServiceImpl s3Service;

	public String save(FileInfo fileInfo) {
		if(systemStorageType.equalsIgnoreCase("cloud")) {
			for (MultipartFile file : fileInfo.getFiles()) {
				String uploadDir = fileInfo.getStorageType().name() + "/" + fileInfo.getRefId();

				// Normalize file name
				String fileName = StringUtils.cleanPath(file.getOriginalFilename());

				String extension = FileUtils.getExtension(fileName);

				String newFileName = fileName + "_" + Instant.now().getEpochSecond() + "." + extension;

				Thread t = new Thread(() -> {
					String saveLocation = null;
					try {
						saveLocation = s3Service.uploadFileTos3bucket(uploadDir, newFileName, file);
					} catch (URISyntaxException e) {
						throw new RuntimeException("URI wrong. Please try again!", e);
					} catch (IOException e) {
						throw new RuntimeException("Could not store file . Please try again!", e);
					}
					FileStorage fileStorage = fileInfo.getFileStorage();
					fileStorage.setFileName(fileName);
					fileStorage.setFileSize(file.getSize());
					fileStorage.setSaveLocation(saveLocation);
					fileStorageRepository.save(fileStorage);
				});
				t.start();
			}
		} else {
			try {
				for (MultipartFile file : fileInfo.getFiles()) {
					String uploadDir = fileUploadDir + File.separator + fileInfo.getStorageType().name() + File.separator + fileInfo.getRefId();

					Path saveLocation = Paths.get(fileStorageLocation + uploadDir).toAbsolutePath().normalize();

					try {
						Files.createDirectories(saveLocation);
					} catch (Exception e) {

					}

					// Normalize file name
					String fileName = StringUtils.cleanPath(file.getOriginalFilename());
					String saveFileName = FileUtils.getNewFileName(fileStorageLocation + uploadDir + File.separator + fileName);

					// Copy file to the target location (Replacing existing file with the same name)
					Path targetLocation = saveLocation.resolve(saveFileName);
					Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

					FileStorage fileStorage = fileInfo.getFileStorage();
					fileStorage.setFileName(fileName);
					fileStorage.setFileSize(file.getSize());
					fileStorage.setSaveLocation(uploadDir + File.separator + saveFileName);
					fileStorageRepository.save(fileStorage);

				}


			} catch (IOException ex) {
				throw new RuntimeException("Could not store file . Please try again!", ex);
				//throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
			}
		}
		return "";
	}

	public String saveSynchronized(FileInfo fileInfo) {
		if(systemStorageType.equalsIgnoreCase("cloud")) {
			for (MultipartFile file : fileInfo.getFiles()) {
				String uploadDir = fileInfo.getStorageType().name() + "/" + fileInfo.getRefId();

				// Normalize file name
				String fileName = StringUtils.cleanPath(file.getOriginalFilename());

				String extension = FileUtils.getExtension(fileName);

				String newFileName = fileName + "_" + Instant.now().getEpochSecond() + "." + extension;


				String saveLocation = null;
				try {
					saveLocation = s3Service.uploadFileTos3bucket(uploadDir, newFileName, file);
				} catch (URISyntaxException e) {
					throw new RuntimeException("URI wrong. Please try again!", e);
				} catch (IOException e) {
					throw new RuntimeException("Could not store file . Please try again!", e);
				}
				FileStorage fileStorage = fileInfo.getFileStorage();
				fileStorage.setFileName(fileName);
				fileStorage.setFileSize(file.getSize());
				fileStorage.setSaveLocation(saveLocation);
				fileStorageRepository.save(fileStorage);

			}
		} else {
			try {
				for (MultipartFile file : fileInfo.getFiles()) {
					String uploadDir = fileUploadDir + File.separator + fileInfo.getStorageType().name() + File.separator + fileInfo.getRefId();

					Path saveLocation = Paths.get(fileStorageLocation + uploadDir).toAbsolutePath().normalize();

					try {
						Files.createDirectories(saveLocation);
					} catch (Exception e) {

					}

					// Normalize file name
					String fileName = StringUtils.cleanPath(file.getOriginalFilename());
					String saveFileName = FileUtils.getNewFileName(fileStorageLocation + uploadDir + File.separator + fileName);

					// Copy file to the target location (Replacing existing file with the same name)
					Path targetLocation = saveLocation.resolve(saveFileName);
					Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

					FileStorage fileStorage = fileInfo.getFileStorage();
					fileStorage.setFileName(fileName);
					fileStorage.setFileSize(file.getSize());
					fileStorage.setSaveLocation(uploadDir + File.separator + saveFileName);
					fileStorageRepository.save(fileStorage);

				}


			} catch (IOException ex) {
				throw new RuntimeException("Could not store file . Please try again!", ex);
				//throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
			}
		}
		return "";
	}


	public Page<FileStorage> findAll(Predicate predicate, Pageable pageable) {
		return fileStorageRepository.findAll(predicate, pageable);
	}


	public Iterable<FileStorage> findAll(Predicate predicate) {
		return fileStorageRepository.findAll(predicate);
	}
	public Long countAll(StorageType storageType,Long refId) {
		FileStoragePayload payload = new FileStoragePayload();
		payload.setStorageType(storageType);
		payload.setRefId(refId);
		return countAll(payload.getPredicate());
	}
	private Long countAll(Predicate predicate) {
		return fileStorageRepository.count(predicate);
	}

	public Iterable<FileStorage> findAllAfterDeteleFile(FileStoragePayload payload) {

		Optional<FileStorage> fileStorageOptional = fileStorageRepository.findById(payload.getId());
		if (fileStorageOptional.isPresent()) {
			// 1. 파일 삭제

			FileStorage fileStorage = fileStorageOptional.get();

			payload.setStorageType(fileStorage.getStorageType());
			payload.setRefId(fileStorage.getRefId());
			payload.setRefId2(fileStorage.getRefId2());

			// 2. 데이터 삭제
			fileStorageRepository.delete(fileStorage);
		}


		// 3. 삭제후 파일 목록을 리턴
		return fileStorageRepository.findAll(payload.getPredicate());
	}

	public FileStorage copyDocument(FileStorage fileStorage){
		FileStorage clone = new FileStorage();
		clone.setFileName(fileStorage.getFileName());
		clone.setFileSize(fileStorage.getFileSize());
		clone.setSaveLocation(fileStorage.getSaveLocation());
		clone.setStorageType(fileStorage.getStorageType());
		return clone;
	}

	public List<FileStorage> saveAll(List<FileStorage> fileStorageList){
		return fileStorageRepository.saveAll(fileStorageList);
	}

	public SuccessOut post(FileUploadPost fileUploadPost) {
		save(new FileInfo(fileUploadPost.getStorageType(), fileUploadPost.getRefId(), fileUploadPost.getFiles()));
		return SuccessOut.getDefault();
	}

	public void cloneFile(StorageType storageTypeSource, Long refIdSource,StorageType storageTypeDest, Long refIdDest){
		List<FileStorage> fileStorageSources= fileStorageRepository.findByRefIdAndStorageType(refIdSource,storageTypeSource);
		List<FileStorage> fileStorageDest = fileStorageSources.stream().map(fileStorage -> {
			FileStorage fileStorage1 = new FileStorage();
			fileStorage1.setStorageType(storageTypeDest);
			fileStorage1.setRefId(refIdDest);

			fileStorage1.setFileName(fileStorage.getFileName());
			fileStorage1.setSaveLocation(fileStorage.getSaveLocation());
			fileStorage1.setFileSize(fileStorage.getFileSize());
			return fileStorage1;
		}).collect(Collectors.toList());
		if(!fileStorageDest.isEmpty()){
			fileStorageRepository.saveAll(fileStorageDest);
		}
	}
	public void removeAndCloneFile(StorageType storageTypeSource, Long refIdSource,StorageType storageTypeDest, Long refIdDest,List<Long> removeFileIds) {
		List<FileStorage> fileStorageSources= fileStorageRepository.findByRefIdAndStorageType(refIdSource,storageTypeSource);
		List<FileStorage> fileStorageDest = fileStorageSources.stream()
				.filter(file -> {if(removeFileIds==null) return true;
					else return removeFileIds.stream().noneMatch(id -> file.getId().equals(id));
				})
				.map(fileStorage -> {
			FileStorage fileStorage1 = new FileStorage();
			fileStorage1.setStorageType(storageTypeDest);
			fileStorage1.setRefId(refIdDest);

			fileStorage1.setFileName(fileStorage.getFileName());
			fileStorage1.setSaveLocation(fileStorage.getSaveLocation());
			fileStorage1.setFileSize(fileStorage.getFileSize());
			return fileStorage1;
		}).collect(Collectors.toList());
		if(!fileStorageDest.isEmpty()){
			fileStorageRepository.saveAll(fileStorageDest);
		}
	}

	public void updateFile(Long refIdSource,Long refIdDes,StorageType storageType){
		List<FileStorage> fileStorageSources= fileStorageRepository.findByRefIdAndStorageType(refIdSource,storageType);
		List<FileStorage> fileStorageDests = fileStorageRepository.findByRefIdAndStorageType(refIdDes,storageType);
		List<FileStorage> fileStorageNewList =  fileStorageSources.stream().filter(fileStorageSource ->  fileStorageDests.stream().noneMatch( fileStorageDest ->  fileStorageSource.getFileName().equals(fileStorageDest.getFileName()))).map( fileStorage -> {
					FileStorage fileStorage1 = new FileStorage();
					fileStorage1.setStorageType(storageType);
					fileStorage1.setRefId(refIdDes);
					fileStorage1.setFileName(fileStorage.getFileName());
					fileStorage1.setSaveLocation(fileStorage.getSaveLocation());
					fileStorage1.setFileSize(fileStorage.getFileSize());
					return fileStorage1;
		}
		).collect(Collectors.toList());
		fileStorageDests.addAll(fileStorageNewList);
		List<FileStorage> fileRemoveList = fileStorageDests.stream().filter(fileStorageDest ->
			fileStorageSources.stream().noneMatch( fileStorageSource -> fileStorageDest.getFileName().equals(fileStorageSource.getFileName()))).collect(Collectors.toList());
		if(!fileStorageNewList.isEmpty()){
			fileStorageRepository.saveAll(fileStorageNewList);
		}
		if (!fileRemoveList.isEmpty()){
			fileStorageRepository.deleteAll(fileRemoveList);
		}

	}
}
