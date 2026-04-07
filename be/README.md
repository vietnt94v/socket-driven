# Backend REST API

Java Spring Boot service cho ứng dụng chat. Cổng mặc định: `8081` (có thể đổi bằng biến môi trường `SERVER_PORT`).

**Base URL (local):** `http://localhost:8081`

---

## Xác thực (Authentication)

- Các endpoint **không** nằm trong danh sách public cần header:

  `Authorization: Bearer <accessToken>`

- `accessToken` và `refreshToken` nhận được từ `POST /api/auth/login` hoặc `POST /api/auth/register`. Refresh: `POST /api/auth/refresh`.

**Public (không cần JWT):** `GET /api/health`, `GET /uploads/**`, toàn bộ `/api/auth/*`, và `WebSocket /ws/**`.

---

## 1. Health

| Method | Path           | Auth |
| ------ | -------------- | ---- |
| GET    | `/api/health`  | Không |

**Response:** `text/plain` — nội dung `ok`.

---

## 2. Auth — `/api/auth`

### POST `/api/auth/login`

**Request body (JSON):**

```json
{
  "username": "alice",
  "password": "secret123"
}
```

**Response (200):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "username": "alice",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Nguyen",
  "gender": "",
  "image": "https://example.com/avatar.png"
}
```

`id` là UUID của user (chuỗi). `firstName` / `lastName` được tách từ `displayName` nếu có.

### POST `/api/auth/register`

**Request body (JSON):**

```json
{
  "username": "bob",
  "email": "bob@example.com",
  "password": "secret123",
  "displayName": "Bob Tran"
}
```

**Response:** giống cấu trúc `LoginResponse` như `/login`.

### POST `/api/auth/refresh`

**Request body (JSON):**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

### POST `/api/auth/logout`

**Request body (JSON, tùy chọn):**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

- Nếu gửi kèm `Authorization: Bearer <accessToken>`: server dùng user từ token và có thể thu hồi session refresh.
- Nếu không có JWT: có thể chỉ gửi `refreshToken` trong body để logout theo refresh.

**Response:** thân rỗng (HTTP 200, không có body JSON).

---

## 3. Users — `/api/users`

Tất cả cần JWT trừ khi bạn đã mở thêm rule trong `SecurityConfig` (hiện tại: **cần auth**).

### GET `/api/users`

Query tùy chọn: `q` — nếu có và không rỗng thì tìm user theo repository `searchByQuery`; nếu không có `q` thì trả toàn bộ user, sắp xếp theo `username` tăng dần.

**Ví dụ:** `GET /api/users?q=ali`

**Response (200):** mảng `UserSummaryDto`

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "username": "alice",
    "displayName": "Alice",
    "firstName": "Alice",
    "lastName": "",
    "image": ""
  }
]
```

### GET `/api/users/{id}`

`id`: UUID user.

**Response (200):** một object giống phần tử trong mảng trên.

### PATCH `/api/users/me`

Cập nhật profile user đang đăng nhập.

**Request body (JSON):** chỉ cập nhật field được gửi và **không rỗng** (`null`, bỏ field, hoặc chuỗi rỗng → giữ nguyên giá trị cũ). Email trùng user khác sẽ lỗi.

```json
{
  "displayName": "Alice M.",
  "email": "new@example.com"
}
```

**Response (200):** `UserSummaryDto`.

### POST `/api/users/me/avatar`

`Content-Type: multipart/form-data`

- Part `file`: file ảnh upload.

**Response (200):**

```json
{
  "url": "https://.../public-url-to-file"
}
```

---

## 4. Conversations — `/api/conversations`

Cần JWT.

### GET `/api/conversations`

Danh sách hội thoại của user hiện tại.

**Response (200):**

```json
[
  {
    "id": "conv-uuid-here",
    "type": "DIRECT",
    "name": null,
    "avatarUrl": null,
    "lastMessageAt": "2026-04-07T10:15:30Z"
  }
]
```

`type`: `DIRECT` | `GROUP` (theo `ConversationType`).

### POST `/api/conversations`

Tạo hội thoại 1-1 hoặc nhóm.

**Direct (1-1):**

```json
{
  "type": "DIRECT",
  "otherUserId": "uuid-cua-user-kia",
  "name": null,
  "memberIds": null
}
```

**Group:**

```json
{
  "type": "GROUP",
  "otherUserId": null,
  "name": "Nhóm bạn thân",
  "memberIds": [
    "uuid-user-1",
    "uuid-user-2"
  ]
}
```

