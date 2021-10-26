FROM jboss/wildfly:latest

LABEL maintainer="Harald Pehl <hpehl@redhat.com>"

COPY --chown=jboss /module.xml $JBOSS_HOME/modules/system/layers/base/org/jboss/as/console/main/
COPY --chown=jboss /maven/hal-console.jar $JBOSS_HOME/modules/system/layers/base/org/jboss/as/console/main/

RUN sed \
    -e 's/<http-interface http-authentication-factory="management-http-authentication">/<http-interface>/' \
    -e 's/<http-upgrade enabled="true" sasl-authentication-factory="management-sasl-authentication"\/>/<http-upgrade enabled="true"\/>/' \
    $JBOSS_HOME/standalone/configuration/standalone.xml > $JBOSS_HOME/standalone/configuration/standalone-insecure.xml \
    && sed \
    -e 's/<http-interface http-authentication-factory="management-http-authentication">/<http-interface>/' \
    -e 's/<http-upgrade enabled="true" sasl-authentication-factory="management-sasl-authentication"\/>/<http-upgrade enabled="true"\/>/' \
    $JBOSS_HOME/standalone/configuration/standalone-full.xml > $JBOSS_HOME/standalone/configuration/standalone-full-insecure.xml \
    && sed \
    -e 's/<http-interface http-authentication-factory="management-http-authentication">/<http-interface>/' \
    -e 's/<http-upgrade enabled="true" sasl-authentication-factory="management-sasl-authentication"\/>/<http-upgrade enabled="true"\/>/' \
    $JBOSS_HOME/standalone/configuration/standalone-ha.xml > $JBOSS_HOME/standalone/configuration/standalone-ha-insecure.xml \
    && sed \
    -e 's/<http-interface http-authentication-factory="management-http-authentication">/<http-interface>/' \
    -e 's/<http-upgrade enabled="true" sasl-authentication-factory="management-sasl-authentication"\/>/<http-upgrade enabled="true"\/>/' \
    $JBOSS_HOME/standalone/configuration/standalone-full-ha.xml > $JBOSS_HOME/standalone/configuration/standalone-full-ha-insecure.xml \
    && $JBOSS_HOME/bin/add-user.sh -u admin -p admin --silent

EXPOSE 9090

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
