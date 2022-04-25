<h1>Implementation Guide</h1>
<h2>Install the DHIS2 Integration App</h2>
Assuming you have Bahmni installer latest version installed and running successfully. 
<ol>
<li>Update your distribution.<pre><code>sudo yum update</code></pre></li>
<li>Navigate to the home directory.<pre><code>cd /home</code></pre></li>
<li>Clone the binaries repo.<pre><code>git clone https://github.com/khobatha/bahmni-dhis2-integration-rpms.git</code></pre></li>
<li>Install the dhis integration app.
<ul>Navigate to the binaries folder<pre><code>cd bahmni-dhis2-integration-rpms</code></pre></ul>
<ul>Install the latest version of the dhis2 integration app<pre><code>sudo yum install dhis-integration-1.0-1_060721.noarch.rpm</code></pre></ul>
</li>
</ol>

<h2>Configure DHIS2 Integration: Security</h2>  
<ol>
<li>Download and place the ssl.conf file.
<pre><code>
cd /etc/httpd/conf.d/ <br>
wget https://raw.githubusercontent.com/Possiblehealth/possible-config/89662e8e823fac3dbcaf111aa72713a63139bb03/playbooks/roles/possible-dhis-integration/templates/dhis_integration_ssl.conf<br>
</code></pre>
</li>
<li>Navigate to the ssl.conf file and disable all configuration entries (SSLCertificateFile, SSLCertificateKey, SSLCertificateChainFile) containing hiels.org.
<pre><code>cd /etc/httpd/conf.d/ssl.conf<br></code></pre>
</li>  
<li>Update ssl.conf with new configuration entries for SSLCertificateFile and SSLCertificateKey.
<pre><code>SSLCertificateFile /etc/bahmni-certs/cert.crt</code></pre>
<pre><code>SSLCertificateKeyFile /etc/bahmni-certs/domain.key</code></pre>
</li>  
<li>Restart http service to reload the new ssl template configurations.
<pre><code>sudo systemctl restart httpd</code></pre>
<pre><code>sudo systemctl status httpd</code></pre>
</li>
<li>Use InstallCert to generate and install a self-signed ssl certificate for Bahmni - DHIS2 Integration App authentication.
<ul>Navigate to the home director<pre><code>cd /home</code></pre></ul>
<ul>Clone the InstallCert repo<pre><code>git clone https://github.com/escline/InstallCert.git</code></pre></ul>
<ul>Navigate to the folder<pre><code>cd InstallCert</code></pre></ul>
<ul>Compile InstallCert. NB: Follow <a href="https://mcvictech.blogspot.com/2017/07/how-to-install-java-8-jdkjre-8u131-on.html">this</a> link to install java 8 if jdk is missing on your server.<pre><code>javac InstallCert.java</code></pre></ul>  
</li> 
<ul>Once compiled successfully, run InstallCert to retrive an ssl certificate for the EMR (accept default certificate 1). NB: Note down the CN associated with the certificate for use later in configuration the Integration App running properties. In the example shown below, the CN associated with the generated self-signed certificate is f4120c18c732. <pre><code>java InstallCert localhost:443</code></pre></ul>  
<ul>Extract self-signed certificate from created jssecacerts keystore. NB: If keytool not accessible, use java alternatives command to reselect jre1.8 is your preferred java platform and rerun the keytool command.<pre><code>keytool -exportcert -alias localhost-1 -keystore jssecacerts -storepass changeit -file emr.cer</code></pre></ul> 
<ul>The next step is now to import the extracted certificate into the system keystore (cacerts). NB: You may use sudo find / -name cacerts to locate the exact path to your system keystore. <pre><code>keytool -importcert -alias localhost-1 -keystore /usr/java/jre1.8.0_131/lib/security/cacerts -storepass changeit -file emr.cer</code></pre></ul>
<ul>The last step is to use the automatically generated CN as an alias for the server's hostname. Open file /etc/hosts and append the CN to the right of localhost. <pre><code>sudo nano /etc/hosts</code></pre></ul>
</li> 
  
