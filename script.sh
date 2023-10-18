#!/bin/sh
/Users/kcarron/Downloads/tomcat/bin/shutdown.sh
cd /Users/kcarron/Repositories/customAuthNode/
mvn clean install
rm -f /Users/kcarron/Downloads/tomcat/logs/*
rm /Users/kcarron/Downloads/tomcat/webapps/openam/WEB-INF/lib/customAuthNode-1.0.4-SNAPSHOT.jar
cp /Users/kcarron/Repositories/customAuthNode/target/customAuthNode-1.0.5-SNAPSHOT.jar /Users/kcarron/Downloads/tomcat/webapps/openam/WEB-INF/lib/
sleep 3
/Users/kcarron/Downloads/tomcat/bin/startup.sh

tail -f /Users/kcarron/Downloads/tomcat/logs/catalina.out