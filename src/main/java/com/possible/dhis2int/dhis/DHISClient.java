package com.possible.dhis2int.dhis;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.web.RestTemplateFactory;

@Service
public class DHISClient {
	
	private RestTemplateFactory restTemplateFactory;
	
	private Properties properties;
	
	@Autowired
	public DHISClient(RestTemplateFactory restTemplateFactory, Properties properties) {
		this.restTemplateFactory = restTemplateFactory;
		this.properties = properties;
	}
	
	public ResponseEntity<String> post(String url, JSONObject jsonObject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
		return restTemplateFactory.getRestTemplate().exchange(
				properties.dhisUrl + url, HttpMethod.POST,
				entity, String.class);
	}
}