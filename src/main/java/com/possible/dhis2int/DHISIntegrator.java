package com.possible.dhis2int;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class DHISIntegrator {
	
	private final String DOWNLOAD_FORMAT = "application/vnd.ms-excel";
	
	private final String UPLOAD_ENDPOINT = "/api/dataValueSets";
	
	private Properties properties;
	
	@Autowired
	public DHISIntegrator(Properties properties) {
		this.properties = properties;
		
	}
	
	@RequestMapping(path = "/is-logged-in")
	public String isLoggedIn(){
		return "Logged in";
	}
	
	@RequestMapping(path = "/upload-to-dhis", method = RequestMethod.GET)
	public String uploadToDhis(@RequestParam("name") String name,
	                           @RequestParam("year") Integer year,
	                           @RequestParam("month") Integer month) {
		JSONObject reportConfig = getConfig(properties.reportsJson);
		List<JSONObject> reports = jsonArraytoList(reportConfig.getJSONObject(name).getJSONObject("config")
				.getJSONArray("reports"));
		JSONObject dhisConfig = getDhisConfig(name);
		ReportDateRange dateRange = new DateConverter().getDateRange(year, month);
		List programDataValue = getDataValues(reports, dhisConfig, dateRange);
		
		JSONObject programDataValueSet = new JSONObject();
		programDataValueSet.put("orgUnit", dhisConfig.getString("orgUnit"));
		programDataValueSet.put("dataValues", programDataValue);
		programDataValueSet.put("period", String.format("%d%02d", year, month));
		
		JSONObject response = post(programDataValueSet);
		
		return "ok";
	}
	
	@RequestMapping(path = "/download", method = RequestMethod.GET)
	public void downloadReport(@RequestParam("name") String name,
	                           @RequestParam("year") Integer year,
	                           @RequestParam("month") Integer month,
	                           HttpServletResponse response) {
		ReportDateRange reportDateRange = new DateConverter().getDateRange(year, month);
		try {
			String redirectUri = UriComponentsBuilder.fromHttpUrl(properties.reportsUrl)
					.queryParam("responseType", DOWNLOAD_FORMAT)
					.queryParam("name", name)
					.queryParam("startDate", reportDateRange.getStartDate())
					.queryParam("endDate", reportDateRange.getEndDate())
					.toUriString();
			response.sendRedirect(redirectUri);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject getConfig(String configFile) {
		try {
			return new JSONObject(new JSONTokener(new FileReader(configFile)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ResultSet getResult(String sql, ReportDateRange dateRange) throws ClassNotFoundException, SQLException {
		String formattedSql = sql.replaceAll("#startDate#", dateRange.getStartDate()).replaceAll("#endDate#",
				dateRange.getEndDate());
		return DriverManager.getConnection(properties.openmrsDBUrl).createStatement().executeQuery(formattedSql);
	}
	
	private String getContent(String filePath) throws IOException {
		return Files.readAllLines(Paths.get(filePath)).stream().reduce((x, y) -> x + "\n" + y).get();
	}
	
	private JSONObject post(JSONObject jsonObject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(properties.dhisUser, properties.dhisPassword));
		
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(properties.dhisUrl + UPLOAD_ENDPOINT, HttpMethod.POST,
				entity, String.class);
		if (responseEntity.getStatusCodeValue() != 200) {
			System.out.println("Failed due to :" + responseEntity.getBody());
		}
		return new JSONObject(new JSONTokener(responseEntity.getBody()));
	}
	
	private List getDataValues(List<JSONObject> reports, JSONObject dhisConfig, ReportDateRange dateRange) {
		ArrayList<Object> programDataValues = new ArrayList<>();
		
		reports.forEach((report) -> {
			String sqlPath = report.getJSONObject("config").getString("sqlPath");
			try {
				ResultSet resultSet = getResult(getContent(sqlPath), dateRange);
				JSONArray dataValues = dhisConfig.getJSONObject("reports")
						.getJSONObject(report.getString("name"))
						.getJSONArray("dataValues");
				updateDataValues(resultSet, dataValues);
				programDataValues.addAll(jsonArraytoList(dataValues));
			}
			catch (IOException | SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
		return programDataValues;
	}
	
	private List<JSONObject> jsonArraytoList(JSONArray elements) {
		List<JSONObject> list = new ArrayList<>();
		elements.forEach((element) -> list.add((JSONObject) element));
		return list;
	}
	
	private void updateDataValues(ResultSet resultSet, JSONArray dataValues) throws SQLException {
		for (Object dataValue_ : dataValues) {
			JSONObject dataValue = (JSONObject) dataValue_;
			resultSet.absolute(dataValue.getInt("row"));
			String value = resultSet.getString(dataValue.getInt("column"));
			dataValue.put("value", value);
		}
	}
	
	private JSONObject getDhisConfig(String programName) {
		String dhisConfigFile = properties.dhisConfigDirectory + programName + ".json";
		return getConfig(dhisConfigFile);
	}
}
