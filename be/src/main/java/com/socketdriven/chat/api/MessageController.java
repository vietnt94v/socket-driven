package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.MessageDto;
import com.socketdriven.chat.api.dto.PatchMessageRequest;
import com.socketdriven.chat.service.MessageService;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @PostMapping("/{id}/read")
  public void markRead(@PathVariable UUID id) {
    messageService.markRead(id, CurrentUserId.get());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    messageService.softDeleteMessage(id, CurrentUserId.get());
  }

  @PatchMapping("/{id}")
  public MessageDto patch(@PathVariable UUID id, @RequestBody PatchMessageRequest request) {
    return messageService.patchMessage(id, CurrentUserId.get(), request);
  }
}
