# 백엔드 배포 가이드

이 레포지토리는 GitHub Actions를 통해 자동으로 EC2에 배포됩니다.

## 🚀 자동 배포

`main` 브랜치에 푸시하면 자동으로 다음이 배포됩니다:
1. **docker-compose.yml** → Private EC2
2. **nginx 설정** → Public EC2
3. **백엔드 코드** → Private EC2

```bash
git add .
git commit -m "Update backend"
git push origin main  # 자동 배포
```

## ⚙️ GitHub Secrets 설정

레포지토리 Settings > Secrets and variables > Actions에서 다음 시크릿을 추가하세요:

- `PUBLIC_EC2_IP`: Public EC2의 Public IP 주소
- `PRIVATE_EC2_IP`: Private EC2의 Private IP 주소
- `SSH_PRIVATE_KEY`: EC2 접속용 SSH 개인 키 (PEM 파일 전체 내용)

## 📋 포함된 파일

이 레포지토리에는 다음 파일들이 포함되어 있습니다:

```
3-caleb-son-community-BE/
├── .github/workflows/deploy.yml    # 자동 배포 워크플로우
├── docker-compose.yml               # Private EC2용 Docker Compose
├── nginx/                           # Public EC2용 Nginx 설정
│   ├── nginx.conf
│   └── docker-compose.yml
├── src/                             # 백엔드 소스 코드
├── Dockerfile
└── DEPLOYMENT.md                    # 이 파일
```

## 🔄 배포 프로세스

GitHub Actions가 자동으로 다음 작업을 수행합니다:

### 1단계: 인프라 배포
- `docker-compose.yml`을 Private EC2의 `~/community-project/`에 배포
- `nginx` 설정을 Public EC2의 `~/nginx-proxy/`에 배포
- Nginx 자동 재시작 (Private EC2 IP 자동 설정)

### 2단계: 백엔드 배포
- 백엔드 코드를 Private EC2의 `~/community-project/3-caleb-son-community-BE/`에 배포
- Docker 이미지 빌드
- 백엔드 컨테이너 재시작

## 📊 배포 확인

```bash
# GitHub Actions 탭에서 배포 진행 상황 확인

# 또는 EC2에서 직접 확인
ssh -i your-key.pem -J ubuntu@<PUBLIC_EC2_IP> ubuntu@<PRIVATE_EC2_IP>
cd ~/community-project
docker-compose ps backend

# 로그 확인
docker-compose logs -f backend

# API 테스트
curl http://<PUBLIC_EC2_IP>/api/health
```

## 🔧 수동 배포

로컬에서 직접 배포하려면:

```bash
# 1. 백엔드 파일 압축
tar -czf backend.tar.gz \
    Dockerfile \
    build.gradle.kts \
    settings.gradle.kts \
    gradle/ \
    src/ \
    --exclude='.git' \
    --exclude='build' \
    --exclude='bin'

# 2. Private EC2로 전송
scp -i your-key.pem -o ProxyJump=ubuntu@<PUBLIC_EC2_IP> \
    backend.tar.gz ubuntu@<PRIVATE_EC2_IP>:~/community-project/3-caleb-son-community-BE/

# 3. EC2에서 배포
ssh -i your-key.pem -J ubuntu@<PUBLIC_EC2_IP> ubuntu@<PRIVATE_EC2_IP>
cd ~/community-project/3-caleb-son-community-BE
tar -xzf backend.tar.gz
rm backend.tar.gz
cd ~/community-project
docker-compose stop backend
docker-compose rm -f backend
docker-compose build backend
docker-compose up -d backend
```

## 🔍 문제 해결

### GitHub Actions가 실행되지 않는 경우

1. `.gitignore`에서 워크플로우 파일이 차단되지 않았는지 확인
2. `.github/workflows/deploy.yml` 파일이 Git에 커밋되었는지 확인
3. GitHub Secrets가 올바르게 설정되었는지 확인

### 배포 실패 시

1. GitHub Actions 로그 확인
2. SSH 연결 테스트
3. EC2의 docker-compose.yml 파일 확인
4. 보안 그룹 설정 확인

## 💡 중요 사항

- **docker-compose.yml**과 **nginx** 설정은 이 레포지토리에 포함되어 있으며, 푸시 시 자동으로 배포됩니다
- **프론트엔드 레포**에서도 동일한 파일들이 배포되므로, 두 레포 중 하나만 푸시해도 인프라가 업데이트됩니다
- Nginx 설정의 Private EC2 IP는 GitHub Actions가 자동으로 설정합니다
