package vn.com.twendie.avis.api.core.util;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@Component
public class FileUtils {

    private final Random random = new Random();

    public List<String> saveWithTimestamp(Collection<MultipartFile> multipartFiles, String folderPath) throws IOException {
        List<String> fileNames = Lists.newArrayList();
        for (MultipartFile multipartFile : multipartFiles) {
            fileNames.add(saveWithTimestamp(multipartFile, folderPath));
        }
        return fileNames;
    }

    public String saveWithTimestamp(MultipartFile multipartFile, String folderPath) throws IOException {
        String fileName = System.currentTimeMillis() + "-" + random.nextInt(Integer.MAX_VALUE) + "-" +
                multipartFile.getOriginalFilename();
        return save(multipartFile, folderPath, fileName);
    }

    public String save(MultipartFile multipartFile, String folderPath, String fileName) throws IOException {
        Path filePath = Paths.get(createFolderIfNotExists(folderPath).toString(), fileName);
        multipartFile.transferTo(filePath);
        return filePath.toString();
    }

    public Path createFolderIfNotExists(String folderPath) throws IOException {
        Path path = Paths.get(folderPath);
        return Files.exists(path) ? path : Files.createDirectories(path);
    }

}
