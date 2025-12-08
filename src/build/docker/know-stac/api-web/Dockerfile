#
#
#

FROM tomcat:11-jdk17

ENV KNOW_STAC_VERSION=LATEST
ENV LOG_LEVEL=error
ENV POSTGRES_PORT=5433

RUN mkdir -p $CATALINA_HOME/wars

# Copy KNOW_STAC WAR
COPY target/know-stac.war $CATALINA_HOME/wars/know-stac.war

# Deploy CGR War
RUN mkdir -p $CATALINA_HOME/wars/know-stac && cd $CATALINA_HOME/wars/know-stac && $JAVA_HOME/bin/jar xf $CATALINA_HOME/wars/know-stac.war
RUN mv $CATALINA_HOME/wars/know-stac $CATALINA_HOME/webapps/ROOT

# Log4j properties
RUN wget -nv -O $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml https://raw.githubusercontent.com/terraframe/geoprism-cloud/dev/ansible/roles/webserver/files/log4j2.xml
RUN sed -i -e "s/<Root level=\"error\">/<Root level=\"$LOG_LEVEL\">/g" $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml

# Pre-load with a default appcfg
COPY target/appcfg $CATALINA_HOME/appcfg

RUN wget -nv -O $CATALINA_HOME/bin/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
RUN chmod +x $CATALINA_HOME/bin/wait-for-it.sh

#CMD ["$CATALINA_HOME/bin/catalina.sh", "run"]
