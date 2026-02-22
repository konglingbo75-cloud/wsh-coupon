@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
java -jar d:\acewillqoder\wsh-backend\wsh-service\target\wsh-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
