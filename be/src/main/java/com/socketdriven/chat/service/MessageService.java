package com.socketdriven.chat.service;

import com.socketdriven.chat.api.dto.MessageDto;
import com.socketdriven.chat.api.dto.SendMessageRequest;
import com.socketdriven.chat.domain.Conversation;
import com.socketdriven.chat.domain.ConversationMember;
import com.socketdriven.chat.domain.Message;
import com.socketdriven.chat.domain.MessageStatus;
import com.socketdriven.chat.domain.MessageStatusId;
import com.socketdriven.chat.domain.MessageType;
import com.socketdriven.chat.domain.User;
import com.socketdriven.chat.repository.ConversationMemberRepository;
import com.socketdriven.chat.repository.ConversationRepository;
import com.socketdriven.chat.repository.MessageRepository;
import com.socketdriven.chat.repository.MessageStatusRepository;
import com.socketdriven.chat.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

  private final MessageRepository messageRepository;
  private final MessageStatusRepository messageStatusRepository;
  private final ConversationRepository conversationRepository;
  private final ConversationMemberRepository conversationMemberRepository;
  private final UserRepository userRepository;
  private final ChatBroadcastRegistry chatBroadcastRegistry;
  private final ObjectMapper objectMapper;

  public MessageService(
      MessageRepository messageRepository,
      MessageStatusRepository messageStatusRepository,
      ConversationRepository conversationRepository,
      ConversationMemberRepository conversationMemberRepository,
      UserRepository userRepository,
      ChatBroadcastRegistry chatBroadcastRegistry,
      ObjectMapper objectMapper) {
    this.messageRepository = messageRepository;
    this.messageStatusRepository = messageStatusRepository;
    this.conversationRepository = conversationRepository;
    this.conversationMemberRepository = conversationMemberRepository;
    this.userRepository = userRepository;
    this.chatBroadcastRegistry = chatBroadcastRegistry;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public Page<MessageDto> listMessages(UUID conversationId, UUID readerId, Pageable pageable) {
    ensureActiveMember(conversationId, readerId);
    return messageRepository
        .findByConversation_IdAndDeletedIsFalseOrderByCreatedAtDesc(conversationId, pageable)
        .map(MessageService::toDto);
  }

  @Transactional
  public MessageDto sendText(UUID conversationId, UUID senderId, SendMessageRequest req) {
    ensureActiveMember(conversationId, senderId);
    Conversation conv = conversationRepository.findById(conversationId).orElseThrow();
    Message m = new Message();
    m.setConversation(conv);
    m.setSender(userRepository.getReferenceById(senderId));
    m.setType(MessageType.TEXT);
    m.setContent(req.content());
    if (req.replyToId() != null) {
      Message reply = messageRepository.findById(req.replyToId()).orElseThrow();
      if (!reply.getConversation().getId().equals(conversationId)) {
        throw new IllegalArgumentException("reply in another conversation");
      }
      m.setReplyTo(reply);
    }
    messageRepository.save(m);
    conv.setLastMessageAt(m.getCreatedAt());
    conversationRepository.save(conv);

    List<ConversationMember> active =
        conversationMemberRepository.findByConversation_IdAndActiveTrue(conversationId);
    List<MessageStatus> statuses = new ArrayList<>();
    for (ConversationMember cm : active) {
      if (cm.getUser().getId().equals(senderId)) {
        continue;
      }
      MessageStatus ms = new MessageStatus();
      MessageStatusId id = new MessageStatusId();
      id.setMessageId(m.getId());
      id.setUserId(cm.getUser().getId());
      ms.setId(id);
      ms.setMessage(m);
      ms.setUser(cm.getUser());
      statuses.add(ms);
    }
    messageStatusRepository.saveAll(statuses);

    String payload = newMessagePayload(conversationId, m);
    List<UUID> recipientIds =
        statuses.stream().map(s -> s.getUser().getId()).toList();
    Set<UUID> delivered = chatBroadcastRegistry.sendToUsers(recipientIds, payload);
    for (MessageStatus ms : statuses) {
      if (delivered.contains(ms.getUser().getId())) {
        ms.setDeliveredAt(Instant.now());
      }
    }
    messageStatusRepository.saveAll(statuses);

    return toDto(m);
  }

  @Transactional
  public void markRead(UUID messageId, UUID readerId) {
    Message m = messageRepository.findById(messageId).orElseThrow();
    UUID cid = m.getConversation().getId();
    ensureActiveMember(cid, readerId);
    if (m.getSender().getId().equals(readerId)) {
      return;
    }
    MessageStatusId mid = new MessageStatusId();
    mid.setMessageId(messageId);
    mid.setUserId(readerId);
    MessageStatus s =
        messageStatusRepository.findById(mid).orElseThrow();
    s.setReadAt(Instant.now());
    messageStatusRepository.save(s);
  }

  @Transactional
  public void postSystemMessage(UUID conversationId, User actor, String content) {
    Conversation conv = conversationRepository.findById(conversationId).orElseThrow();
    Message m = new Message();
    m.setConversation(conv);
    m.setSender(actor);
    m.setType(MessageType.SYSTEM);
    m.setContent(content);
    messageRepository.save(m);
    conv.setLastMessageAt(m.getCreatedAt());
    conversationRepository.save(conv);
    String payload = newMessagePayload(conversationId, m);
    List<UUID> recipients =
        conversationMemberRepository.findByConversation_IdAndActiveTrue(conversationId).stream()
            .map(cm -> cm.getUser().getId())
            .toList();
    chatBroadcastRegistry.sendToUsers(recipients, payload);
  }

  private void ensureActiveMember(UUID conversationId, UUID userId) {
    conversationMemberRepository
        .findByConversation_IdAndUser_Id(conversationId, userId)
        .filter(ConversationMember::isActive)
        .orElseThrow(() -> new IllegalArgumentException("not a member"));
  }

  private String newMessagePayload(UUID conversationId, Message m) {
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("type", "NEW_MESSAGE");
      map.put("conversationId", conversationId.toString());
      map.put("message", toDto(m));
      return objectMapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static MessageDto toDto(Message m) {
    return new MessageDto(
        m.getId(),
        m.getConversation().getId(),
        m.getSender().getId(),
        m.getType().name(),
        m.getContent(),
        m.getReplyTo() != null ? m.getReplyTo().getId() : null,
        m.isDeleted(),
        m.getCreatedAt(),
        m.getUpdatedAt());
  }
}
