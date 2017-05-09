package com.possible.dhis2int.openmrs;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.possible.dhis2int.Properties;

@Service
public class OpenmrsClient {
	
	private static final Logger logger = Logger.getLogger(OpenmrsClient.class);
	
	private Properties properties;
	
	@Autowired
	public OpenmrsClient(Properties properties) {
		this.properties = properties;
	}
	
	public <T> ResponseEntity<T> get(String sessionId, String url, Class<T> returnType) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Cookie", OpenMRSAuthenticator.OPENMRS_SESSION_ID_COOKIE_NAME + "=" + sessionId);
		try {
			return new RestTemplate()
					.exchange(properties.openmrsRootUrl + url,
							HttpMethod.GET,
							new HttpEntity<>(null, requestHeaders),
							returnType
					);
		}
		catch (HttpClientErrorException exception) {
			logger.warn("Could not authenticate with OpenMRS", exception);
			return new ResponseEntity<>(exception.getStatusCode());
		}
	}
}