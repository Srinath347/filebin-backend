package com.example.filebin.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.example.filebin.config.AppConfig;
import com.example.filebin.service.FileManagementService;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileManagementServiceImpl implements FileManagementService {

    private Logger logger = LoggerFactory.getLogger(FileManagementServiceImpl.class);

    @Autowired
    private AmazonS3 amazonS3Client;

    @Autowired
    private AppConfig appConfig;

    @Override
    public String uploadFile(@NonNull final String fileName, @NonNull final MultipartFile file) {

        File uploadFile = convertMultiPartFileToFile(fileName, file);
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + fileExtension;
        boolean response = uploadFileToS3bucket(uniqueFileName, uploadFile, appConfig.getBucketName());
        StringBuilder url = new StringBuilder("http://filebin-env-3.eba-2fmzkv9m.ap-south-1.elasticbeanstalk.com/download/");
        url.append(uniqueFileName);
        return response ? url.toString() : "";
    }

    private File convertMultiPartFileToFile(@NonNull final String fileName, @NonNull final MultipartFile file) {

        File convertedFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return convertedFile;
    }

    private boolean uploadFileToS3bucket(@NonNull final String fileName, @NonNull final File file,
                                         @NonNull final String bucketName) {

        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
        PutObjectResult response = amazonS3Client.putObject(request);
        return (response.getMetadata() != null);
    }

    @Override
    public byte[] downloadFile(@NonNull final String fileName) {

        String bucketName = appConfig.getBucketName();
        byte[] content = null;
        try {
            S3Object file = amazonS3Client.getObject(bucketName, fileName);
            deleteFileFromS3bucket(fileName, bucketName);
            final S3ObjectInputStream stream = file.getObjectContent();
            try {
                content = IOUtils.toByteArray(stream);
                file.close();
            } catch (IOException ex) {
               logger.warn(ex.getMessage());
            }
        } catch (AmazonS3Exception exception) {
            logger.warn("file does not exist");
        }
        return content;
    }

    private void deleteFileFromS3bucket(@NonNull final String fileName, @NonNull final String bucketName) {
        logger.info("deleting the file : " + fileName);
        amazonS3Client.deleteObject(bucketName, fileName);
        logger.info("file deleted");
    }
}
