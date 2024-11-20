FROM maven:3.8.4-openjdk-17-slim

RUN apt-get update && apt-get install -y tesseract-ocr

WORKDIR /PieceReader

COPY pom.xml pom.xml
RUN mvn dependency:go-offline

COPY . .

CMD ["mvn", "spring-boot:run"]