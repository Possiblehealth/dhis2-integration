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

mysql --user="root" --password="P@ssw0rd" --database="openmrs" --execute="CREATE table dhis2_log ( 
																		id INT(6) unsigned auto_increment primary key, 
																		report_name varchar(100) not null, 
																		submitted_date timestamp, 
																		submitted_by varchar(30) not null, 
																		report_log varchar(4000) not null, 
																		status varchar(30) not null,
																		comment varchar(30) not null, 
																		report_month integer, 
																		report_year integer);"


usermod -s /usr/sbin/nologin bahmni

mkdir -p /opt/dhis-integration/var/log/
mkdir /etc/dhis-integration/
mkdir /var/log/dhis-integration/
mkdir /dhis-integration/
mkdir /dhis-integration-data/
mkdir /var/www/bahmni_config/dhis2/

chown -R bahmni:bahmni /opt/dhis-integration/
chown -R bahmni:bahmni /dhis-integration/
chown -R bahmni:bahmni /dhis-integration-data/
chown -R bahmni:bahmni /var/www/bahmni_config/dhis2/ 
chmod +x /opt/dhis-integration/bin/dhis-integration

mv /opt/dhis-integration/bin/dhis-integration*.jar /opt/dhis-integration/bin/dhis-integration.jar

ln -sf /opt/dhis-integration/etc/application.yml /etc/dhis-integration/dhis-integration.yml
ln -sf /opt/dhis-integration/etc/log4j.properties /etc/dhis-integration/log4j.properties
ln -sf /opt/dhis-integration/var/log/dhis-integration.log /var/log/dhis-integration/dhis-integration.log
ln -sf /opt/dhis-integration/bin/dhis-integration /etc/init.d/dhis-integration



chkconfig --add dhis-integration