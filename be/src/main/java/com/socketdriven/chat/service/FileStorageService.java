package com.socketdriven.chat.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class FileStorageService {

  private final Path uploadRoot;

  public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
    this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
  }

  public String saveAndPublicUrl(String subdirectory, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("file required");
    }
    String original = file.getOriginalFilename();
    String ext = extensionOf(original);
    String stored = UUID.randomUUID() + ext;
    Path dir = uploadRoot.resolve(subdirectory).normalize();
    if (!dir.startsWith(uploadRoot)) {
      throw new IllegalStateException("invalid path");
    }
    try {
      Files.createDirectories(dir);
      Path target = dir.resolve(stored);
      file.transferTo(target.toFile());
    } catch (IOException e) {
      throw new IllegalStateException("failed to store file", e);
    }
    return ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/uploads/")
        .path(subdirectory)
        .path("/")
        .path(stored)
        .build()
        .toUriString();
  }

  private static String extensionOf(String original) {
    if (original == null || !original.contains(".")) {
      return "";
    }
    String ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    if (ext.length() > 12 || !ext.matches("\\.[a-z0-9]+")) {
      return "";
    }
    return ext;
  }
}
