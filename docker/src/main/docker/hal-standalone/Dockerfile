FROM jboss/base-jdk:8
MAINTAINER Harald Pehl <hpehl@redhat.com>

COPY --chown=jboss /maven/hal-standalone.jar hal-standalone.jar

EXPOSE 9090

CMD ["java", "-jar", "hal-standalone.jar"]