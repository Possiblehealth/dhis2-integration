#!/bin/bash

nohup java -jar /opt/dhis-integration/bin/dhis-integration.jar \
 --spring.config.location=/etc/dhis-integration/dhis-integration.yml \
 >> /var/log/dhis-integration/dhis-integration.log \
 2>&1 &