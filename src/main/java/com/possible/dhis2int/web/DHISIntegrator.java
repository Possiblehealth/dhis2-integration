package com.possible.dhis2int.web;

import static com.possible.dhis2int.web.Messages.CONFIG_FILE_NOT_FOUND;
import static com.possible.dhis2int.web.Messages.DHIS_RETURNED_NON_OK_STATUS_CODE;
import static com.possible.dhis2int.web.Messages.DHIS_SUBMISSION_FAILED;
import static com.possible.dhis2int.web.Messages.FILE_READING_EXCEPTION;
import static com.possible.dhis2int.web.Messages.REPORT_DOWNLOAD_FAILED;
import static com.possible.dhis2int.web.Messages.SQL_EXECUTION_EXCEPTION;
import static com.possible.dhis2int.web.Messages.SQL_OUTPUT_MAPPING_EXCEPTION;
import static com.possible.dhis2int.web.Cookies.BAHMNI_USER;
import static java.lang.String.format;
import static org.apache.log4j.Logger.getLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.possible.dhis2int.dhis.DHISClient;
import com.possible.dhis2int.log.AuditLog;
import com.possible.dhis2int.date.DateConverter;
import com.possible.dhis2int.Properties;
import com.possible.dhis2int.date.ReportDateRange;

@RestController
public class DHISIntegrator {
	
	private final Logger logger = getLogger(DHISIntegrator.class);
	
	private final String DOWNLOAD_FORMAT = "application/vnd.ms-excel";
	
	private final String UPLOAD_ENDPOINT = "/api/dataValueSets";
	
	private final DHISClient dHISClient;
	
	private Properties properties;
	
	private AuditLog auditLog;
	
	@Autowired
	public DHISIntegrator(DHISClient dHISClient, Properties properties, AuditLog auditLog) {
		this.dHISClient = dHISClient;
		this.properties = properties;
		this.auditLog = auditLog;
	}
	
	@RequestMapping(path = "/is-logged-in")
	public String isLoggedIn() {
		return "Logged in";
	}
	
