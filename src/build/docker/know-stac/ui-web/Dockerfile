#
#
#

FROM tomcat:11-jdk17

ENV KNOW_STAC_UI_VERSION=LATEST
ENV LOG_LEVEL=error

RUN mkdir -p $CATALINA_HOME/wars

# Copy KNOW_STAC WAR
COPY target/know-stac-ui.war $CATALINA_HOME/wars/know-stac-ui.war

# Deploy CGR War
RUN mkdir -p $CATALINA_HOME/wars/know-stac-ui && cd $CATALINA_HOME/wars/know-stac-ui && $JAVA_HOME/bin/jar xf $CATALINA_HOME/wars/know-stac-ui.war
RUN mv $CATALINA_HOME/wars/know-stac-ui $CATALINA_HOME/webapps/ROOT

# Log4j properties
RUN wget -nv -O $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml https://raw.githubusercontent.com/terraframe/geoprism-cloud/dev/ansible/roles/webserver/files/log4j2.xml
RUN sed -i -e "s/<Root level=\"error\">/<Root level=\"$LOG_LEVEL\">/g" $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml

#CMD ["$CATALINA_HOME/bin/catalina.sh", "run"]
