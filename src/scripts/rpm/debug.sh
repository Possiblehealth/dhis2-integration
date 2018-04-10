#!/bin/bash

DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,address=8030,server=y,suspend=n"

nohup java -jar ${DEBUG_OPTS} /opt/dhis-integration/bin/dhis-integration.jar \
 --spring.config.location=/etc/dhis-integration/dhis-integration.yml \
 >> /var/log/dhis-integration/dhis-integration.log \
 2>&1 &