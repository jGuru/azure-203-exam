FROM java:8
WORKDIR /home
ENV JDBC_URL=url
ENV MYSQL_DB_USERNAME=uname
ENV MYSQL_DB_PASSWORD=password
ADD courseapi.jar courseapi.jar
EXPOSE 8080 3307
CMD java -version
CMD java -jar courseapi.jar
