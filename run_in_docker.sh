mvn clean
mvn install
mvn package
docker stop monitor-transcoding
docker rm monitor-transcoding
docker build -t monitor-transcoding:1.0 .
docker run --network host -d --name monitor-transcoding -p 1399:1399  monitor-transcoding:1.0