	@RequestMapping(path = "/upload-to-dhis")
	public String uploadToDHIS(@RequestParam("name") String program,
	                           @RequestParam("year") Integer year,
	                           @RequestParam("month") Integer month,
	                           HttpServletRequest clientReq,
	                           HttpServletResponse clientRes) {
		String userName = new Cookies(clientReq).getValue(BAHMNI_USER);
		try {
			ResponseEntity<String> DHISResponse = uploadToDHIS(program, year, month);
			String responseBody = validateSubmission(DHISResponse);
			auditLog.success(program, userName, responseBody);
			return responseBody;
		}
		catch (DHISIntegratorException e) {
			logger.error(DHIS_SUBMISSION_FAILED, e);
			clientRes.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			auditLog.failure(program, userName, DHIS_SUBMISSION_FAILED);
			return DHIS_SUBMISSION_FAILED;
		}
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
			logger.error(format(REPORT_DOWNLOAD_FAILED, name), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private String validateSubmission(ResponseEntity<String> submissionRes)
			throws DHISIntegratorException {
		if (submissionRes.getStatusCodeValue() != 200) {
			throw new DHISIntegratorException(
					format(DHIS_RETURNED_NON_OK_STATUS_CODE, submissionRes.getStatusCodeValue(), submissionRes.getBody()));
		}
		JSONObject submissionResBody = new JSONObject(new JSONTokener(submissionRes.getBody()));
		return null;
	}
	
	private ResponseEntity<String> uploadToDHIS(String name, Integer year, Integer month) throws DHISIntegratorException {
		JSONObject reportConfig = getConfig(properties.reportsJson);
		List<JSONObject> childReports = jsonArrayToList(reportConfig.getJSONObject(name).getJSONObject("config")
				.getJSONArray("reports"));
		JSONObject dhisConfig = getDHISConfig(name);
		ReportDateRange dateRange = new DateConverter().getDateRange(year, month);
		List programDataValue = getProgramDataValues(childReports, dhisConfig.getJSONObject("reports"), dateRange);
		
		JSONObject programDataValueSet = new JSONObject();
		programDataValueSet.put("orgUnit", dhisConfig.getString("orgUnit"));
		programDataValueSet.put("dataValues", programDataValue);
		programDataValueSet.put("period", format("%d%02d", year, month));
		
		return dHISClient.post(UPLOAD_ENDPOINT, programDataValueSet, String.class);
	}
	
	private JSONObject getConfig(String configFile) throws DHISIntegratorException {
		try {
			return new JSONObject(new JSONTokener(new FileReader(configFile)));
		}
		catch (FileNotFoundException e) {
			throw new DHISIntegratorException(format(CONFIG_FILE_NOT_FOUND, configFile), e);
		}
	}
	
	private ResultSet getResult(String sql, ReportDateRange dateRange) throws DHISIntegratorException {
		String formattedSql = sql.replaceAll("#startDate#", dateRange.getStartDate()).replaceAll("#endDate#",
				dateRange.getEndDate());
		try {
			return DriverManager.getConnection(properties.openmrsDBUrl).createStatement().executeQuery(formattedSql);
		}
		catch (SQLException e) {
			throw new DHISIntegratorException(format(SQL_EXECUTION_EXCEPTION, formattedSql), e);
		}
	}
	
	private String getContent(String filePath) throws DHISIntegratorException {
		try {
			return Files.readAllLines(Paths.get(filePath)).stream().reduce((x, y) -> x + "\n" + y).get();
		}
		catch (IOException e) {
			throw new DHISIntegratorException(format(FILE_READING_EXCEPTION, filePath), e);
		}
	}
	
	private List getProgramDataValues(List<JSONObject> reportSqlConfigs, JSONObject reportDHISConfigs,
	                                  ReportDateRange dateRange)
			throws DHISIntegratorException {
		ArrayList<Object> programDataValues = new ArrayList<>();
		
		for (JSONObject report : reportSqlConfigs) {
			JSONArray dataValues = getReportDataElements(reportDHISConfigs, dateRange, report);
			programDataValues.addAll(jsonArrayToList(dataValues));
		}
		return programDataValues;
	}
	
	private JSONArray getReportDataElements(JSONObject reportDHISConfigs, ReportDateRange dateRange, JSONObject report)
			throws DHISIntegratorException {
		JSONArray dataValues = new JSONArray();
		try {
			dataValues = reportDHISConfigs.getJSONObject(report.getString("name")).getJSONArray("dataValues");
		}
		catch (JSONException e) {
			return dataValues;
		}
		String sqlPath = report.getJSONObject("config").getString("sqlPath");
		ResultSet resultSet = getResult(getContent(sqlPath), dateRange);
		for (Object dataValue_ : dataValues) {
			JSONObject dataValue = (JSONObject) dataValue_;
			try {
				updateDataElements(resultSet, dataValue);
			}
			catch (SQLException e) {
				throw new DHISIntegratorException(format
						(SQL_OUTPUT_MAPPING_EXCEPTION, dataValue.getInt("row"), dataValue.getInt("column"), sqlPath), e);
			}
		}
		return dataValues;
	}
	
	private List<JSONObject> jsonArrayToList(JSONArray elements) {
		List<JSONObject> list = new ArrayList<>();
		elements.forEach((element) -> list.add((JSONObject) element));
		return list;
	}
	
	private void updateDataElements(ResultSet resultSet, JSONObject dataElement) throws SQLException {
		resultSet.absolute(dataElement.getInt("row"));
		String value = resultSet.getString(dataElement.getInt("column"));
		dataElement.put("value", value);
	}
	
	private JSONObject getDHISConfig(String programName) throws DHISIntegratorException {
		String DHISConfigFile = properties.dhisConfigDirectory + programName.replaceAll(" ", "_") + ".json";
		return getConfig(DHISConfigFile);
	}
}