</ol> 

  
<h2>Install and configure DHIS2 integration app</h2>  
<ol>
<li>Update the properties file for DHIS2 integration app, located at <br>'/etc/dhis-integration/dhis-integration.yml', <br>with right configuration.
  
<table>
  <tr><th>Key</th><th>Description</th><th>Example</th></tr>
  <tr><td>openmrs.root.url</td><td>Url to access Openmrs service</td><td>http://localhost:8050/openmrs/ws/rest/v1</td></tr>
  <tr><td>bahmni.login.url</td><td>When user isn't logged in, then user is redirected to this url.</td><td>
https://ehr.possible.org/bahmni/home/#/login?showLoginMessage
</td></tr>
  <tr><td>reports.url</td><td>Bahmni reports url. Used for downloading reports.</td><td>https://ehr.possible.org/bahmnireports/report</td></tr>
  <tr><td>reports.json</td><td>This file contains configurations of DHIS2 reports.</td><td>/var/www/bahmni_config/openmrs/apps/reports/reports.json</td></tr>
  <tr><td>dhis.config.directory</td><td>This folder contains DHIS2 integration configurations program wise.</td><td>/var/www/bahmni_config/dhis2/</td></tr>
  <tr><td>dhis.url</td><td>The DHIS2 government server instance url.</td><td>Ex. 1: http://100.100.100.100:8080/</br>
Ex. 2: http://200.100.20.30:8888/hmistest/
Note that the url could be at domain or ip address level (ex1) or could be at a specific path(ex2)
</td></tr>
  <tr><td>dhis.user</td><td>The username to access DHIS2 instance.</td><td>username</td></tr>
  <tr><td>dhis.password</td><td>
The password for the DHIS2 user.
</td><td>password</td></tr>
  <tr><td>openmrs.db.url</td><td>Mysql connection url to access "openmrs" database. Set valid user and password in the url.</td><td>jdbc:mysql://localhost/openmrs?user=openmrs-user&password=password</td></tr>
  <tr><td>submission.audit.folder</td><td>All DHIS2 submissions are stored in this directory. Ensure the directory exists and "bahmni" user has access to it, or configure a different directory.</td><td>/dhis-integration-data</td></tr>
  <tr><td>server.port</td><td>Server config. Port for server to listen to.</td><td>8040</td></tr>
  <tr><td>server.context-path</td><td>Server config. Mapping incoming requests.</td><td>/dhis-integration/</td></tr>
  <tr><td>log4j.config.file</td><td>Server config. Properties file for logger of dhis-integration server.</td><td>log4j.properties</td></tr>
 </table>
</li>
</ol>


  
<ol>  
<li>Ensure Bahmni reports service is installed and running successfully.
 <pre><code>
  service bahmni-reports status ##should be running
 </code></pre>
 </li>
<li>Configure Bahmni landing page to show DHIS2 integration app.<br>
Insert the following in <br>"/var/www/bahmni_config/openmrs/apps/home/extension.json" file
<pre>
  <code>
"possible_dhis_2_integration": {
    "id": "possible.dhis2Integration",
    "extensionPointId": "org.bahmni.home.dashboard",
    "type": "link",
   "label": "DHIS2 integration",
   "url": "/dhis-integration/index.html",
   "icon": "fa-book",
   "order": 11,
   "requiredPrivilege": "app:reports"
}
 </code></pre>
</li>
<li>Ensure Bahmni reports service is installed and running successfully.
<pre><code>service bahmni-reports status ##should be running
</code></pre>
</li>
<li>Restart ssl and dhis-integration services.
</li>
<li><pre><code>
service httpd restart<br>
service dhis-integration restart
</code></pre>
</li>
</ol>
Now the DHIS2 integration app is available on landing screen, given that the user has reporting privileges.<br>
Once you open the app you land on DHIS integration app page, where you select the month and year for given program, type a comment and submit report.                  
 
