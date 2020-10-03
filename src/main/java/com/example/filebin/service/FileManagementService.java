package com.example.filebin.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileManagementService {

    String uploadFile(String fileName, MultipartFile multipartFiles);

    byte[] downloadFile(String fileName);
}
