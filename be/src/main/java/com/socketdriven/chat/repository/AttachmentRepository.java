package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.Attachment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {}
