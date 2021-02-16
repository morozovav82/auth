FROM frolvlad/alpine-java:jdk8.202.08-slim
COPY target/auth-1.1.jar /
CMD ["java", "-jar", "auth-1.1.jar"]
