#!/bin/bash

set -x

USER=bahmni
GROUP=bahmni

id -g ${GROUP} 2>/dev/null
if [ $? -eq 1 ]; then
    groupadd ${GROUP}
fi

id ${USER} 2>/dev/null
if [ $? -eq 1 ]; then
    useradd -g ${USER} ${USER}
fi

usermod -s /usr/sbin/nologin bahmni

mkdir -p /opt/dhis-integration/var/log/
mkdir /etc/dhis-integration/
mkdir /var/log/dhis-integration/

chown -R bahmni:bahmni /opt/dhis-integration/
chmod +x /opt/dhis-integration/bin/dhis-integration

mv /opt/dhis-integration/bin/dhis-integration*.jar /opt/dhis-integration/bin/dhis-integration.jar

ln -sf /opt/dhis-integration/etc/application.yml /etc/dhis-integration/dhis-integration.yml
ln -sf /opt/dhis-integration/var/log/dhis-integration.log /var/log/dhis-integration/dhis-integration.log
ln -sf /opt/dhis-integration/bin/dhis-integration /etc/init.d/dhis-integration

chkconfig --add dhis-integration