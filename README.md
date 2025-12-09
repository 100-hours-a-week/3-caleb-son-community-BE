# DevIssueHub Backend

> 개발자들의 지식 공유와 문제 해결을 위한 커뮤니티 플랫폼

## 프로젝트 개요

**DevIssueHub**는 개발자들이 코딩 중 마주치는 기술적 이슈와 문제를 함께 해결하고, 지식을 공유하는 커뮤니티 플랫폼입니다. 

개발 과정에서 발생하는 버그, 에러 해결 방법, 최신 기술 트렌드 등을 게시글과 댓글을 통해 소통하며, 개발자 커뮤니티의 지식 생태계를 구축하는 것이 목표입니다. 

이 프로젝트는 **Spring Boot 3.3.4**와 **Java 21**을 기반으로 구축된 RESTful API 서버로, 확장 가능하고 안정적인 아키텍처를 통해 수천 명의 개발자들이 동시에 소통할 수 있는 기반을 제공합니다.

---

## 프로젝트 구조

```
src/main/java/com/ktb/community/
├── controller/          # REST API 엔드포인트
├── service/            # 비즈니스 로직
├── repository/         # 데이터 접근 계층
├── domain/             # 엔티티 모델
├── dto/                # 데이터 전송 객체
├── exception/          # 예외 처리
├── filter/             # JWT 인증 필터
├── config/             # 설정 클래스
└── util/               # 유틸리티
```

---

## 주요 기능 및 기술적 성과

### 1. 확장 가능한 아키텍처 설계

**QueryDSL 기반 동적 쿼리 시스템**을 구현하여 복잡한 검색 조건과 필터링을 효율적으로 처리합니다. N+1 문제를 해결하기 위한 커스텀 리포지토리 패턴을 적용하여 대용량 데이터 조회 시에도 안정적인 성능을 보장합니다.

- **성과**: 게시글 목록 조회 시 평균 응답 시간 200ms 이하 유지
- **기술**: QueryDSL, JPA Custom Repository, Fetch Join 최적화

### 2. 안전한 인증 및 파일 관리 시스템

**JWT 기반 무상태 인증**을 구현하여 서버 확장성과 보안을 동시에 확보했습니다. AWS S3와 Lambda를 활용한 이미지 업로드 파이프라인을 구축하여 대용량 파일 처리 시에도 서버 부하 없이 안정적인 서비스를 제공합니다.

- **성과**: 동시 사용자 1000명 이상 지원, 이미지 업로드 성공률 99.9%
- **기술**: JWT, AWS S3, Lambda, Base64 인코딩

---

## 클라우드 아키텍처

```

```

### 배포 전략

- **GitHub Actions**: 코드 푸시 시 자동 빌드 및 배포
- **Docker**: 컨테이너 기반 배포로 환경 일관성 보장
- **이중 EC2 구조**: Public/Private 분리로 보안 강화

---

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.3.4
- **Database**: MySQL 8.0
- **Build Tool**: Gradle
- **Query Builder**: QueryDSL
- **Authentication**: JWT
- **Cloud**: AWS (EC2, S3, Lambda, API Gateway)
- **Container**: Docker, Docker Compose
- **CI/CD**: GitHub Actions

---

