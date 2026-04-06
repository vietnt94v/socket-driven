--liquibase formatted sql

--changeset socketdriven:008-create-refresh-token-sessions
CREATE TABLE refresh_token_sessions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  jti VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_refresh_sessions_user ON refresh_token_sessions (user_id);
