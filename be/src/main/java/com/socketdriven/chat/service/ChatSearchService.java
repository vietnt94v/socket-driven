package com.socketdriven.chat.service;

import com.socketdriven.chat.api.dto.ChatSearchResponse;
import com.socketdriven.chat.api.dto.GroupSearchHitDto;
import com.socketdriven.chat.api.dto.UserSummaryDto;
import com.socketdriven.chat.domain.Conversation;
import com.socketdriven.chat.domain.ConversationType;
import com.socketdriven.chat.repository.ConversationMemberRepository;
import com.socketdriven.chat.repository.ConversationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatSearchService {

  private final UserService userService;
  private final ConversationRepository conversationRepository;
  private final ConversationMemberRepository conversationMemberRepository;

  public ChatSearchService(
      UserService userService,
      ConversationRepository conversationRepository,
      ConversationMemberRepository conversationMemberRepository) {
    this.userService = userService;
    this.conversationRepository = conversationRepository;
    this.conversationMemberRepository = conversationMemberRepository;
  }

  @Transactional(readOnly = true)
  public ChatSearchResponse search(UUID me, String q) {
    if (q == null || q.isBlank()) {
      return new ChatSearchResponse(List.of(), List.of());
    }
    String trimmed = q.trim();
    List<UserSummaryDto> users = userService.searchExcludingUser(me, trimmed);
    List<Conversation> groups =
        conversationRepository.searchGroupsForMember(me, trimmed, ConversationType.GROUP);
    List<GroupSearchHitDto> groupHits =
        groups.stream()
            .map(
                c ->
                    new GroupSearchHitDto(
                        c.getId(),
                        Optional.ofNullable(c.getName()).orElse(""),
                        Optional.ofNullable(c.getAvatarUrl()).orElse(""),
                        (int)
                            conversationMemberRepository.countByConversation_IdAndActiveTrue(
                                c.getId())))
            .toList();
    return new ChatSearchResponse(users, groupHits);
  }
}
