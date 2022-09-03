#!/bin/bash

cd ../../../
sudo yum autoremove dhis-integration -y
mvn -Dmaven.test.skip=true install
sudo yum install target/rpm/dhis-integration/RPMS/noarch/dhis-integration-1.0-1.noarch.rpm -y
cd $HOME
sudo cp application.yml /opt/dhis-integration/etc
sudo systemctl restart dhis-integration