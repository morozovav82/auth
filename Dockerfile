FROM frolvlad/alpine-java:jdk8.202.08-slim
COPY target/auth-2.0.jar /
CMD ["java", "-jar", "auth-2.0.jar"]
