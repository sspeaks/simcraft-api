##############################################################
# Build WAR
##############################################################
FROM maven AS WAR
WORKDIR /root
COPY . .
RUN mvn clean package -DskipTests -f prod.pom.xml
##############################################################
# Build simc
##############################################################
FROM tomcat:8.0-jre8 AS SIMC
# Install dependencies for simc
RUN apt-get update \
    && apt-get install -qy git libssl-dev libcurl4-openssl-dev gcc make g++
# Build simc
RUN mkdir -p /root/simcraft \
    && mkdir -p /root/simcraft/code \
    && mkdir -p /root/simcraft/result \
    && cd /root/simcraft/code \
    && git clone https://github.com/simulationcraft/simc \
    && cd simc \
    && make BITS=64 OPENSSL=1 -C engine \
    && mv /root/simcraft/code/simc/engine/simc /root/simcraft/simc
##############################################################
# Build Tomcat
##############################################################

FROM tomcat:8.0-jre8

# Copy configurations
COPY /configs/tomcat-users.xml /usr/local/tomcat/conf/
COPY /configs/context.xml /usr/local/tomcat/webapps/manager/META-INF/

# Copy application
COPY --from=WAR /root/target/simcraft-api.war /usr/local/tomcat/webapps/simcraft-api.war
# Copy simc
COPY --from=SIMC /root/simcraft/simc /root/simcraft/simc
RUN mkdir -p /root/simcraft/result
# Copy api key for simc
COPY /apikey.txt /root/simcraft/apikey.txt