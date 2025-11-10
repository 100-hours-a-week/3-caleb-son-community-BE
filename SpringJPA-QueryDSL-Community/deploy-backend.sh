#!/bin/bash

# 백엔드 배포 스크립트
# 사용법: ./deploy-backend.sh <EC2-IP> <PEM-KEY-PATH>

if [ "$#" -ne 2 ]; then
    echo "사용법: ./deploy-backend.sh <EC2-IP> <PEM-KEY-PATH>"
    echo "예시: ./deploy-backend.sh 13.125.123.45 ~/my-key.pem"
    exit 1
fi

EC2_IP=$1
PEM_KEY=$2
JAR_NAME="spring-community-querydsl-final-0.0.1-SNAPSHOT.jar"

echo "🚀 백엔드 배포 시작..."

# 1. JAR 파일 빌드
echo "🔨 JAR 파일 빌드 중..."
./gradlew clean bootJar

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패!"
    exit 1
fi

# 2. EC2로 전송
echo "📤 EC2로 전송 중..."
scp -i "$PEM_KEY" build/libs/$JAR_NAME ubuntu@$EC2_IP:/home/ubuntu/backend/app.jar

# 3. application-prod.yml 전송 (있으면)
if [ -f "src/main/resources/application-prod.yml" ]; then
    echo "📤 프로덕션 설정 파일 전송 중..."
    scp -i "$PEM_KEY" src/main/resources/application-prod.yml ubuntu@$EC2_IP:/home/ubuntu/backend/
fi

# 4. EC2에서 배포 실행
echo "⚙️  EC2에서 백엔드 재시작 중..."
ssh -i "$PEM_KEY" ubuntu@$EC2_IP << 'ENDSSH'
    cd /home/ubuntu/backend
    
    # 기존 프로세스 중지
    pm2 delete backend 2>/dev/null || true
    
    # 새 프로세스 시작
    pm2 start "java -Xmx512m -Xms256m -Dspring.profiles.active=prod -jar app.jar" --name backend
    
    pm2 save
    
    echo "✅ 백엔드 배포 완료!"
    pm2 status
ENDSSH

echo "🎉 배포가 완료되었습니다!"
echo "📍 API 주소: http://$EC2_IP/api"
echo "📊 로그 확인: ssh -i $PEM_KEY ubuntu@$EC2_IP 'pm2 logs backend'"

