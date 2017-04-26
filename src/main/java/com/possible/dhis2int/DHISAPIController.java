package com.possible.dhis2int;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/dhis/api")
public class DHISAPIController {
	
	private final Properties properties;
	
	private String ORG_UNITS_END_POINT = "/api/organisationUnits.json?query={query}";
	
	private String DATA_SETS_END_POINT = "/api/organisationUnits/{orgUnitId}.json?fields=id,name,dataSets[id,name]";
	
	private String DATA_ELEMENTS_END_POINT = "/api/dataSets/{dataSetId}.json?fields=id,name,dataElements[id,name]";
	
	private RestTemplateFactory restTemplateFactory;
	
	@Autowired
	public DHISAPIController(Properties properties, RestTemplateFactory restTemplateFactory) {
		this.properties = properties;
		this.restTemplateFactory = restTemplateFactory;
	}
	
	@RequestMapping(value = "/orgUnits", method = RequestMethod.GET)
	public String getOrgUnits(@RequestParam("orgUnitName") String orgName) throws IOException {
		
		ResponseEntity<String> responseEntity = get(ORG_UNITS_END_POINT, orgName);
		
		if (responseEntity.getStatusCodeValue() != 200) {
			System.out.println("Failed due to :" + responseEntity.getBody());
		}
		
		return responseEntity.getBody();
	}
	
	@RequestMapping(value = "/programs", method = RequestMethod.GET)
	public String getDataSets(@RequestParam("orgUnitId") String orgUnitId) {
		ResponseEntity<String> responseEntity = get(DATA_SETS_END_POINT, orgUnitId);
		
		if (responseEntity.getStatusCodeValue() != 200) {
			System.out.println("Failed due to :" + responseEntity.getBody());
		}
		
		return responseEntity.getBody();
	}
	
	@RequestMapping(value = "/dataElements", method = RequestMethod.GET)
	public String getDataElements(@RequestParam("dataSetId") String dataSetId) {
		ResponseEntity<String> responseEntity = get(DATA_ELEMENTS_END_POINT, dataSetId);
		
		if (responseEntity.getStatusCodeValue() != 200) {
			System.out.println("Failed due to :" + responseEntity.getBody());
		}
		return responseEntity.getBody();
	}
	
	private ResponseEntity<String> get(String api, String... parameters) {
		return restTemplateFactory.getRestTemplate().exchange(properties.dhisUrl + api, HttpMethod.GET, HttpEntity.EMPTY,
				String.class, parameters);
	}
	
}
