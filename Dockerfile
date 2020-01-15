FROM bellsoft/liberica-openjdk-alpine:11.0.5
RUN mkdir /build
ADD . /build
WORKDIR /build
RUN ./gradlew shadowJar
RUN mkdir /app
RUN cp -r config /app
RUN cp build/libs/*.jar /app
WORKDIR /app
CMD ["java", "-jar", "*.jar"]