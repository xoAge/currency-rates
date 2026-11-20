# ---------- Этап 1: сборка ----------
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

# Сначала только то, что нужно для скачивания зависимостей, - этот слой кешируется
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Потом исходники - они меняются чаще всего
COPY src/ src/
RUN ./mvnw -B package -DskipTests

# ---------- Этап 2: запуск ----------
FROM eclipse-temurin:26-jre
WORKDIR /app

RUN useradd --system --no-create-home app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]