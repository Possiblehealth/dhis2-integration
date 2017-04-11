package com.possible.dhis2int;

import static java.lang.String.format;

import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DHISIntegrator {
	
	public static final String reportsBaseUrl = "http://127.0.0.1:8051/bahmnireports/report?";
	
	private static final String parameters = "name=%s&startDate=%s&endDate=%s&&responseType=text/csv";
	
	private static final String dhisConfigDirectory = "/var/www/bahmni_config/dhis2/";
	
	private static String api = "http://35.154.1.137:8080/api/dataValueSets";
	
	private static String databaseUrl = "jdbc:mysql://192.168.33.10/openmrs?"
			+ "user=openmrs-user&password=password";
	
	private Properties properties;
	
	@Autowired
	public DHISIntegrator(Properties properties) {
		this.properties = properties;
		
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
	
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public String sample(HttpServletResponse response) {
		return "hello";
	}
	
	@RequestMapping(path = "/upload-to-dhis", method = RequestMethod.GET)
	public List uploadToDhis(@RequestParam("name") String name,
	                         @RequestParam("year") Integer year,
	                         @RequestParam("month") Integer month) {
		JSONObject reportConfig = getConfig(properties.reportsJson);
		List<JSONObject> reports = jsonArraytoList(reportConfig.getJSONObject(name).getJSONObject("config")
				.getJSONArray("reports"));
		JSONObject dhisConfig = getDhisConfig(name);
		ReportDateRange dateRange = new DateConverter().getDateRange(year, month);
		List programDataValue = getDataValues(reports, dhisConfig, dateRange);
		
		//get concatenated report config
		//execute each customsql and map to dhis2 elements
		//post all of them
		return programDataValue;
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
				dataValues.forEach(dataValue -> updateDataValues(resultSet, (JSONObject) dataValue));
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
	
	private void updateDataValues(ResultSet resultSet, JSONObject dataValue) {
		try {
			resultSet.absolute(dataValue.getInt("row"));
			String value = resultSet.getString(dataValue.getInt("column"));
			dataValue.put("value", value);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject getDhisConfig(String programName) {
		String dhisConfigFile = dhisConfigDirectory + programName + ".json";
		return getConfig(dhisConfigFile);
	}
	
	@RequestMapping(path = "/report", method = RequestMethod.GET)
	public void downloadReport(@RequestParam("name") String name,
	                           @RequestParam("year") Integer year,
	                           @RequestParam("month") Integer month,
	                           HttpServletResponse response) {
		ReportDateRange reportDateRange = new DateConverter().getDateRange(year, month);
		
		try {
			String encodedParameters = URLEncoder.encode(
					format(parameters, name, reportDateRange.getStartDate(), reportDateRange.getEndDate()), "UTF-8");
			response.sendRedirect(reportsBaseUrl + encodedParameters);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
