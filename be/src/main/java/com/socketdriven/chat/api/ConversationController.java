package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.AddMemberRequest;
import com.socketdriven.chat.api.dto.ConversationDto;
import com.socketdriven.chat.api.dto.CreateConversationRequest;
import com.socketdriven.chat.api.dto.MemberDto;
import com.socketdriven.chat.api.dto.MessageDto;
import com.socketdriven.chat.api.dto.PatchConversationRequest;
import com.socketdriven.chat.api.dto.SendMessageRequest;
import com.socketdriven.chat.api.dto.UpdateMemberRoleRequest;
import com.socketdriven.chat.service.ConversationService;
import com.socketdriven.chat.service.MessageService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

  @PostMapping
  public ConversationDto create(@RequestBody CreateConversationRequest request) {
    return conversationService.createConversation(CurrentUserId.get(), request);
  }

  @GetMapping("/direct")
  public ConversationDto getDirect(@RequestParam("userId") UUID userId) {
    return conversationService
        .getDirectIfExists(CurrentUserId.get(), userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/{id}")
  public ConversationDto get(@PathVariable UUID id) {
    return conversationService.getConversation(id, CurrentUserId.get());
  }

  @PatchMapping("/{id}")
  public ConversationDto patch(
      @PathVariable UUID id, @RequestBody PatchConversationRequest request) {
    return conversationService.patchConversation(id, CurrentUserId.get(), request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    conversationService.deleteGroupConversation(id, CurrentUserId.get());
  }

  @GetMapping("/{id}/members")
  public List<MemberDto> listMembers(@PathVariable UUID id) {
    return conversationService.listMembers(id, CurrentUserId.get());
  }

  @PostMapping("/{id}/members")
  public void addMember(@PathVariable UUID id, @RequestBody AddMemberRequest request) {
    conversationService.addMember(id, CurrentUserId.get(), request.userId());
  }

  @PatchMapping("/{id}/members/{userId}/role")
  public void updateRole(
      @PathVariable UUID id,
      @PathVariable UUID userId,
      @RequestBody UpdateMemberRoleRequest request) {
    conversationService.updateMemberRole(id, CurrentUserId.get(), userId, request);
  }

  @DeleteMapping("/{id}/members/{userId}")
  public void removeMember(@PathVariable UUID id, @PathVariable String userId) {
    if ("me".equalsIgnoreCase(userId)) {
      conversationService.leaveConversation(id, CurrentUserId.get());
      return;
    }
    try {
      conversationService.removeMember(
          id, CurrentUserId.get(), UUID.fromString(userId));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
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
