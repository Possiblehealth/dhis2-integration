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
	
	public static final String reportsBaseUrl = "https://mybahmni.org/bahmnireports/report";
	
	private static final String DOWNLOAD_FORMAT = "application/vnd.ms-excel";
	
	private static final String dhisConfigDirectory = "/var/www/bahmni_config/dhis2/";
	
	private static String api = "http://35.154.1.137:8080/api/dataValueSets";
	
	private static String databaseUrl = "jdbc:mysql://192.168.33.10/openmrs?"
			+ "user=openmrs-user&password=password";
	
	private Properties properties;
	
	@Autowired
	public DHISIntegrator(Properties properties) {
		this.properties = properties;
		
	}
	
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public String sample(HttpServletResponse response) {
		return "hello";
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
	
	@RequestMapping(path = "/report", method = RequestMethod.GET)
	public void downloadReport(@RequestParam("name") String name,
	                           @RequestParam("year") Integer year,
	                           @RequestParam("month") Integer month,
	                           HttpServletResponse response) {
		ReportDateRange reportDateRange = new DateConverter().getDateRange(year, month);
		try {
			String redirectUri = UriComponentsBuilder.fromHttpUrl(reportsBaseUrl)
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
	
	private static JSONObject getConfig(String configFile) {
		try {
			return new JSONObject(new JSONTokener(new FileReader(configFile)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static ResultSet getResult(String sql, ReportDateRange dateRange) throws ClassNotFoundException, SQLException {
		String formattedSql = sql.replaceAll("#startDate#", dateRange.getStartDate()).replaceAll("#endDate#",
				dateRange.getEndDate());
		return DriverManager.getConnection(databaseUrl).createStatement().executeQuery(formattedSql);
	}
	
	private static String getContent(String filePath) throws IOException {
		return Files.readAllLines(Paths.get(filePath)).stream().reduce((x, y) -> x + "\n" + y).get();
	}
	
	private static JSONObject post(JSONObject jsonObject) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("admin", "District123"));
		
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(api, HttpMethod.POST, entity, String.class);
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
		String dhisConfigFile = dhisConfigDirectory + programName + ".json";
		return getConfig(dhisConfigFile);
	}
}
