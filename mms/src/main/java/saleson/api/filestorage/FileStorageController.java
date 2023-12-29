package saleson.api.filestorage;


import com.emoldino.framework.dto.SuccessOut;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import saleson.api.counter.CounterService;
import saleson.api.filestorage.payload.FileStoragePayload;
import saleson.api.filestorage.payload.FileUploadPost;
import saleson.api.filestorage.payload.MultiFileStoragePayload;
import saleson.api.mold.MoldService;
import saleson.common.enumeration.StorageType;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.model.FileStorage;

import javax.mail.Multipart;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file-storage")
public class FileStorageController {

	@Autowired
	CounterService counterService;

	@Lazy
	@Autowired
	MoldService moldService;

	@Autowired
	FileStorageService fileStorageService;

	@Autowired
	ObjectMapper objectMapper;


	@GetMapping
	public ResponseEntity<Iterable<FileStorage>> getAll(FileStoragePayload payload) {
		Iterable<FileStorage> files = fileStorageService.findAll(payload.getPredicate());
		return new ResponseEntity<>(files, HttpStatus.OK);
	}

	@GetMapping("/mold")
	public ResponseEntity<Map<StorageType, List<FileStorage>>> getAll(MultiFileStoragePayload payload){
		Iterable<FileStorage> files = fileStorageService.findAll(payload.getPredicate());
		Map<StorageType, List<FileStorage>> result = new HashMap<>();
		files.forEach(file -> {
			if(!result.containsKey(file.getStorageType())){
				result.put(file.getStorageType(), new ArrayList<>());
			}
			result.get(file.getStorageType()).add(file);
		});
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Iterable<FileStorage>> deleteFile(
			@PathVariable("id") Long id,
			FileStoragePayload payload) {
		if (!id.equals(payload.getId())) {
			return ResponseEntity.badRequest().build();
		}

		Iterable<FileStorage> files = fileStorageService.findAllAfterDeteleFile(payload);
		return new ResponseEntity<>(files, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<Iterable<FileStorage>> saveFile(FileInfo fileInfo) {
		fileStorageService.saveSynchronized(fileInfo);
		FileStoragePayload payload = new FileStoragePayload();
		payload.setRefId(fileInfo.getRefId());
		payload.setStorageType(fileInfo.getStorageType());
		Iterable<FileStorage> files = fileStorageService.findAll(payload.getPredicate());
		return new ResponseEntity<>(files, HttpStatus.OK);
	}

/*
	@PostMapping
	SuccessOut post(FileUploadPost fileUploadPost) {
		return fileStorageService.post(fileUploadPost);
	}

*/



}
