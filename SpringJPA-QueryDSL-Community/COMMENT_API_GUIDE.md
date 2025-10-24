# Comment API 가이드

## 📋 목차
1. [API 엔드포인트](#api-엔드포인트)
2. [데이터 모델](#데이터-모델)
3. [DTO 구조](#dto-구조)
4. [비즈니스 로직](#비즈니스-로직)
5. [레포지토리](#레포지토리)
6. [예외 처리](#예외-처리)

---

## API 엔드포인트

### Base URL
```
/posts/{postId}/comments
```

### 1. 댓글 목록 조회
**GET** `/posts/{postId}/comments`

#### Request
- **Path Parameter**: `postId` (Integer) - 게시글 ID
- **Headers**: 인증 불필요

#### Response
```json
{
  "code": "get_comments_success",
  "data": {
    "comments": [
      {
        "id": 1,
        "author": {
          "id": 1,
          "nickname": "사용자1",
          "email": "user@example.com",
          "profileImageUrl": "https://..."
        },
        "content": "댓글 내용",
        "deleted": false,
        "publishedAt": "2024-01-01T12:00:00",
        "updatedAt": "2024-01-01T12:00:00"
      }
    ],
    "pagination": {
      "total_count": 10
    }
  }
}
```

#### 특징
- 삭제되지 않은 댓글만 조회 (`deleted = false`)
- `publishedAt` 오름차순 정렬
- 게시글이 존재하지 않으면 404 에러 (post_not_found)

---

### 2. 댓글 생성
**POST** `/posts/{postId}/comments`

#### Request
- **Path Parameter**: `postId` (Integer) - 게시글 ID
- **Headers**: 
  - `X-USER-ID` (Integer, Required) - 사용자 ID
- **Body**:
```json
{
  "content": "댓글 내용"
}
```

#### Validation
- `content`: 필수 (NotBlank), 최대 1000자

#### Response
```json
{
  "code": "create_comment_success",
  "data": {
    "commentId": 1,
    "content": "댓글 내용"
  }
}
```

#### Status Code
- `201 Created` - 성공
- `404 Not Found` - 게시글이 없음
- `401 Unauthorized` - 사용자 인증 실패

#### 부가 효과
- 게시글의 `commentCount` 증가

---

### 3. 댓글 수정
**PATCH** `/posts/{postId}/comments/{commentId}`

#### Request
- **Path Parameters**: 
  - `postId` (Integer) - 게시글 ID
  - `commentId` (Integer) - 댓글 ID
- **Headers**: 
  - `X-USER-ID` (Integer, Required) - 사용자 ID
- **Body**:
```json
{
  "content": "수정된 댓글 내용"
}
```

#### Validation
- `content`: 필수 (NotBlank), 최대 1000자

#### Response
```json
{
  "code": "update_comment_success",
  "data": {
    "commentId": 1,
    "content": "수정된 댓글 내용"
  }
}
```

#### Status Code
- `200 OK` - 성공
- `404 Not Found` - 댓글이 없음
- `403 Forbidden` - 작성자가 아님 (not_authorized)

#### 특징
- 작성자만 수정 가능 (권한 검증)
- `updatedAt` 자동 갱신

---

### 4. 댓글 삭제 (Soft Delete)
**PUT** `/posts/{postId}/comments/{commentId}`

#### Request
- **Path Parameters**: 
  - `postId` (Integer) - 게시글 ID
  - `commentId` (Integer) - 댓글 ID
- **Headers**: 
  - `X-USER-ID` (Integer, Required) - 사용자 ID

#### Response
- **Status Code**: `204 No Content`

#### Status Code
- `204 No Content` - 성공
- `404 Not Found` - 댓글이 없음
- `403 Forbidden` - 작성자가 아님 (not_authorized)

#### 특징
- Soft Delete: 실제로 삭제하지 않고 `deleted` 플래그를 `true`로 변경
- 이미 삭제된 댓글은 다시 삭제해도 카운트 변경 없음
- 게시글의 `commentCount` 감소

#### 부가 효과
- `deleted` = `true`
- 게시글의 `commentCount` 감소 (최소값 0)

---

## 데이터 모델

### Comment Entity

```java
@Entity
@Table(name = "comments",
       indexes = { @Index(name="idx_comments_post_pub", columnList = "post_id,published_at") })
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id", columnDefinition="int unsigned")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name="fk_comments_user"))
    private User author;

    @JsonIgnore  // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="post_id", foreignKey = @ForeignKey(name="fk_comments_post"))
    private Post post;

    @Column(name="content", length=1000, nullable=false)
    private String content;

    @Column(name="is_deleted", nullable=false)
    private boolean deleted = false;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @Column(name="published_at", nullable=false)
    private LocalDateTime publishedAt;

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
}
```

### 필드 설명
| 필드 | 타입 | 설명 | 제약조건 |
|------|------|------|----------|
| `id` | Integer | 댓글 ID (PK) | Auto Increment, unsigned |
| `author` | User | 작성자 (FK) | ManyToOne, Lazy Loading |
| `post` | Post | 게시글 (FK) | ManyToOne, Lazy Loading, JsonIgnore |
| `content` | String | 댓글 내용 | 최대 1000자, NotNull |
| `deleted` | boolean | 삭제 여부 | 기본값 false |
| `deletedAt` | LocalDateTime | 삭제 일시 | Nullable |
| `publishedAt` | LocalDateTime | 작성 일시 | NotNull, Auto |
| `updatedAt` | LocalDateTime | 수정 일시 | NotNull, Auto |

### 인덱스
- `idx_comments_post_pub`: (post_id, published_at) - 게시글별 댓글 조회 최적화

---

## DTO 구조

### CreateCommentRequest
```java
public record CreateCommentRequest(
    @NotBlank 
    @Size(max=1000) 
    String content
) { }
```

### UpdateCommentRequest
```java
public record UpdateCommentRequest(
    @NotBlank 
    @Size(max=1000) 
    String content
) { }
```

---

## 비즈니스 로직

### CommentService

#### 1. list(Integer postId)
```java
public List<Comment> list(Integer postId)
```
- 게시글 존재 여부 확인
- 활성 댓글 목록 반환 (deleted = false)
- publishedAt 오름차순 정렬

#### 2. create(Integer userId, Integer postId, CreateCommentRequest req)
```java
@Transactional
public Comment create(Integer userId, Integer postId, CreateCommentRequest req)
```
- 게시글 존재 여부 확인
- 사용자 존재 여부 확인
- 댓글 생성
- **게시글 commentCount 증가**
- 트랜잭션으로 일관성 보장

#### 3. update(Integer userId, Integer commentId, UpdateCommentRequest req)
```java
public Comment update(Integer userId, Integer commentId, UpdateCommentRequest req)
```
- 댓글 존재 여부 확인
- 작성자 권한 확인 (작성자만 수정 가능)
- 댓글 내용 수정
- updatedAt 자동 갱신 (@PreUpdate)

#### 4. softDelete(Integer userId, Integer commentId)
```java
@Transactional
public void softDelete(Integer userId, Integer commentId)
```
- 댓글 존재 여부 확인
- 작성자 권한 확인 (작성자만 삭제 가능)
- deleted 플래그를 true로 변경
- **게시글 commentCount 감소** (최소값 0)
- 이미 삭제된 댓글은 카운트 변경 없음
- 트랜잭션으로 일관성 보장

---

## 레포지토리

### CommentRepository
```java
public interface CommentRepository 
    extends JpaRepository<Comment, Integer>, CommentRepositoryCustom { }
```

### CommentRepositoryCustom
```java
public interface CommentRepositoryCustom {
    List<Comment> findActiveByPostId(Integer postId);
}
```

### CommentRepositoryImpl (QueryDSL)
```java
@Override
public List<Comment> findActiveByPostId(Integer postId) {
    QComment c = QComment.comment;
    QPost p = QPost.post;
    return queryFactory
        .selectFrom(c)
        .join(c.post, p)
        .where(p.id.eq(postId).and(c.deleted.isFalse()))
        .orderBy(c.publishedAt.asc(), c.id.asc())
        .fetch();
}
```

#### 특징
- QueryDSL 사용으로 타입 안전성 보장
- 명시적 JOIN으로 N+1 문제 방지
- 삭제되지 않은 댓글만 조회
- publishedAt 오름차순, 같은 시간이면 id 오름차순

---

## 예외 처리

### 발생 가능한 예외

| 예외 | ErrorCode | 메시지 | 상황 |
|------|-----------|--------|------|
| ApiException | RESOURCE_NOT_FOUND | post_not_found | 게시글이 존재하지 않음 |
| ApiException | RESOURCE_NOT_FOUND | comment_not_found | 댓글이 존재하지 않음 |
| ApiException | UNAUTHORIZED | token_not_valid | 사용자 인증 실패 |
| ApiException | FORBIDDEN | not_authorized | 작성자가 아님 (수정/삭제 권한 없음) |

---

## 프론트엔드 연동 가이드

### 1. 댓글 목록 조회
```javascript
const res = await apiFetch(`/posts/${postId}/comments`);
const comments = res.data.comments;

comments.forEach(comment => {
  console.log(comment.author.nickname);  // 작성자
  console.log(comment.content);          // 내용
  console.log(comment.publishedAt);      // 작성일 (ISO 8601 형식)
});
```

### 2. 댓글 생성
```javascript
await apiFetch(`/posts/${postId}/comments`, {
  method: 'POST',
  body: JSON.stringify({ content: '댓글 내용' })
});
```

### 3. 댓글 수정
```javascript
await apiFetch(`/posts/${postId}/comments/${commentId}`, {
  method: 'PATCH',
  body: JSON.stringify({ content: '수정된 내용' })
});
```

### 4. 댓글 삭제
```javascript
await apiFetch(`/posts/${postId}/comments/${commentId}`, {
  method: 'PUT'
});
```

---

## 주의사항

### 1. JSON 직렬화
- Comment의 `post` 필드는 `@JsonIgnore` 처리되어 응답에 포함되지 않음
- 순환 참조 방지 (Comment → Post → Images → Post...)

### 2. Lazy Loading
- `author`와 `post`는 Lazy Loading
- 조회 시 N+1 문제 주의 (QueryDSL에서 JOIN으로 해결)

### 3. 트랜잭션
- 생성/삭제 시 게시글의 commentCount도 함께 변경
- `@Transactional`로 일관성 보장

### 4. Soft Delete
- DELETE 메서드가 아닌 PUT 메서드 사용
- 실제 데이터는 삭제되지 않고 `deleted` 플래그만 변경
- 삭제된 댓글은 조회되지 않음

### 5. 권한 검증
- 수정/삭제는 작성자만 가능
- `X-USER-ID` 헤더의 userId와 author.id 비교

---

## 테스트 시나리오

### 1. 정상 플로우
1. 게시글 생성
2. 댓글 생성 (commentCount 증가)
3. 댓글 목록 조회
4. 댓글 수정 (작성자)
5. 댓글 삭제 (작성자, commentCount 감소)

### 2. 예외 케이스
- 존재하지 않는 게시글에 댓글 생성 시도
- 존재하지 않는 댓글 수정/삭제 시도
- 다른 사용자의 댓글 수정/삭제 시도
- 이미 삭제된 댓글 재삭제 시도

---

## 개선 가능한 사항

### 1. 페이징
현재는 전체 댓글을 한 번에 반환합니다. 댓글이 많은 경우 페이징 처리 필요:
```java
Page<Comment> findActiveByPostId(Integer postId, Pageable pageable);
```

### 2. 대댓글 (답글)
Comment에 `parentCommentId` 추가로 계층 구조 지원 가능

### 3. 좋아요 기능
CommentLike 엔티티 추가로 댓글 좋아요 기능 구현 가능

### 4. 신고 기능
CommentReport 엔티티 추가로 부적절한 댓글 신고 기능 구현 가능

### 5. 실시간 알림
댓글 생성 시 게시글 작성자에게 알림 전송

---

## 데이터베이스 스키마

```sql
CREATE TABLE comments (
    comment_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    post_id INT UNSIGNED NOT NULL,
    content VARCHAR(1000) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME NULL,
    published_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(post_id),
    
    INDEX idx_comments_post_pub (post_id, published_at)
);
```

---

## 버전 히스토리

- **v1.0** (2024-10-22)
  - Comment CRUD API 구현
  - Soft Delete 적용
  - QueryDSL을 활용한 동적 쿼리
  - JSON 순환 참조 방지 (@JsonIgnore)

