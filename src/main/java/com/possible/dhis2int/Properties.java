package com.possible.dhis2int;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Properties {
	
	@Value("${bahmni.login.url}")
	public String bahmniLoginUrl;
	
	@Value("${openmrs.root.url}")
	public String openmrsRootUrl;
	
}
