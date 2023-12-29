package saleson.api.filestorage.payload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import saleson.common.enumeration.StorageType;

@Data
public class FileUploadPost {
    StorageType storageType;
    Long refId;
    MultipartFile[] files;
}
