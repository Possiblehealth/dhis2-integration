package com.possible.dhis2int;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory {
	
	private final Properties properties;
	
	@Autowired
	public RestTemplateFactory(Properties properties) {
		this.properties = properties;
	}
	
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(properties.dhisUser, properties.dhisPassword));
		return restTemplate;
	}
}