FROM openjdk:17

WORKDIR /app

COPY target/monitorTranscoding-1.0-SNAPSHOT.jar /app/

EXPOSE 1399

ENTRYPOINT ["java", "-jar", "monitorTranscoding-1.0-SNAPSHOT.jar"]

# docker stop monitor-transcoding
# docker rm monitor-transcoding
# docker build -t monitor-transcoding:1.0 .
# docker run --network host -d --name monitor-transcoding -p 1399:1399  monitor-transcoding:1.0