<h2>Configure New Program</h2>
<ol><li>Configure the concatenated reports for the program</li>
<li>Put the following configuration in the concatenated report to make it DHIS2 program.<br>
"DHISProgram": true,Example: <a href="https://github.com/Possiblehealth/possible-config/blob/8228d24730d854fa282ee04f16ec3d598e86909c/openmrs/apps/reports/reports.json#L1780-L1782">Safe motherhood program</a></li>
 <li>Create a DHIS configuration file for the program with the name of program under '/var/www/bahmni_config/dhis2/' folder.</li>
 <li>To use different folder change 'dhis.config.directory' configuration at '/etc/dhis-integration/dhis-integration.yml'.</li>
</ol>

<p>
DHIS configuration file should have the following structure.
<strong>DHIS Config</strong>
<pre><code>
{
  "orgUnit": "<orgUnitId | find it from DHIS instance>",
  "reports": {
    "<name of 1st sub report | find it from reports.json>": {
      "dataValues": [
        {
          "categoryOptionCombo": "<category option combination id | find it from DHIS instance>",
          "dataElement": "<data element id | find it from DHIS instance>",
          "row": <row number of the cell | find it from output of the SQL report>,
          "column": <column number of the cell | find it from output of the SQL report>
        },
        {
          "categoryOptionCombo": "<category option combination id | find it from DHIS instance>",
          "dataElement": "<data element id | find it from DHIS instance>",
          "row": <row number of the cell | find it from output of the SQL report>,
          "column": <column number of the cell | find it from output of the SQL report>
        },
        ............more data element mappings............
      ]
    },
    "<name of 2nd sub report | find it from reports.json>": {
    "dataValues": [......]
    },
    "<name of 3rd sub report | find it from reports.json>": {
    "dataValues": [......]
    },
    ............more sub report mappings............
  }
}
</code></pre>
An example configuration for <a href="https://raw.githubusercontent.com/Possiblehealth/possible-config/8228d24730d854fa282ee04f16ec3d598e86909c/dhis2/Safe_Motherhood_Program.json">Safe Motherhood program</a> will look like the following
Example: Safe motherhood program
 <table>
 <tr><th>Key</th><th>Description</th></tr>
 <tr><td>orgUnit</td><td>This is the organisation unit ID from DHIS</td></tr>
 <tr><td>reports</td><td>
<>This is list of reports which are the inner reports of concatenated report of the program. Each report name is a unique key in this object.</p>
<p>'Antenatal Checkup' is one of the inner reports configured in concatenated report, for example.</p>
</td></tr>
 <tr><td>dataValues</td><td>This is list of data element mappings. Each mapping maps a cell in SQL output to a dataElement in DHIS.</td></tr>
 <tr><td>categoryOptionCombo</td><td>This is the 'category option combination id' of the dataElement in DHIS.</td></tr>
 <tr><td>dataElement</td><td>This is the 'data element id' of the dataElement in DHIS.</td></tr>
 <tr><td>row and column</td><td>This 'row and column' numbers refers to a particular cell in the output of configured SQL.</td></tr>
 </table>
 
<strong>Notes:</strong> To find out the orgUnit Id, dataElement Id, category option combo Id do the following:
<ol><li>Access the DHIS2 government server in your browser.</li>
<li>Open data entry apps and select appropriate organisation and location.</li>
<li>Once the data entry forms are visible, click on the input boxes where you enter the data.</li>
<li>Right click on the input box and select "Inspect" from the options.</li>
<li>Copy the Id of the html element from window (See image), it would look like the following string: "kSnqP4GPOsQ-kdsirVNKdhm-val".</li>
<li>This string is in the format of "dataElementId - categoryOptionComboId - ...."</li>
<li>These dataElementId and categoryOptionComboId need to be used in DHIS2 configuration file. Refer the below image.</li>
</ol>
</p>
