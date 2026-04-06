package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.MessageStatus;
import com.socketdriven.chat.domain.MessageStatusId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageStatusRepository extends JpaRepository<MessageStatus, MessageStatusId> {}
