package com.stg.service.impl;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.sas.SasProtocol;
import com.stg.errors.ApplicationException;
import com.stg.errors.MiniApiException;
import com.stg.errors.UploadFileException;
import com.stg.service.UploadFileService;
import com.stg.service.dto.insurance.UploadFileRespDto;
import com.stg.service.dto.upload.FileSasUrlInfo;
import com.stg.service.dto.upload.UploadImageInfo;
import com.stg.utils.FileUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.stg.utils.Common.getExtensionFile;
import static com.stg.utils.CommonMessageError.MSG12;

@Service
@Transactional
@RequiredArgsConstructor
public class UploadFIleImpl implements UploadFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFIleImpl.class);
    private static final String FOLDER_IMAGE = "image";
    private static final long IMAGE_SIZE = (5 * 1024 * 1024); // 5MB
    private final BlobServiceClient blobServiceClient;

    @Value("${azure.storage.public_container}")
    private String publicContainer;
    @Value("${azure.storage.private_container}")
    private String privateContainer;
    @Value("${azure.storage.private_document_container}")
    private String privateDocumentContainer;

    @Autowired
    private UploadFileAsync uploadFileAsync;

    @Override
    public ResponseEntity<UploadImageInfo> uploadImage(MultipartFile parts) throws IOException {
        LOGGER.info("Uploading Image local with file name: {}, size: {}", parts.getOriginalFilename(), parts.getSize());
        if (parts.getSize() > IMAGE_SIZE) {
            throw new ApplicationException("Dung lượng ảnh vượt quá 5MB. Vui lòng chọn ảnh có dung lượng <=5MB");
        }
        String mediaType = FileUtil.getRealMimeType(parts);
        LOGGER.info("Media type = {}", mediaType);

        if (!(mediaType.equals(MediaType.IMAGE_PNG_VALUE) || mediaType.equals(MediaType.IMAGE_JPEG_VALUE))) {
            throw new ApplicationException("Định dạng ảnh không được chấp nhận. Vui lòng chọn định dạng ảnh png, jpg, jpeg");
        }

        String urlUpload = fileUpload(parts);
        urlUpload = urlUpload.replace("\\", "/").replace("//", "/");
        if (urlUpload.contains("image/")) {
            urlUpload = urlUpload.substring(urlUpload.lastIndexOf("/") + 1);
        }
        LOGGER.info("urlUpload = {}", urlUpload);
        UploadImageInfo info = new UploadImageInfo();
        info.setUrl(urlUpload);
        ResponseEntity<UploadImageInfo> responseEntity = new ResponseEntity<>(info, HttpStatus.OK);
        LOGGER.info("Uploaded Image with responseEntity={}", responseEntity);

        return responseEntity;
    }

    public String fileUpload(MultipartFile partFile) throws IOException {
        InputStream inputStream;
        String fileName = partFile.getOriginalFilename();
        String filePath = "";
        try {
            inputStream = partFile.getInputStream();

            String imagesUrl = getClass().getClassLoader().getResource(FOLDER_IMAGE).getFile();
            File dir = new File(imagesUrl);
            dir.mkdirs();
            File newFile = new File(dir, fileName);
            if (!newFile.exists()) {
                boolean file = newFile.createNewFile();
                LOGGER.debug("Create file value {}", file);
            }

            filePath = newFile.getAbsolutePath();
            LOGGER.info("File Path: " + filePath);

            try (OutputStream outputStream = new FileOutputStream(newFile)) {
                int read;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                throw new ApplicationException("Lỗi khi upload file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new ApplicationException("Lỗi khi upload file: " + e.getMessage());
        }

        return filePath;
    }

    @Override
    public ResponseEntity downloadImageResource(String path) throws IOException {
        LOGGER.debug("This directory: {}", new File("./").getAbsolutePath());

        String imagesUrl = getClass().getClassLoader().getResource(FOLDER_IMAGE).getFile();
        String imagePath = imagesUrl + File.separator + path;
        File file = new File(imagePath);
        InputStream ioStream = new FileInputStream(file);

        String fileName = path.substring(path.lastIndexOf("/") + 1);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        responseHeaders.add("File-Name", fileName);
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(ioStream));
    }

    @Override
    public ResponseEntity<UploadImageInfo> uploadImageAzure(MultipartFile parts) throws ExecutionException, InterruptedException {
        LOGGER.info("Uploading Image to Azure with file name: {}, size: {}", parts.getOriginalFilename(), parts.getSize());
        if (parts.getSize() > IMAGE_SIZE) {
            throw new ApplicationException("Dung lượng ảnh vượt quá 5MB. Vui lòng chọn ảnh có dung lượng <=5MB");
        }
        String mediaType = FileUtil.getRealMimeType(parts);
        LOGGER.debug("Media type = {}", mediaType);

        if (!(mediaType.equals(MediaType.IMAGE_PNG_VALUE) || mediaType.equals(MediaType.IMAGE_JPEG_VALUE))) {
            throw new ApplicationException("Định dạng ảnh không được chấp nhận. Vui lòng chọn định dạng ảnh png, jpg, jpeg");
        }

        CompletableFuture<String> urlUploadFileFuture = uploadFileAsync.uploadAndDownloadFile(parts, MediaType.IMAGE_JPEG_VALUE, getBlobContainerClient(publicContainer));
        CompletableFuture.allOf(urlUploadFileFuture).join();
        String urlUploadFile = urlUploadFileFuture.get();

        LOGGER.debug("urlUpload = {}", urlUploadFile);
        UploadImageInfo info = new UploadImageInfo();
        info.setUrl(urlUploadFile);
        ResponseEntity<UploadImageInfo> responseEntity = new ResponseEntity<>(info, HttpStatus.OK);
        LOGGER.debug("Uploaded Image with responseEntity={}", responseEntity);

        return responseEntity;
    }

    @Override
    public UploadFileRespDto uploadMultiPathFileAzure(MultipartFile[] files) {
        try {
            if (files.length > 15) {
                throw new UploadFileException("Số lượng file quý khách đã đạt mức tối đa cho phép (15 file). Vui lòng xoá file đã chọn trước đó nếu muốn tải lại ");
            }
            List<UploadImageInfo> uploadImageInfos = new ArrayList<>();

            List<CompletableFuture<String>> urlUploadFileFutures = new ArrayList<>();
            for (MultipartFile parts : files) {
                UploadImageInfo info = new UploadImageInfo();
                if (parts.getSize() > IMAGE_SIZE) {
                    info.setMessage("Chỉ được phép chọn file có dung lượng nhỏ hơn 5mb. Vui lòng chọn lại file.");
                    info.setCode(400);
                    info.setStatus(HttpStatus.BAD_REQUEST);
                    uploadImageInfos.add(info);
                    continue;
                }
                String mediaType = FileUtil.getRealMimeType(parts);

                if (!(mediaType.equals(MediaType.IMAGE_PNG_VALUE) || mediaType.equals(MediaType.IMAGE_JPEG_VALUE)
                        || mediaType.equals(MediaType.APPLICATION_PDF_VALUE))) {
                    info.setMessage("Chỉ được phép chọn file có định dạng: PDF, jpg, jpeg, png, live. Vui lòng chọn lại file.");
                    info.setCode(400);
                    info.setStatus(HttpStatus.BAD_REQUEST);
                    uploadImageInfos.add(info);
                } else {
                    if (MediaType.APPLICATION_PDF_VALUE.equals(mediaType) && !"pdf".equalsIgnoreCase(getExtensionFile(parts.getOriginalFilename()))) {
                        LOGGER.error("[MINI]--File bị thay đổi định dạng");
                        info.setMessage("File định dạng bị thay đổi.");
                        info.setCode(400);
                        info.setStatus(HttpStatus.BAD_REQUEST);
                        uploadImageInfos.add(info);
                        continue;
                    }
                    CompletableFuture<String> urlUploadFileFuture = uploadFileAsync.uploadAndDownloadFile(parts, mediaType, getBlobContainerClient(privateDocumentContainer));
                    urlUploadFileFutures.add(urlUploadFileFuture);
                }
            }

            CompletableFuture<String>[] arrUrlUploadFileFutures = new CompletableFuture[urlUploadFileFutures.size()];
            urlUploadFileFutures.toArray(arrUrlUploadFileFutures);
            CompletableFuture.allOf(arrUrlUploadFileFutures).join();
            for (CompletableFuture<String> completableFuture : urlUploadFileFutures) {
                UploadImageInfo info = new UploadImageInfo();
                info.setUrl(completableFuture.get());
                info.setMessage("Success");
                info.setCode(201);
                uploadImageInfos.add(info);
            }
            return new UploadFileRespDto().setUploadImageInfos(uploadImageInfos);
        } catch (InterruptedException | ExecutionException ie) {
            Thread.currentThread().interrupt();
            throw new MiniApiException(MSG12);
        }
    }

    public @NonNull BlobContainerClient getBlobContainerClient(@NonNull String containerName) {
        // create container if not exists
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }
        return blobContainerClient;
    }

    @Override
    public void downloadFileDoiSoat(String date, HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        BlobContainerClient containerClient = getBlobContainerClient(privateContainer);
        String fileName = "";
        for (BlobItem blobItem : containerClient.listBlobs()) {
            if (blobItem.getName().contains(date)) {
                BlockBlobClient blockBlobClient = containerClient.getBlobClient(blobItem.getName()).getBlockBlobClient();
                blockBlobClient.downloadStream(outputStream);
                fileName = blobItem.getName();
                break;
            }
        }
        String filePath = fileName;
        response.addHeader("file_name", filePath);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
    }

    @Override
    public FileSasUrlInfo generateSasUrl(String fileName) {
        BlobContainerSasPermission blobContainerSasPermission = new BlobContainerSasPermission()
                .setReadPermission(true)
                .setWritePermission(true)
                .setListPermission(true);
        BlobServiceSasSignatureValues builder = new BlobServiceSasSignatureValues(OffsetDateTime.now().plusMinutes(5), blobContainerSasPermission)
                .setProtocol(SasProtocol.HTTPS_ONLY);
        BlobContainerClient containerClient = getBlobContainerClient(privateDocumentContainer);
        return new FileSasUrlInfo(String.format("https://%s.blob.core.windows.net/" + privateDocumentContainer + "/%s?%s",
                containerClient.getAccountName(), fileName, containerClient.generateSas(builder)));
    }

}
