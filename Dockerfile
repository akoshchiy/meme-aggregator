FROM bellsoft/liberica-openjdk-alpine:11.0.5
RUN gradlew shadowJar
RUN cp -r config ./build/libs
WORKDIR ./build/libs
CMD ["java", "-jar", "*.jar"]