package com.socketdriven.chat.api;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserId {

  private CurrentUserId() {}

  public static UUID get() {
    return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
  }
}
