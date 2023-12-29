package com.stg.service;

import com.stg.service.dto.insurance.UploadFileRespDto;
import com.stg.service.dto.upload.FileSasUrlInfo;
import com.stg.service.dto.upload.UploadImageInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface UploadFileService {

    ResponseEntity<UploadImageInfo> uploadImage(MultipartFile parts) throws IOException;

    ResponseEntity downloadImageResource(String path) throws IOException;

    ResponseEntity<UploadImageInfo> uploadImageAzure(MultipartFile parts) throws IOException, ExecutionException, InterruptedException;

    UploadFileRespDto uploadMultiPathFileAzure(MultipartFile[] files);

    void downloadFileDoiSoat(String date, HttpServletResponse response) throws IOException;

    FileSasUrlInfo generateSasUrl(String fileName);

}
