package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.UploadUrlResponse;
import com.socketdriven.chat.service.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

  private final FileStorageService fileStorageService;

  public UploadController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @PostMapping(consumes = "multipart/form-data")
  public UploadUrlResponse upload(@RequestParam("file") MultipartFile file) {
    String url = fileStorageService.saveAndPublicUrl("files", file);
    return new UploadUrlResponse(url);
  }
}
