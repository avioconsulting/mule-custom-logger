#!/bin/sh

GROUP_ID=com.avioconsulting.mule

sed "s/<groupId>$GROUP_ID<\/groupId>/<groupId>$1<\/groupId>/" pom.xml > anypoint-pom.xml
./mvnw deploy -DskipTests -f anypoint-pom.xml -P exchange -Dtoken=$2
STATUS=$?
rm -f anypoint-pom.xml
if [ $STATUS -ne 0 ]; then
    echo "Error while executing maven."
    exit 1
else
    echo "Maven executed successfully"
fi