#!/bin/bash
read -p "Version: " VERSION
mvn clean package
rm -rf /Users/dave/workspace/aiur/warpgate/src/main/maven/io/aiur/oss/spring-data-jdbc
mvn deploy:deploy-file \
    -DgroupId=io.aiur.oss \
    -DartifactId=spring-data-jdbc \
    -Dversion=$VERSION-SNAPSHOT \
    -Dfile=target/spring-data-jdbc-0.1.0-SNAPSHOT.jar \
    -Dsources=target/spring-data-jdbc-0.1.0-SNAPSHOT-sources.jar \
    -Dpackaging=jar \
    -Durl=file:///Users/dave/workspace/aiur/warpgate/src/main/maven



rm -rf /Users/dave/workspace/aiur/platform/parent/src/main/maven/io/aiur/oss/spring-data-jdbc
mvn deploy:deploy-file \
    -DgroupId=io.aiur.oss \
    -DartifactId=spring-data-jdbc \
    -Dversion=$VERSION-SNAPSHOT \
    -Dfile=target/spring-data-jdbc-0.1.0-SNAPSHOT.jar \
    -Dsources=target/spring-data-jdbc-0.1.0-SNAPSHOT-sources.jar \
    -Dpackaging=jar \
    -Durl=file:///Users/dave/workspace/aiur/platform/parent/src/main/maven