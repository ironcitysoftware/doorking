@echo off

git pull
call ant

set CLASSPATH=bin
set CLASSPATH=%CLASSPATH%;lib\protobuf-2.6.1.jar
set CLASSPATH=%CLASSPATH%;lib\google-api-client-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-api-client-gson-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-api-client-jackson2-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-api-services-sheets-v4-rev549-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-http-client-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-http-client-gson-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-http-client-jackson2-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-http-client-jdo-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-oauth-client-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-oauth-client-java6-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\google-oauth-client-jetty-1.25.0.jar
set CLASSPATH=%CLASSPATH%;lib\gson-2.1.jar
set CLASSPATH=%CLASSPATH%;lib\guava-21.0.jar
set CLASSPATH=%CLASSPATH%;lib\httpclient-4.5.5.jar
set CLASSPATH=%CLASSPATH%;lib\jackson-core-2.9.6.jar
set CLASSPATH=%CLASSPATH%;lib\jdo2-api-2.3-eb.jar
set CLASSPATH=%CLASSPATH%;lib\jetty-6.1.26.jar
set CLASSPATH=%CLASSPATH%;lib\jetty-util-6.1.26.jar
set CLASSPATH=%CLASSPATH%;lib\javax.servlet-api-4.0.1.jar

java -cp %CLASSPATH% doorking.Sync
pause
