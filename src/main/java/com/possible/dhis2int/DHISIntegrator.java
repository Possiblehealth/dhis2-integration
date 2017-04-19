package com.possible.dhis2int;

import static org.apache.log4j.Logger.getLogger;

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

import org.apache.log4j.Logger;
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
	
	private static final Logger logger = getLogger(DHISIntegrator.class);
	
	private final String DOWNLOAD_FORMAT = "application/vnd.ms-excel";
	
	private final String UPLOAD_ENDPOINT = "/api/dataValueSets";
	
	private Properties properties;
	
	@Autowired
	public DHISIntegrator(Properties properties) {
		this.properties = properties;
		
	}
	
	@RequestMapping(path = "/is-logged-in")
	public String isLoggedIn() {
		return "Logged in";
	}
	
	@RequestMapping(path = "/upload-to-dhis")
	public String uploadToDhis(@RequestParam("name") String name,
	                           @RequestParam("year") Integer year,
	                           @RequestParam("month") Integer month,
	                           HttpServletResponse response) {
		try {
			uploadToDhis(name, year, month);
		}
		catch (SQLException | IOException e) {
			logger.error("Upload to DHIS failed", e);
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		return "ok";
	}
	
	@RequestMapping(path = "/download")
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
			logger.error("Download failure:", e);
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void uploadToDhis(String name, Integer year, Integer month) throws SQLException, IOException {
		JSONObject reportConfig = getConfig(properties.reportsJson);
		List<JSONObject> childReports = jsonArraytoList(reportConfig.getJSONObject(name).getJSONObject("config")
				.getJSONArray("reports"));
		JSONObject dhisConfig = getDhisConfig(name);
		ReportDateRange dateRange = new DateConverter().getDateRange(year, month);
		List programDataValue = getDataValues(childReports, dhisConfig.getJSONObject("reports"), dateRange);
		
		JSONObject programDataValueSet = new JSONObject();
		programDataValueSet.put("orgUnit", dhisConfig.getString("orgUnit"));
		programDataValueSet.put("dataValues", programDataValue);
		programDataValueSet.put("period", String.format("%d%02d", year, month));
		
		JSONObject response = post(programDataValueSet);
	}
	
	private JSONObject getConfig(String configFile) {
		try {
			return new JSONObject(new JSONTokener(new FileReader(configFile)));
		}
		catch (IOException e) {
			logger.error("Invalid json file:", e);
			e.printStackTrace();
		}
		return null;
	}
	
	private ResultSet getResult(String sql, ReportDateRange dateRange) throws SQLException {
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
	
	private List getDataValues(List<JSONObject> reportSqlConfigs, JSONObject reportDHISConfigs, ReportDateRange dateRange)
			throws SQLException, IOException {
		ArrayList<Object> programDataValues = new ArrayList<>();
		
		for (JSONObject report : reportSqlConfigs) {
			String sqlPath = report.getJSONObject("config").getString("sqlPath");
			ResultSet resultSet = getResult(getContent(sqlPath), dateRange);
			JSONArray dataValues = reportDHISConfigs
					.getJSONObject(report.getString("name"))
					.getJSONArray("dataValues");
			updateDataValues(resultSet, dataValues);
			programDataValues.addAll(jsonArraytoList(dataValues));
		}
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
