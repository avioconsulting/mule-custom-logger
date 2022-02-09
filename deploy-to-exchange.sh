#!/bin/sh

sed "s/<groupId>com.avioconsulting.mule<\/groupId>/<groupId>$1<\/groupId>/" pom.xml > anypoint-pom.xml
./mvnw deploy -DskipTests -f anypoint-pom.xml -P exchange -Dtoken=$2
