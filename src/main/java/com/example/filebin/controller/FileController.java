package com.example.filebin.controller;

import com.amazonaws.util.StringUtils;
import com.example.filebin.service.FileManagementService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

    private Logger logger = LoggerFactory.getLogger(FileController.class);

    private FileManagementService fileManagementService;

    FileController(FileManagementService fileManagementService) {
        this.fileManagementService = fileManagementService;
    }

    @CrossOrigin("*")
    @PostMapping(value = "/upload/{fileName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadFile(@PathVariable("fileName") final String fileName,
                                     @RequestPart(value = "file") final MultipartFile file) {

        logger.info("uploading file : " + fileName);
        String url = fileManagementService.uploadFile(fileName, file);
        JsonObject json = new JsonObject();
        json.add("link", new Gson().toJsonTree(url));
        if (StringUtils.isNullOrEmpty(url)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        logger.info("response : " + json.toString());
        return ResponseEntity.ok().body(json.toString());
    }

    @CrossOrigin("*")
    @GetMapping(value = "/download/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("fileName") final String fileName) {

        logger.info("downloading file : " + fileName);
        final byte[] data = fileManagementService.downloadFile(fileName);
        if(data == null) {
            return ResponseEntity.notFound().build();
        }
        final ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
