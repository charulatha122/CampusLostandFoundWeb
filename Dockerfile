
FROM tomcat:10.1-jre17-temurin

COPY CampusLostFoundWeb.war /usr/local/tomcat/webapps/CampusLostFoundWeb.war

EXPOSE 8080
