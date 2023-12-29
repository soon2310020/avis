package com.stg.service.impl;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.stg.errors.UploadFileException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.stg.utils.Common.generateUUIDId;
import static com.stg.utils.Common.getExtensionFile;

@Service
//@Transactional
@RequiredArgsConstructor
public class UploadFileAsync {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileAsync.class);

    @Async
    public CompletableFuture<String> uploadAndDownloadFile(@NonNull MultipartFile file, String contentType, BlobContainerClient blobContainerClient) {
        String blobUrl;
        String filename = generateUUIDId(30) + "." + getExtensionFile(file.getOriginalFilename());
        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(filename).getBlockBlobClient();
        try {
            // delete file if already exists in that container
            if (Boolean.TRUE.equals(blockBlobClient.exists())) {
                blockBlobClient.delete();
            }

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            Map<String, String> metadata = Collections.singletonMap("metadata", "value");
            BlobRequestConditions requestConditions = new BlobRequestConditions();
            Context context = new Context("key", "value");
            Duration timeout = Duration.ofSeconds(60);
            blockBlobClient.uploadWithResponse(new BufferedInputStream(file.getInputStream()), file.getSize(),
                    headers, metadata, AccessTier.HOT, null, requestConditions, timeout, context);

            blobUrl = blockBlobClient.getBlobName();
        } catch (IOException e) {
            LOGGER.error("Error while processing file {}", e.getLocalizedMessage());
            throw new UploadFileException("Error while processing file");
        }
        return CompletableFuture.completedFuture(blobUrl);
    }
}
