<h1>Implementation Guide</h1>
Connect to the EMR instance and follow the instructions below. This guide was tested on a dockerised EMR implementation. 
<h2>A. Install the DHIS2 Integration App</h2>
Assuming you have Bahmni installer latest version installed and running successfully. 
<ol>
<li>Update your distribution.<pre><code>sudo yum update</code></pre></li>
<li>Navigate to the home directory.<pre><code>cd /home</code></pre></li>
<li>Clone the binaries repo.<pre><code>git clone https://github.com/khobatha/bahmni-app-dhis2-integration-rpms.git</code></pre></li>
<li>Install the dhis integration app.
<ul>Navigate to the binaries folder<pre><code>cd bahmni-app-dhis2-integration-rpms</code></pre></ul>
<ul>Install the latest version of the dhis2 integration app<pre><code>sudo yum install dhis-integration-1.0-1_060721.noarch.rpm</code></pre></ul>
</li>
</ol>

<h2>B. Configure DHIS2 Integration: Security</h2>  
<ol>
<li>Download and place the ssl.conf file.
<pre><code>
cd /etc/httpd/conf.d/ <br>
wget https://raw.githubusercontent.com/Possiblehealth/possible-config/89662e8e823fac3dbcaf111aa72713a63139bb03/playbooks/roles/possible-dhis-integration/templates/dhis_integration_ssl.conf<br>
</code></pre>
</li>
<li>Navigate to the ssl.conf file and disable (comment out) all configuration entries (SSLCertificateFile, SSLCertificateKey, SSLCertificateChainFile) containing hiels.org.
<pre><code>cd /etc/httpd/conf.d/ssl.conf<br></code></pre>
</li>
<li>Use openssl to generate a self signed certificate, valid for 1 year, and copy it to /etc/bahmni-certs. NB: Use hostnamectl command to check the static
 hostname of the container and enter it as the Common Name (CN) when prompted for the CN by the openssl tool.
<pre><code>cd ~</code></pre>
<pre><code>openssl req -newkey rsa:4096 -x509 -sha256 -days 365 -nodes -out dhis2_integration_app.crt -keyout dhis2_integration_app.key</code></pre>
<pre><code>cp dhis2_integration_app.crt dhis2_integration_app.key /etc/bahmni-certs </code></pre>
</li>   
<li>Update /etc/httpd/conf.d/ssl.conf with new configuration entries for SSLCertificateFile and SSLCertificateKey.
<pre><code>SSLCertificateFile /etc/bahmni-certs/dhis2_integration_app.crt</code></pre>
<pre><code>SSLCertificateKeyFile /etc/bahmni-certs/dhis2_integration_app.key</code></pre>
</li>   
<li>Restart http service to reload the new ssl template configurations and verify that it is running.
<pre><code>sudo systemctl restart httpd</code></pre>
<pre><code>sudo systemctl status httpd</code></pre>
</li>
<li>Install the self-signed ssl certificate into the system keystore.
<ul>Navigate to the home directory<pre><code>cd ~</code></pre></ul>
<ul>The next step is now to import the certificate into the system keystore (cacerts). NB: You may use sudo find / -name cacerts to locate the exact path to your system keystore. Use the container static hostname as an alias. <pre><code>keytool -importcert -alias 0568561e1f23 -keystore /usr/java/jre1.8.0_131/lib/security/cacerts -storepass changeit -file dhis2_integration_app.crt</code></pre></ul>
</li> 
<li>Restart dhis-integration service to reload the new security configurations and verify that it is running.
<pre><code>sudo systemctl restart dhis-integration</code></pre>
<pre><code>sudo systemctl status dhis-integration</code></pre>
</li>
  
</ol> 

  
<h2>C. Configure DHIS2 Integration App: Properties</h2>  
<ol>
<li>Update the properties file of the DHIS2 integration app with right configuration. NB: Use the hostname of the EMR container in the openmrs.root.url and openmrs.db.url as exemplified below.
<ul>Navigate to the propertiles file.<pre><code>cd /etc/dhis-integration/dhis-integration.yml</code></pre></ul>
<table>
  <tr><th>Key</th><th>Description</th><th>Example</th></tr>
  <tr><td>openmrs.root.url</td><td>Url to access Openmrs service</td><td>http://0568561e1f23/openmrs/ws/rest/v1</td></tr>
  <tr><td>bahmni.login.url</td><td>When user isn't logged in, then user is redirected to this url.</td><td>
https://localhost/bahmni/home/#/login?showLoginMessage
</td></tr>
  <tr><td>reports.url</td><td>Bahmni reports url. Used for downloading reports.</td><td>https://localhost/bahmnireports/report</td></tr>
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
  <tr><td>openmrs.db.url</td><td>Mysql connection url to access "openmrs" database. Set valid user and password in the url.</td><td>jdbc:mysql://0568561e1f23/openmrs?user=user&password=password</td></tr>
  <tr><td>submission.audit.folder</td><td>All DHIS2 submissions are stored in this directory. Ensure the directory exists and "bahmni" user has access to it, or configure a different directory.</td><td>/dhis-integration-data</td></tr>
  <tr><td>server.port</td><td>Server config. Port for server to listen to.</td><td>8040</td></tr>
  <tr><td>server.context-path</td><td>Server config. Mapping incoming requests.</td><td>/dhis-integration/</td></tr>
  <tr><td>log4j.config.file</td><td>Server config. Properties file for logger of dhis-integration server.</td><td>log4j.properties</td></tr>
 </table>
