# === Build Stage ===
FROM gradle:8.12-jdk17 AS build
WORKDIR /app

# 의존성 캐싱을 위해 빌드 설정 파일만 먼저 복사
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 후 빌드 (테스트 제외 - CI에서 별도 수행)
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# === Runtime Stage ===
FROM eclipse-temurin:17-jre
WORKDIR /app

# 보안: root가 아닌 별도 사용자로 실행
RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown appuser:appuser app.jar
USER appuser

EXPOSE 8080

# JVM 메모리 설정은 환경변수로 주입 (OCI ARM 인스턴스 공유 환경 고려)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
