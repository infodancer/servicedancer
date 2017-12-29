FROM openjdk:8-jre
MAINTAINER Matthew Hunter <matthew@infodancer.org>

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/infodancer/servicelistener.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/lib           /usr/share/infodancer/lib
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/infodancer/servicelistener.jar