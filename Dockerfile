FROM openjdk:17

WORKDIR /app
COPY . .

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

CMD ["sh", "-c", "java -jar target/*.jar"]