package com.socketdriven.chat.service;

import com.socketdriven.chat.api.dto.PatchUserRequest;
import com.socketdriven.chat.api.dto.UploadUrlResponse;
import com.socketdriven.chat.api.dto.UserSummaryDto;
import com.socketdriven.chat.domain.User;
import com.socketdriven.chat.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;

  public UserService(UserRepository userRepository, FileStorageService fileStorageService) {
    this.userRepository = userRepository;
    this.fileStorageService = fileStorageService;
  }

  @Transactional(readOnly = true)
  public List<UserSummaryDto> list(String q) {
    List<User> users =
        (q == null || q.isBlank())
            ? userRepository.findAll(Sort.by(Sort.Order.asc("username")))
            : userRepository.searchByQuery(q.trim());
    return users.stream().map(UserService::toSummary).toList();
  }

  @Transactional(readOnly = true)
  public List<UserSummaryDto> searchExcludingUser(UUID excludeUserId, String q) {
    if (q == null || q.isBlank()) {
      return List.of();
    }
    return userRepository.searchByQuery(q.trim()).stream()
        .filter(u -> !u.getId().equals(excludeUserId))
        .map(UserService::toSummary)
        .toList();
  }

  @Transactional(readOnly = true)
  public UserSummaryDto getById(UUID id) {
    User u = userRepository.findById(id).orElseThrow();
    return toSummary(u);
  }

  @Transactional
  public UserSummaryDto updateMe(UUID userId, PatchUserRequest req) {
    User u = userRepository.findById(userId).orElseThrow();
    if (req.displayName() != null && !req.displayName().isBlank()) {
      u.setDisplayName(req.displayName().trim());
    }
    if (req.email() != null && !req.email().isBlank()) {
      String em = req.email().trim();
      if (!em.equalsIgnoreCase(u.getEmail()) && userRepository.existsByEmail(em)) {
        throw new IllegalArgumentException("Email already taken");
      }
      u.setEmail(em);
    }
    u.setUpdatedAt(Instant.now());
    userRepository.save(u);
    return toSummary(u);
  }

  @Transactional
  public UploadUrlResponse uploadAvatar(UUID userId, MultipartFile file) {
    User u = userRepository.findById(userId).orElseThrow();
    String url = fileStorageService.saveAndPublicUrl("avatars", file);
    u.setAvatarUrl(url);
    u.setUpdatedAt(Instant.now());
    userRepository.save(u);
    return new UploadUrlResponse(url);
  }

  private static UserSummaryDto toSummary(User u) {
    String dn = Optional.ofNullable(u.getDisplayName()).orElse(u.getUsername());
    String[] parts = dn.split(" ", 2);
    return new UserSummaryDto(
        u.getId(),
        u.getUsername(),
        Optional.ofNullable(u.getDisplayName()).orElse(""),
        parts[0],
        parts.length > 1 ? parts[1] : "",
        Optional.ofNullable(u.getAvatarUrl()).orElse(""));
  }
}
