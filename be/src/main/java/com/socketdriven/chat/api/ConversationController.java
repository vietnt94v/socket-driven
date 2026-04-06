package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.AddMemberRequest;
import com.socketdriven.chat.api.dto.ConversationDto;
import com.socketdriven.chat.api.dto.CreateDirectRequest;
import com.socketdriven.chat.api.dto.CreateGroupRequest;
import com.socketdriven.chat.api.dto.MessageDto;
import com.socketdriven.chat.api.dto.SendMessageRequest;
import com.socketdriven.chat.service.ConversationService;
import com.socketdriven.chat.service.MessageService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

  private final ConversationService conversationService;
  private final MessageService messageService;

  public ConversationController(
      ConversationService conversationService, MessageService messageService) {
    this.conversationService = conversationService;
    this.messageService = messageService;
  }

  @GetMapping
  public List<ConversationDto> list() {
    return conversationService.listForUser(CurrentUserId.get());
  }

  @PostMapping("/direct")
  public ConversationDto createDirect(@RequestBody CreateDirectRequest request) {
    return conversationService.findOrCreateDirect(CurrentUserId.get(), request.otherUserId());
  }

  @PostMapping("/group")
  public ConversationDto createGroup(@RequestBody CreateGroupRequest request) {
    return conversationService.createGroup(CurrentUserId.get(), request);
  }

  @PostMapping("/{id}/members")
  public void addMember(@PathVariable UUID id, @RequestBody AddMemberRequest request) {
    conversationService.addMember(id, CurrentUserId.get(), request.userId());
  }

  @DeleteMapping("/{id}/members/{userId}")
  public void removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
    conversationService.removeMember(id, CurrentUserId.get(), userId);
  }

  @GetMapping("/{id}/messages")
  public Page<MessageDto> messages(@PathVariable UUID id, Pageable pageable) {
    return messageService.listMessages(id, CurrentUserId.get(), pageable);
  }

  @PostMapping("/{id}/messages")
  public MessageDto sendMessage(@PathVariable UUID id, @RequestBody SendMessageRequest request) {
    return messageService.sendText(id, CurrentUserId.get(), request);
  }
}
