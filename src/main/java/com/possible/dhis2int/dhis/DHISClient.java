package com.possible.dhis2int.dhis;

import java.io.Console;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.openmrs.AuthenticationResponse;
import com.possible.dhis2int.openmrs.OpenMRSAuthenticator;
import com.possible.dhis2int.web.Cookies;
import com.possible.dhis2int.web.DHISIntegrator;
import com.possible.dhis2int.web.RestTemplateFactory;

@Service
public class DHISClient {

	private RestTemplateFactory restTemplateFactory;

	private Properties properties;
	
	private OpenMRSAuthenticator authenticator;
	
	public static final Logger LOGGER = Logger.getLogger(DHISIntegrator.class);

	@Autowired
	public DHISClient(RestTemplateFactory restTemplateFactory, Properties properties, OpenMRSAuthenticator authenticator) {
		this.restTemplateFactory = restTemplateFactory;
		this.properties = properties;
		this.authenticator = authenticator;
	}
	
	public boolean hasDhisSubmitPrivilege(HttpServletRequest request, HttpServletResponse response) {
        Cookies cookies = new Cookies(request);
        String cookie = cookies.getValue(Cookies.DHIS_INTEGRATION_COOKIE_NAME);
        
        AuthenticationResponse authenticationResponse = authenticator.authenticateReportSubmitingPrivilege(cookie);
        switch (authenticationResponse) {
            case SUBMIT_AUTHORIZED:
            	return true;
            case SUBMIT_UNAUTHORIZED:
            	return false;
            default:
            	return false;
        }
    }
	
	
	public ResponseEntity<String> postDailyReport(String url, JSONObject jsonObject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
		return restTemplateFactory.getRestTemplateEwars().exchange(properties.dhisEwarsUrl + url, HttpMethod.POST, entity,
				String.class);
	}
	
	
	public ResponseEntity<String> post(String url, JSONObject jsonObject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
		return restTemplateFactory.getRestTemplate().exchange(properties.dhisUrl + url, HttpMethod.POST, entity,
				String.class);
	}

	public ResponseEntity<String> get(String url) {
		LOGGER.info("Inside get method");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(new JSONObject().toString(), headers);
		System.out.println(properties.dhisUrl + url);
		return restTemplateFactory.getRestTemplate().exchange(properties.dhisUrl + url, HttpMethod.GET, entity, String.class);
	}
}