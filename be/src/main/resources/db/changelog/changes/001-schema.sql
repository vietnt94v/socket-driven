--liquibase formatted sql

--changeset socketdriven:001-create-users
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(100),
  avatar_url VARCHAR(500),
  status VARCHAR(20) CHECK (status IS NULL OR status IN ('online', 'offline', 'away')),
  last_seen_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

--changeset socketdriven:002-create-conversations
CREATE TABLE conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type VARCHAR(10) NOT NULL CHECK (type IN ('DIRECT', 'GROUP')),
  name VARCHAR(100),
  avatar_url VARCHAR(500),
  created_by UUID NOT NULL REFERENCES users (id),
  last_message_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conversations_last_message ON conversations (last_message_at DESC NULLS LAST);

--changeset socketdriven:003-create-conversation-members
CREATE TABLE conversation_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users (id),
  role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MEMBER')),
  joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  left_at TIMESTAMPTZ,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  added_by UUID REFERENCES users (id),
  removed_by UUID REFERENCES users (id),
  UNIQUE (conversation_id, user_id)
);

CREATE INDEX idx_conv_members_user ON conversation_members (user_id, is_active);

--changeset socketdriven:004-create-messages
CREATE TABLE messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
  sender_id UUID NOT NULL REFERENCES users (id),
  type VARCHAR(20) NOT NULL CHECK (type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM')),
  content TEXT,
  reply_to_id UUID REFERENCES messages (id),
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation_created ON messages (conversation_id, created_at DESC);

--changeset socketdriven:005-create-message-status
CREATE TABLE message_status (
  message_id UUID NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users (id),
  delivered_at TIMESTAMPTZ,
  read_at TIMESTAMPTZ,
  PRIMARY KEY (message_id, user_id)
);

CREATE INDEX idx_message_status_message ON message_status (message_id);

--changeset socketdriven:006-create-attachments
CREATE TABLE attachments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  message_id UUID NOT NULL REFERENCES messages (id) ON DELETE CASCADE,
  file_url VARCHAR(1000) NOT NULL,
  file_name VARCHAR(255),
  mime_type VARCHAR(100),
  size_bytes BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

--changeset socketdriven:007-create-user-sessions
CREATE TABLE user_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  socket_id VARCHAR(100) NOT NULL,
  device_info VARCHAR(200),
  connected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  disconnected_at TIMESTAMPTZ,
  is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_sessions_user_active ON user_sessions (user_id, is_active);
