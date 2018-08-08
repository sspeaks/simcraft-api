FROM tomcat:8.0-jre8

# Copy configurations
COPY /configs/tomcat-users.xml /usr/local/tomcat/conf/
COPY /configs/context.xml /usr/local/tomcat/webapps/manager/META-INF/

# Copy application
COPY /target/simcraft-api.war /usr/local/tomcat/webapps/simcraft-api.war

# Install dependencies for simc
RUN apt-get update \
    && apt-get install -qy git libssl-dev gcc make g++

# Build simc
RUN mkdir -p /root/simcraft \
    && mkdir -p /root/simcraft/code \
    && mkdir -p /root/simcraft/result \
    && cd /root/simcraft/code \
    && git clone https://github.com/simulationcraft/simc \
    && cd simc \
    && make BITS=64 OPENSSL=1 -C engine \
    && mv /root/simcraft/code/simc/engine/simc /root/simcraft/simc \
    && cd / \
    && cd / \
    && rm -fr /code \
    && apt-get remove -qy gcc g++ make \
    && apt-get clean -qy \
    && apt-get autoclean -qy \
    && apt-get autoremove -qy \
    && apt-get purge -qy \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* \
    && rm -rf /var/lib/{apt,dpkg,cache,log}

# Copy api key
COPY /apikey.txt /root/simcraft/apikey.txt