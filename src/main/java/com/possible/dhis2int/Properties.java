package com.possible.dhis2int;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Properties {
	
	@Value("${bahmni.login.url}")
	public String bahmniLoginUrl;
	
	@Value("${openmrs.root.url}")
	public String openmrsRootUrl;
	
	@Value("${reports.json}")
	public String reportsJson;
	
	@Value("${reports.url}")
	public String reportsUrl;
	
	@Value("${dhis.config.directory}")
	public String dhisConfigDirectory;
	
	@Value("${dhis.url}")
	public String dhisUrl;
	
	@Value("${openmrs.db.url}")
	public String openmrsDBUrl;
	
	@Value("${dhis.password}")
	public String dhisPassword;
	
	@Value("${dhis.user}")
	public String dhisUser;
}
