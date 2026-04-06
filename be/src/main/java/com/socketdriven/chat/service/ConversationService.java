package com.socketdriven.chat.service;

import com.socketdriven.chat.api.dto.ConversationDto;
import com.socketdriven.chat.api.dto.CreateConversationRequest;
import com.socketdriven.chat.api.dto.CreateGroupRequest;
import com.socketdriven.chat.api.dto.MemberDto;
import com.socketdriven.chat.api.dto.PatchConversationRequest;
import com.socketdriven.chat.api.dto.UpdateMemberRoleRequest;
import com.socketdriven.chat.domain.Conversation;
import com.socketdriven.chat.domain.ConversationMember;
import com.socketdriven.chat.domain.ConversationType;
import com.socketdriven.chat.domain.MemberRole;
import com.socketdriven.chat.domain.User;
import com.socketdriven.chat.repository.ConversationMemberRepository;
import com.socketdriven.chat.repository.ConversationRepository;
import com.socketdriven.chat.repository.UserRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConversationService {

  private final ConversationRepository conversationRepository;
  private final ConversationMemberRepository conversationMemberRepository;
  private final UserRepository userRepository;
  private final MessageService messageService;

  public ConversationService(
      ConversationRepository conversationRepository,
      ConversationMemberRepository conversationMemberRepository,
      UserRepository userRepository,
      MessageService messageService) {
    this.conversationRepository = conversationRepository;
    this.conversationMemberRepository = conversationMemberRepository;
    this.userRepository = userRepository;
    this.messageService = messageService;
  }

  @Transactional(readOnly = true)
  public List<ConversationDto> listForUser(UUID userId) {
    return conversationMemberRepository.findActiveForUserOrderByLastMessage(userId).stream()
        .map(cm -> toDto(cm.getConversation()))
        .toList();
  }

  @Transactional
  public ConversationDto createConversation(UUID me, CreateConversationRequest req) {
    if (req.type() == null || req.type().isBlank()) {
      throw new IllegalArgumentException("type required");
    }
    String t = req.type().trim().toUpperCase();
    if ("DIRECT".equals(t)) {
      if (req.otherUserId() == null) {
        throw new IllegalArgumentException("otherUserId required for DIRECT");
      }
      return findOrCreateDirect(me, req.otherUserId());
    }
    if ("GROUP".equals(t)) {
      if (req.name() == null || req.name().isBlank()) {
        throw new IllegalArgumentException("name required for GROUP");
      }
      if (req.memberIds() == null || req.memberIds().isEmpty()) {
        throw new IllegalArgumentException("memberIds required for GROUP");
      }
      return createGroup(me, new CreateGroupRequest(req.name(), req.memberIds()));
    }
    throw new IllegalArgumentException("invalid type");
  }

  @Transactional(readOnly = true)
  public ConversationDto getConversation(UUID conversationId, UUID userId) {
    ensureActiveMember(conversationId, userId);
    Conversation c = conversationRepository.findById(conversationId).orElseThrow();
    return toDto(c);
  }

  @Transactional
  public ConversationDto patchConversation(
      UUID conversationId, UUID actorId, PatchConversationRequest req) {
    Conversation c = conversationRepository.findById(conversationId).orElseThrow();
    if (c.getType() != ConversationType.GROUP) {
      throw new IllegalArgumentException("only group can be updated");
    }
    ConversationMember actor = requireAdmin(conversationId, actorId);
    if (req.name() != null && !req.name().isBlank()) {
      c.setName(req.name().trim());
    }
    if (req.avatarUrl() != null) {
      c.setAvatarUrl(req.avatarUrl().isBlank() ? null : req.avatarUrl().trim());
    }
    conversationRepository.save(c);
    return toDto(c);
  }

  @Transactional
  public void deleteGroupConversation(UUID conversationId, UUID actorId) {
    Conversation c = conversationRepository.findById(conversationId).orElseThrow();
    if (c.getType() != ConversationType.GROUP) {
      throw new IllegalArgumentException("only group can be deleted");
    }
    requireAdmin(conversationId, actorId);
    dissolve(conversationId);
  }

  @Transactional(readOnly = true)
  public List<MemberDto> listMembers(UUID conversationId, UUID viewerId) {
    ensureActiveMember(conversationId, viewerId);
    return conversationMemberRepository.findByConversation_IdAndActiveTrue(conversationId).stream()
        .map(ConversationService::toMemberDto)
        .toList();
  }

  @Transactional
  public void updateMemberRole(
      UUID conversationId,
      UUID actorId,
      UUID targetUserId,
      UpdateMemberRoleRequest req) {
    Conversation c = conversationRepository.findById(conversationId).orElseThrow();
    if (c.getType() != ConversationType.GROUP) {
      throw new IllegalArgumentException("only for group");
    }
    requireAdmin(conversationId, actorId);
    MemberRole newRole;
    try {
      newRole = MemberRole.valueOf(req.role().trim().toUpperCase());
    } catch (Exception e) {
      throw new IllegalArgumentException("invalid role");
    }
    ConversationMember target =
        conversationMemberRepository
            .findByConversation_IdAndUser_Id(conversationId, targetUserId)
            .orElseThrow();
    if (!target.isActive()) {
      throw new IllegalArgumentException("not an active member");
    }
    if (target.getRole() == MemberRole.ADMIN && newRole == MemberRole.MEMBER) {
      long admins =
          conversationMemberRepository.countByConversation_IdAndActiveTrueAndRole(
              conversationId, MemberRole.ADMIN);
      if (admins <= 1) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "cannot remove last admin");
      }
    }
    target.setRole(newRole);
    conversationMemberRepository.save(target);
  }

  @Transactional
  public void leaveConversation(UUID conversationId, UUID userId) {
    removeMember(conversationId, userId, userId);
  }

  @Transactional
  public ConversationDto findOrCreateDirect(UUID me, UUID otherUserId) {
    if (me.equals(otherUserId)) {
      throw new IllegalArgumentException("cannot chat with self");
    }
    userRepository.findById(otherUserId).orElseThrow();
    Optional<Conversation> existing =
        conversationRepository.findDirectBetween(me, otherUserId, ConversationType.DIRECT);
    if (existing.isPresent()) {
      return toDto(existing.get());
    }
    User meUser = userRepository.findById(me).orElseThrow();
    User other = userRepository.getReferenceById(otherUserId);
    Conversation c = new Conversation();
    c.setType(ConversationType.DIRECT);
    c.setCreatedBy(meUser);
    conversationRepository.save(c);
    ConversationMember m1 = new ConversationMember();
    m1.setConversation(c);
    m1.setUser(meUser);
    m1.setRole(MemberRole.MEMBER);
    m1.setAddedBy(meUser);
    ConversationMember m2 = new ConversationMember();
    m2.setConversation(c);
    m2.setUser(other);
    m2.setRole(MemberRole.MEMBER);
    m2.setAddedBy(meUser);
    conversationMemberRepository.save(m1);
    conversationMemberRepository.save(m2);
    return toDto(c);
  }

  @Transactional
  public ConversationDto createGroup(UUID creatorId, CreateGroupRequest req) {
    if (req.name() == null || req.name().isBlank()) {
      throw new IllegalArgumentException("group name required");
    }
    Set<UUID> all = new HashSet<>(req.memberIds());
    all.add(creatorId);
    if (all.size() < 3) {
      throw new IllegalArgumentException("group needs at least 3 members including creator");
    }
    for (UUID uid : all) {
      userRepository.findById(uid).orElseThrow();
    }
    User creator = userRepository.findById(creatorId).orElseThrow();
    Conversation c = new Conversation();
    c.setType(ConversationType.GROUP);
    c.setName(req.name().trim());
    c.setCreatedBy(creator);
    conversationRepository.save(c);
    for (UUID uid : all) {
      ConversationMember cm = new ConversationMember();
      cm.setConversation(c);
      cm.setUser(userRepository.getReferenceById(uid));
      cm.setRole(uid.equals(creatorId) ? MemberRole.ADMIN : MemberRole.MEMBER);
      cm.setAddedBy(creator);
      conversationMemberRepository.save(cm);
    }
    return toDto(c);
  }

  @Transactional
  public void removeMember(UUID conversationId, UUID actorId, UUID targetUserId) {
    Conversation c = conversationRepository.findById(conversationId).orElseThrow();
    if (c.getType() != ConversationType.GROUP) {
      throw new IllegalArgumentException("only for group");
    }
    ConversationMember actor =
        conversationMemberRepository
            .findByConversation_IdAndUser_Id(conversationId, actorId)
            .orElseThrow();
    ConversationMember target =
        conversationMemberRepository
            .findByConversation_IdAndUser_Id(conversationId, targetUserId)
            .orElseThrow();
    if (!target.isActive()) {
      return;
    }
    boolean self = actorId.equals(targetUserId);
    if (!self && actor.getRole() != MemberRole.ADMIN) {
      throw new IllegalArgumentException("forbidden");
    }
    if (target.getRole() == MemberRole.ADMIN) {
      List<ConversationMember> ranked =
          conversationMemberRepository.findActiveByConversationOrderByJoinedAt(conversationId);
      Optional<ConversationMember> successor =
          ranked.stream()
              .filter(m -> !m.getUser().getId().equals(targetUserId))
              .findFirst();
      successor.ifPresent(
          s -> {
            s.setRole(MemberRole.ADMIN);
            conversationMemberRepository.save(s);
          });
    }
    target.setActive(false);
    target.setLeftAt(Instant.now());
    target.setRemovedBy(actor.getUser());
    conversationMemberRepository.save(target);
    long remaining = conversationMemberRepository.countByConversation_IdAndActiveTrue(conversationId);
    if (remaining <= 1) {
      dissolve(conversationId);
    }
    String label =
        Optional.ofNullable(target.getUser().getDisplayName())
            .orElse(target.getUser().getUsername());
    messageService.postSystemMessage(conversationId, actor.getUser(), label + " left the group");
  }

  @Transactional
  public void addMember(UUID conversationId, UUID actorId, UUID newUserId) {
    Conversation c = conversationRepository.findById(conversationId).orElseThrow();
    if (c.getType() != ConversationType.GROUP) {
      throw new IllegalArgumentException("only for group");
    }
    ConversationMember actor =
        conversationMemberRepository
            .findByConversation_IdAndUser_Id(conversationId, actorId)
            .orElseThrow();
    if (actor.getRole() != MemberRole.ADMIN || !actor.isActive()) {
      throw new IllegalArgumentException("forbidden");
    }
    userRepository.findById(newUserId).orElseThrow();
    Optional<ConversationMember> existing =
        conversationMemberRepository.findByConversation_IdAndUser_Id(conversationId, newUserId);
    if (existing.isPresent()) {
      ConversationMember cm = existing.get();
      if (cm.isActive()) {
        throw new IllegalArgumentException("already member");
      }
      cm.setActive(true);
      cm.setLeftAt(null);
      cm.setJoinedAt(Instant.now());
      cm.setAddedBy(actor.getUser());
      cm.setRemovedBy(null);
      cm.setRole(MemberRole.MEMBER);
      conversationMemberRepository.save(cm);
    } else {
      ConversationMember cm = new ConversationMember();
      cm.setConversation(c);
      cm.setUser(userRepository.getReferenceById(newUserId));
      cm.setRole(MemberRole.MEMBER);
      cm.setAddedBy(actor.getUser());
      conversationMemberRepository.save(cm);
    }
    User nu = userRepository.findById(newUserId).orElseThrow();
    String label = Optional.ofNullable(nu.getDisplayName()).orElse(nu.getUsername());
    messageService.postSystemMessage(
        conversationId, actor.getUser(), label + " was added to the group");
  }

  private ConversationMember requireAdmin(UUID conversationId, UUID userId) {
    ConversationMember m =
        conversationMemberRepository
            .findByConversation_IdAndUser_Id(conversationId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    if (!m.isActive() || m.getRole() != MemberRole.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    return m;
  }

  private void ensureActiveMember(UUID conversationId, UUID userId) {
    conversationMemberRepository
        .findByConversation_IdAndUser_Id(conversationId, userId)
        .filter(ConversationMember::isActive)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
  }

  private void dissolve(UUID conversationId) {
    List<ConversationMember> active =
        conversationMemberRepository.findByConversation_IdAndActiveTrue(conversationId);
    for (ConversationMember cm : active) {
      cm.setActive(false);
      cm.setLeftAt(Instant.now());
      conversationMemberRepository.save(cm);
    }
  }

  private static MemberDto toMemberDto(ConversationMember cm) {
    User u = cm.getUser();
    return new MemberDto(
        u.getId(),
        u.getUsername(),
        Optional.ofNullable(u.getDisplayName()).orElse(""),
        cm.getRole().name(),
        cm.getJoinedAt());
  }

  private static ConversationDto toDto(Conversation c) {
    return new ConversationDto(
        c.getId(),
        c.getType().name(),
        c.getName(),
        c.getAvatarUrl(),
        c.getLastMessageAt());
  }
}
