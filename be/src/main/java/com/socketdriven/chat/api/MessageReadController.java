package com.socketdriven.chat.api;

import com.socketdriven.chat.service.MessageService;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageReadController {

  private final MessageService messageService;

  public MessageReadController(MessageService messageService) {
    this.messageService = messageService;
  }

  @PostMapping("/{id}/read")
  public void markRead(@PathVariable UUID id) {
    messageService.markRead(id, CurrentUserId.get());
  }
}
