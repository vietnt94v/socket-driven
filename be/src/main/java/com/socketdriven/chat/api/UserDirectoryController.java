package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.UserSummaryDto;
import com.socketdriven.chat.domain.User;
import com.socketdriven.chat.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserDirectoryController {

  private final UserRepository userRepository;

  public UserDirectoryController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<UserSummaryDto> list() {
    return userRepository.findAll().stream().map(UserDirectoryController::toDto).toList();
  }

  private static UserSummaryDto toDto(User u) {
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