</li>
<li>Ensure Bahmni reports service is installed and running successfully.
 <pre><code>
  systemctl status bahmni-reports
 </code></pre>
</li>
<li>Restart ssl and dhis-integration services.
<ul>Restart httpd<pre><code>systemctl restart httpd</code></pre></ul>
<ul>Restart dhis-integration service<pre><code>systemctl restart dhis-integration</code></pre></ul>
  The DHIS2 Integration App should now be accessible from the landing screen, given that the user has reporting privileges.
</li>
</li>
</ol>


<h2>D. Configure DHIS2 Integration App: OpenMRS Privileges</h2>  
Create a new OpenMRS privilege named "Submit DHIS Report" and grant it to all user groups that will use the App.
<ol>
<li>Navigate to OpenMRS home page (e.g localhost/openmrs).</li> 
<li>Select Administration.</li>
<li>Select Manage Privileges.</li>  
<li>Select "Add Privilege" and add the "Submit DHIS Report" privilege.</li> 
<li>Select "Manage Roles" and add the "Submit DHIS Report" privilege to all user roles that need to use the DHIS2 Integration App to sync reports to DHIS2.</li> 
</li>
</ol>
                  
 
<h2>E. Configure New Program and Map to DHIS2</h2>
<ol>
<li>Load a simple OpenMRS report to map to DHIS2:
<ul>Navigate to /development inside the EMR container: <pre><code>docker exec -it bahmni_docker_emr-service_1 bash</code></pre><pre><code>cd /development/bahmni_config_release/</code></pre></ul>
<ul>Disable the release version of bahmni_config092 and pull a development version containing the test report: <pre><code>mv bahmni_config092 bahmni_config092_bkp</code></pre><pre><code>git clone https://github.com/khobatha/bahmni_config092.git</code></pre></ul>

<ul>Refresh Bahmni landing page and to go to Reports and ensure that report TESTS-01 | DHIS2 Integration App SYNC Test report has been loaded and that it runs.</ul>
</li>
<li>View configs for the TESTS-01 | DHIS2 Integration App SYNC Test report:
<ul>View the report implementation: <pre><code>cd bahmni_config092/openmrs/apps/reports/sql/</code></pre><pre><code>nano dhis2_integration_test.sql</code></pre></ul>
<ul>View the configuration that registers the report in Bahmni for viewing under Bahmni reports. You must scroll to the end of the config file to see the configuration entry for the test report: <pre><code>cd bahmni_config092/openmrs/apps/reports/</code></pre><pre><code>nano reports.json</code></pre></ul>
</li>
<li>Put the following configuration in the TESTS-01 | DHIS2 Integration App Sync Test report reports.json config to make it a DHIS2 program and therefore have it listed under the Integration App for mapping to DHIS2.<br>Example: <a href="https://github.com/Possiblehealth/possible-config/blob/8228d24730d854fa282ee04f16ec3d598e86909c/openmrs/apps/reports/reports.json#L1780-L1782">Safe motherhood program</a><pre><code>"DHISProgram": true</code></pre></li>
 <li>Navigate to the DHIS2 Integration App on Bahmni and ensure that the TEST-01 report is now listed under DHIS2 programs.</li>
 <li>Create DHIS2 mapping configs for the TESTS-01 | DHIS2 Integration App Sync Test report:
<ul>Navigate to the DHIS2 Integration App mappings configs directory: <pre><code>cd bahmni_config092/dhis2</code></pre></ul>
<ul>Create a new json file to store the mappings for the TESTS-01 report: <pre><code>touch TESTS-01_DHIS2_Integration_App_Sync_Test.json</code></pre></ul>
<li>Define the mappings for the TESTS-01 report in TESTS-01_DHIS2_Integration_App_Sync_Test.json: 
<p>
DHIS2 mapping configuration file should have the following structure:
<pre><code>
{
  "orgUnit": "&lt;orgUnitId | find it from DHIS instance&gt;",
  "reports": {
    "&lt;name of 1st sub report | find it from reports.json&gt;": {
      "dataValues": [
        {
          "categoryOptionCombo": "&lt;category option combination id | find it from DHIS instance&gt;",
          "dataElement": "&lt;data element id | find it from DHIS instance&gt;",
          "row": &lt;row number of the cell | find it from output of the SQL report&gt;,
          "column": &lt;column number of the cell | find it from output of the SQL report&gt;
        },
        {
          "categoryOptionCombo": "&lt;category option combination id | find it from DHIS instance&lgt;",
          "dataElement": "&lt;data element id | find it from DHIS instance&gt;",
          "row": &lt;row number of the cell | find it from output of the SQL report&gt;,
          "column": &lt;column number of the cell | find it from output of the SQL report&gt;
        },
        ............more data element mappings............
      ]
    },
    "&lt;name of 2nd sub report | find it from reports.json&gt;": {
    "dataValues": [......]
    },
    "&lt;name of 3rd sub report | find it from reports.json&gt;": {
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
</p>
</li>
</ol>

