#!/bin/bash

set -x

service dhis-integration stop || true

rm -rf /opt/dhis-integration/
rm -f /etc/init.d/dhis-integration
rm -rf /etc/dhis-integration/
rm -rf /var/log/dhis-integration/