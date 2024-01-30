package com.epam.controller;

import com.epam.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.epam.util.ValidateParam.*;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResource(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        if (isNotValid(multipartFile)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be in MP3 format");
        }
        Integer mp3FileId = resourceService.saveFile(multipartFile);
        return ResponseEntity.status(HttpStatus.OK).body(mp3FileId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(@PathVariable(value = "id") Integer id) {
        byte[] bytes = resourceService.getFileBytes(id);
        if (Objects.isNull(bytes)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(bytes);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable(value = "id") String ids) {
        if (isNotValid(ids)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("IDs length should be less than 200 characters");
        }
        List<Integer> deletedFileIds = resourceService.deleteFiles(ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedFileIds);
    }

}
