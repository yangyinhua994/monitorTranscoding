#!/bin/bash
#服务器ip
# shellcheck disable=SC2164

port=1698
project_name=monitorTranscoding
ip="10.1.20.102"
version="1.0.0-SNAPSHOT"
jar_dir='/home/yyh/jars'
container_suffix="-container"
jar_name="$project_name-${version}.jar"
container_name=${project_name}${container_suffix}


mvn clean
mvn install
mvn package
ssh root@${ip} <<EOF
mkdir -p ${jar_dir}/app
exit
EOF

scp -r target/${jar_name} root@${ip}:${jar_dir}/app
docker_compose="
version: '3.8'

services:
  $project_name:
    image: openjdk:17
    container_name: $container_name
    ports:
      - \"${port}:${port}\"
    working_dir: /app
    command: [java, -jar, ${jar_name}]
"

echo "$docker_compose" > docker-compose.yml
scp -r docker-compose.yml root@${ip}:${jar_dir}
#判断docker-compose是否安装
version=$(ssh -T root@${ip} << 'EOF'
  if command -v docker-compose &> /dev/null; then
    docker-compose -v | awk '{print $3}' | sed 's/,//g'
  else
    echo "Docker Compose is not installed."
  fi
EOF
)
if [[ "$version" == *"is not installed"* ]]; then
  docker_compose_installed=true
fi
if [ $docker_compose_installed = true ]; then
  scp -r ../docker-compose root@${ip}:/usr/local/bin/
  ssh root@${ip} <<EOF
  chmod +x /usr/local/bin/docker-compose
EOF
fi
ssh root@${ip} <<EOF
cd ${jar_dir}
docker stop ${container_name}
docker rm ${container_name}
docker-compose up -d
exit
EOF
