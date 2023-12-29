package com.stg.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {
    public static final List<String> OCR_ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png","image/jpg"
    );
    public static final List<String> OCR_ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpeg", "png","jpg"
    );

    public static String getRealMimeType(MultipartFile file) {
        AutoDetectParser parser = new AutoDetectParser();
        Detector detector = parser.getDetector();
        try {
            Metadata metadata = new Metadata();
            TikaInputStream stream = TikaInputStream.get(file.getInputStream());
            MediaType mediaType = detector.detect(stream, metadata);
            return mediaType.toString();
        } catch (IOException e) {
            return MimeTypes.OCTET_STREAM;
        }
    }
    public static String getFileExtension(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return null;
        }

        String fileName = file.getOriginalFilename();

        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        } else {
            return null;
        }
    }
}
