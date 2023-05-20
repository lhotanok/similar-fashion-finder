FROM maven:latest

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:resolve

COPY src/ ./src/

RUN mvn clean package

CMD ["java", "-cp", "target/backend-jar-with-dependencies.jar", "org.example.App"]
