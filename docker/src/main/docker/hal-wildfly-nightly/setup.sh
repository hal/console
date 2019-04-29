#!/bin/sh

WF_ARTIFACT_VERSION=wildfly-$wildfly_version

function downloadSpecifiedOrLatestWildfly() {
    if [ -z "$wildfly_version" ]
    then
        echo "Version of Wildfly has not been specified, resolving latest one"
        curl https://ci.wildfly.org/guestAuth/repository/download/WF_Nightly/latest.lastFinished/teamcity-ivy.xml > /tmp/teamcity-ivy.xml
        WF_ARTIFACT_VERSION=`xmllint --xpath 'string(ivy-module/publications/artifact[not(contains(@name,"src"))]/@name)' /tmp/teamcity-ivy.xml`
        echo "Latest version of Wildfly is $WF_ARTIFACT_VERSION"
    fi
    curl -O https://ci.wildfly.org/guestAuth/repository/download/WF_Nightly/latest.lastFinished/${WF_ARTIFACT_VERSION}.zip
}

downloadSpecifiedOrLatestWildfly
unzip ${WF_ARTIFACT_VERSION}.zip
mv ${WF_ARTIFACT_VERSION} ${JBOSS_HOME}
rm ${WF_ARTIFACT_VERSION}.zip
chown -R jboss:0 ${JBOSS_HOME}
chmod -R g+rw ${JBOSS_HOME}
