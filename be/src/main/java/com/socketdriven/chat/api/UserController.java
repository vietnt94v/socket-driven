package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.PatchUserRequest;
import com.socketdriven.chat.api.dto.UploadUrlResponse;
import com.socketdriven.chat.api.dto.UserSummaryDto;
import com.socketdriven.chat.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public List<UserSummaryDto> list(@RequestParam(required = false) String q) {
    return userService.list(q);
  }

  @GetMapping("/{id}")
  public UserSummaryDto get(@PathVariable UUID id) {
    return userService.getById(id);
  }

  @PatchMapping("/me")
  public UserSummaryDto patchMe(@RequestBody PatchUserRequest request) {
    return userService.updateMe(CurrentUserId.get(), request);
  }

  @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
  public UploadUrlResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
    return userService.uploadAvatar(CurrentUserId.get(), file);
  }
}
