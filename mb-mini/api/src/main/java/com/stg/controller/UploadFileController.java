package com.stg.controller;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.dto.insurance.UploadFileRespDto;
import com.stg.service.dto.upload.FileSasUrlInfo;
import com.stg.utils.Endpoints;
import com.stg.service.UploadFileService;
import com.stg.service.dto.upload.UploadImageInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "Upload file Apis")
public class UploadFileController {

    private final UploadFileService uploadFileService;

    @GetMapping(Endpoints.URL_DOWNLOAD_IMAGE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity downloadImageResource(@RequestParam(name = "path", required = false) String path) throws IOException {
        return uploadFileService.downloadImageResource(path);
    }

    @PostMapping(Endpoints.URL_UPLOAD_IMAGE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadImageInfo> uploadImageAzure(@RequestParam("image") MultipartFile parts) throws IOException, ExecutionException, InterruptedException {
        return uploadFileService.uploadImageAzure(parts);
    }
    @PostMapping(Endpoints.MINI_APP_UPLOAD_FILE)
    @ResponseStatus(HttpStatus.OK)
    public UploadFileRespDto uploadMultiPathFileAzure(@RequestParam("file") MultipartFile[] files) {
        return uploadFileService.uploadMultiPathFileAzure(files);
    }

    @GetMapping(Endpoints.URL_DOWNLOAD_BAAS)
    @ResponseStatus(HttpStatus.OK)
    public void downloadFileDoiSoat(@RequestParam(name = "date") String date,
                                    HttpServletResponse response) throws IOException {
        uploadFileService.downloadFileDoiSoat(date, response);
    }

    @GetMapping(Endpoints.URL_SAS_URL_DOCUMENT)
    @ResponseStatus(HttpStatus.OK)
    public FileSasUrlInfo getFileSasUrl(@AuthenticationPrincipal CustomUserDetails user,
                                        @RequestParam(name = "fileName") String fileName) {
        return uploadFileService.generateSasUrl(fileName);
    }
}