Nhóm yêu cầu **ít nhất 3 thành viên kể cả người tạo** (creator được thêm tự động); `memberIds` là các user khác (hoặc đủ để tổng ≥ 3 với creator).

**Response (200):** `ConversationDto`.

### GET `/api/conversations/{id}`

**Response (200):** `ConversationDto`.

### PATCH `/api/conversations/{id}`

**Request body (JSON):**

```json
{
  "name": "Tên nhóm mới",
  "avatarUrl": "https://cdn.example.com/group.png"
}
```

**Response (200):** `ConversationDto`.

### DELETE `/api/conversations/{id}`

Xóa hội thoại **nhóm** (theo logic `deleteGroupConversation`). **Response:** empty / 200.

### GET `/api/conversations/{id}/members`

**Response (200):**

```json
[
  {
    "userId": "uuid",
    "username": "alice",
    "displayName": "Alice",
    "role": "ADMIN",
    "joinedAt": "2026-04-01T08:00:00Z"
  }
]
```

`role`: `ADMIN` | `MEMBER`.

### POST `/api/conversations/{id}/members`

**Request body (JSON):**

```json
{
  "userId": "uuid-user-moi"
}
```

**Response:** 200, không body cố định (void).

### PATCH `/api/conversations/{id}/members/{userId}/role`

**Request body (JSON):**

```json
{
  "role": "ADMIN"
}
```

Giá trị hợp lệ: `ADMIN`, `MEMBER`.

### DELETE `/api/conversations/{id}/members/{userId}`

- `userId` = `me` (không phân biệt hoa thường): user hiện tại **rời** nhóm.
- Hoặc `userId` = UUID của thành viên (admin remove).

---

## 5. Messages trong conversation

Cần JWT. Nằm dưới prefix `/api/conversations/{conversationId}/messages`.

### GET `/api/conversations/{id}/messages`

Phân trang Spring Data (`Pageable`): query mặc định ví dụ `?page=0&size=20&sort=createdAt,desc`.

**Response (200):** JSON dạng `Page` (có `content`, `totalElements`, `totalPages`, `number`, `size`, …). Mỗi phần tử trong `content` là `MessageDto`:

```json
{
  "content": [
    {
      "id": "msg-uuid",
      "conversationId": "conv-uuid",
      "senderId": "user-uuid",
      "type": "TEXT",
      "content": "Xin chào!",
      "replyToId": null,
      "deleted": false,
      "createdAt": "2026-04-07T10:00:00Z",
      "updatedAt": "2026-04-07T10:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

`type` có thể là: `TEXT`, `IMAGE`, `FILE`, `SYSTEM`.

### POST `/api/conversations/{id}/messages`

**Request body (JSON):**

```json
{
  "content": "Nội dung tin nhắn",
  "replyToId": null
}
```

`replyToId` tùy chọn — UUID tin nhắn đang trả lời.

**Response (200):** `MessageDto`.

---

## 6. Messages — `/api/messages`

Thao tác trên một tin nhắn theo `id` tin nhắn. Cần JWT.

### POST `/api/messages/{id}/read`

Đánh dấu đã đọc. **Response:** void / 200.

### DELETE `/api/messages/{id}`

Xóa mềm tin nhắn (soft delete). **Response:** void / 200.

### PATCH `/api/messages/{id}`

**Request body (JSON):**

```json
{
  "content": "Nội dung đã chỉnh sửa"
}
```

**Response (200):** `MessageDto`.

---

## 7. Upload — `/api/upload`

Cần JWT.

### POST `/api/upload`

`Content-Type: multipart/form-data` — part tên `file`.

**Response (200):**

```json
{
  "url": "https://.../path/to/stored-file"
}
```

---

## Ghi chú nhanh

- UUID dùng định dạng chuẩn, ví dụ `f47ac10b-58cc-4372-a567-0e02b2c3d479`.
- Sai định dạng UUID trên path thường trả **400**.
- Không có hoặc JWT sai/thiếu quyền: **401** (theo cấu hình Spring Security).

---

## English summary

- **Auth:** Send `Authorization: Bearer <accessToken>` for all protected routes. Obtain tokens via `/api/auth/login`, `/api/auth/register`, or `/api/auth/refresh`.
- **Public:** `/api/health`, `/uploads/**`, `/api/auth/*`, `/ws/**`.
- **Main resources:** `/api/users`, `/api/conversations` (including nested messages), `/api/messages`, `/api/upload`.
- Request/response JSON field names match the Java records under `com.socketdriven.chat.api.dto`.
