package com.possible.dhis2int.web;

import static com.possible.dhis2int.audit.Submission.Status.Failure;
import static com.possible.dhis2int.web.Cookies.BAHMNI_USER;
import static com.possible.dhis2int.web.Messages.CONFIG_FILE_NOT_FOUND;
import static com.possible.dhis2int.web.Messages.DHIS_SUBMISSION_FAILED;
import static com.possible.dhis2int.web.Messages.FILE_READING_EXCEPTION;
import static com.possible.dhis2int.web.Messages.REPORT_DOWNLOAD_FAILED;
import static java.lang.String.format;
import static org.apache.log4j.Logger.getLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.audit.Submission;
import com.possible.dhis2int.audit.Submission.Status;
import com.possible.dhis2int.audit.SubmissionLog;
import com.possible.dhis2int.audit.SubmittedDataStore;
import com.possible.dhis2int.date.DateConverter;
import com.possible.dhis2int.date.ReportDateRange;
import com.possible.dhis2int.db.DatabaseDriver;
import com.possible.dhis2int.db.Results;
import com.possible.dhis2int.dhis.DHISClient;

@RestController
public class DHISIntegrator {

	private final Logger logger = getLogger(DHISIntegrator.class);

	private final String DOWNLOAD_FORMAT = "application/vnd.ms-excel";

	private final String SUBMISSION_ENDPOINT = "/api/dataValueSets";

	private final String DHIS_GET_URL = "dhis-web-dataentry/getDataValues.action";

	private final DHISClient dHISClient;

	private final DatabaseDriver databaseDriver;

	private final Properties properties;

	private final SubmissionLog submissionLog;

	private final SubmittedDataStore submittedDataStore;

	private final String IMAM_PROGRAM_NAME = "03-2_Nutrition_Acute_Malnutrition";
	private final String IMAM = "Integrated Management of Acute Malnutrition (IMAM) Program";

	@Autowired
	public DHISIntegrator(DHISClient dHISClient, DatabaseDriver databaseDriver, Properties properties,
			SubmissionLog submissionLog, SubmittedDataStore submittedDataStore) {
		this.dHISClient = dHISClient;
		this.databaseDriver = databaseDriver;
		this.properties = properties;
		this.submissionLog = submissionLog;
		this.submittedDataStore = submittedDataStore;
	}

	@RequestMapping(path = "/is-logged-in")
	public String isLoggedIn() {
		logger.info("Inside isLoggedIn");
		return "Logged in";
	}

	public void prepareImamReport(Integer year, Integer month) throws JSONException {
		logger.info("Inside prepareImamReport method");

		String imamDataSetId = properties.dhisImamDataSetId;

		JSONObject dhisConfig = (JSONObject) getDHISConfig(IMAM_PROGRAM_NAME);
		String orgUnit = (String) dhisConfig.get("orgUnit");

		StringBuilder dhisRequestUrl = new StringBuilder(DHIS_GET_URL);
		dhisRequestUrl.append("?dataSetId=").append(imamDataSetId).append("&organisationUnitId=").append(orgUnit)
				.append("&multiOrganisationUnit=false").append("periodId=").append(year).append(month);

		ResponseEntity<String> response = dHISClient.get(dhisRequestUrl.toString());
		JSONObject jsonResponse = new JSONObject(response.getBody().toString());

		dhisConfig = (JSONObject) dhisConfig.get("reports");

		JSONArray dataValues = new JSONArray();
		dataValues = dhisConfig.getJSONObject(IMAM).getJSONArray("dataValues");
		JSONArray fieldsFromDhis = new JSONArray();

		JSONArray dhisDataSet = jsonResponse.getJSONArray("dataValues");
		Map<String, Integer> valuesFromDhis = new HashMap<>();

		for (Object dataValue_ : jsonArrayToList(dataValues)) {
			JSONObject dataValue = (JSONObject) dataValue_;
			if (dataValue.has("getElementBack") && dataValue.get("getElementBack") != null
					&& (Boolean) dataValue.get("getElementBack")) {
				String id = new StringBuilder().append(dataValue.get("dataElement")).append("-")
						.append(dataValue.get("categoryOptionCombo")).toString();

				for (Object dataVa_ : jsonArrayToList(dhisDataSet)) {
					JSONObject dataVal = (JSONObject) dataVa_;
					if (dataVal.get("id").equals(id)) {
						dataValue.put("value", dataVal.get("val"));
						fieldsFromDhis.put(dataValue);
						valuesFromDhis.put(dataValue.getString("fieldValue"),
								Integer.parseInt((String) dataVal.get("val")));
					}
				}

			}
		}

		Integer numberOfMaleLessThanSix = valuesFromDhis.get("numberOfMaleLessThanSix") != null
				? valuesFromDhis.get("numberOfMaleLessThanSix")
				: 0;
		Integer numberOfFemalesLessThanSix = valuesFromDhis.get("numberOfFemalesLessThanSix") != null
				? valuesFromDhis.get("numberOfFemalesLessThanSix")
				: 0;
		Integer numberOfMalesMoreThanSix = valuesFromDhis.get("numberOfMalesMoreThanSix") != null
				? valuesFromDhis.get("numberOfMalesMoreThanSix")
				: 0;
		Integer numberOfFemalesMoreThanSix = valuesFromDhis.get("numberOfFemalesMoreThanSix") != null
				? valuesFromDhis.get("numberOfFemalesMoreThanSix")
				: 0;

		databaseDriver.createTempTable(numberOfMaleLessThanSix, numberOfFemalesLessThanSix, numberOfMalesMoreThanSix,
				numberOfFemalesMoreThanSix);

	}

	@RequestMapping(path = "/submit-to-dhis")
	public String submitToDHIS(@RequestParam("name") String program, @RequestParam("year") Integer year,
			@RequestParam("month") Integer month, @RequestParam("comment") String comment,
			@RequestParam("isImam") Boolean isImam, HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		String userName = new Cookies(clientReq).getValue(BAHMNI_USER);
		Submission submission = new Submission();
		String filePath = submittedDataStore.getAbsolutePath(submission);
		Status status;
		if (isImam != null && isImam) {
			prepareImamReport(year, month);
		}
		try {
			prepareImamReport(year, month);
			submitToDHIS(submission, program, year, month);
			status = submission.getStatus();
		} catch (DHISIntegratorException | JSONException e) {
			status = Failure;
			submission.setException(e);
			logger.error(DHIS_SUBMISSION_FAILED, e);
		}

		submittedDataStore.write(submission);
		submissionLog.log(program, userName, comment, status, filePath);
		return submission.getInfo();
	}

	@RequestMapping(path = "/submit-to-dhis-atr")
	public String submitToDhisAtrOptCombo(@RequestParam("name") String program, @RequestParam("year") Integer year,
			@RequestParam("month") Integer month, @RequestParam("comment") String comment, HttpServletRequest clientReq,
			HttpServletResponse clientRes) throws IOException, JSONException {
		String userName = new Cookies(clientReq).getValue(BAHMNI_USER);
		Submission headSubmission = new Submission();
		String filePath = submittedDataStore.getAbsolutePath(headSubmission);
		List<Submission> batchSubmission = new ArrayList<>();
		Results results;
		int i = 0;
		try {
			JSONObject reportConfig = getConfig(properties.reportsJson);
			List<JSONObject> childReports = jsonArrayToList(
					reportConfig.getJSONObject(program).getJSONObject("config").getJSONArray("reports"));
			String sqlPath = childReports.get(0).getJSONObject("config").getString("sqlPath");
			ReportDateRange dateRange = new DateConverter().getDateRange(year, month);
			String type = "MRSGeneric";

			results = getResult(getContent(sqlPath), type, dateRange);

			for (List<String> row : results.getRows()) {
				Submission submission = new Submission();
				submitToDhisAtrOptCombo(row, submission, program, year, month);
				Status status = submission.getStatus();
				submission.setStatus(status);
				batchSubmission.add(submission);
				i++;
			}

		} catch (DHISIntegratorException | JSONException | SQLException e) {
			if (batchSubmission.size() > 0) {
				batchSubmission.get(i).setStatus(Failure);
				batchSubmission.get(i).setException(e);
			}
			logger.error(e.getMessage(), e);
			headSubmission.setException(e);
		} finally {
			Status status = Status.Failure;
			String filePathData = "No Data sent";
			if (batchSubmission.size() > 0) {
				filePathData = filePath;
				submittedDataStore.write(batchSubmission, filePathData);
				status = Status.Success;
				for (Submission submit : batchSubmission) {
					headSubmission = submit;
					if (Status.Failure.equals(submit.retrieveStatus())) {
						status = Failure;
						headSubmission = submit;
						break;
					}
				}
			}
			submissionLog.log(program, userName, comment, status, filePathData);
		}
		return headSubmission.getInfo();
	}

	private Submission submitToDhisAtrOptCombo(List<String> row, Submission submission, String name, Integer year,
			Integer month) throws DHISIntegratorException, JSONException, SQLException {
		JSONObject reportConfig = getConfig(properties.reportsJson);

		JSONObject childReport = reportConfig.getJSONObject(name).getJSONObject("config").getJSONArray("reports")
				.getJSONObject(0); // TODO: why always 0 ?

		JSONObject dhisConfig = getDHISConfig(name);
		ReportDateRange dateRange = new DateConverter().getDateRange(year, month);
		List<Object> programDataValue = getProgramDataValuesAttrOptCombo(row, childReport,
				dhisConfig.getJSONObject("reports"), dateRange);

		JSONObject programDataValueSet = new JSONObject();
		programDataValueSet.put("dataset", dhisConfig.getString("dataset"));
		programDataValueSet.put("orgUnit", dhisConfig.getString("orgUnit"));
		programDataValueSet.put("dataValues", programDataValue);
		programDataValueSet.put("period", format("%d%02d", year, month));
		programDataValueSet.put("attributeOptionCombo", row.get(row.size() - 1));

		ResponseEntity<String> responseEntity = dHISClient.post(SUBMISSION_ENDPOINT, programDataValueSet);
		submission.setPostedData(programDataValueSet);
		submission.setResponse(responseEntity);
		return submission;
	}

	@RequestMapping(path = "/submission-log/download", produces = "text/csv")
	public FileSystemResource downloadSubmissionLog(HttpServletResponse response) throws FileNotFoundException {
		response.setHeader("Content-Disposition", "attachment; filename=" + submissionLog.getDownloadFileName());
		return submissionLog.getFile();
	}

	@RequestMapping(path = "/download")
	public void downloadReport(@RequestParam("name") String name, @RequestParam("year") Integer year,
			@RequestParam("month") Integer month, @RequestParam("isImam") Boolean isImam, HttpServletResponse response)
			throws JSONException {
		ReportDateRange reportDateRange = new DateConverter().getDateRange(year, month);
		if (isImam != null && isImam) {
			prepareImamReport(year, month);
		}
		try {
			String redirectUri = UriComponentsBuilder.fromHttpUrl(properties.reportsUrl)
					.queryParam("responseType", DOWNLOAD_FORMAT).queryParam("name", name)
					.queryParam("startDate", reportDateRange.getStartDate())
					.queryParam("endDate", reportDateRange.getEndDate()).toUriString();
			response.sendRedirect(redirectUri);
		} catch (IOException e) {
			logger.error(format(REPORT_DOWNLOAD_FAILED, name), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private Submission submitToDHIS(Submission submission, String name, Integer year, Integer month)
			throws DHISIntegratorException, JSONException, IOException {
		JSONObject reportConfig = getConfig(properties.reportsJson);

		List<JSONObject> childReports = new ArrayList<JSONObject>();

		if ("ElisGeneric".equalsIgnoreCase(reportConfig.getJSONObject(name).getString("type"))) {
			JSONObject reportObj = new JSONObject();
			reportObj.put("name", reportConfig.getJSONObject(name).getString("name"));
			reportObj.put("type", reportConfig.getJSONObject(name).getString("type"));

			JSONObject configObj = new JSONObject();
			configObj.put("sqlPath", reportConfig.getJSONObject(name).getJSONObject("config").get("sqlPath"));

			reportObj.put("config", configObj);
			childReports.add(reportObj);

		} else {
			childReports = jsonArrayToList(
					reportConfig.getJSONObject(name).getJSONObject("config").getJSONArray("reports"));
		}

		JSONObject dhisConfig = getDHISConfig(name);
		ReportDateRange dateRange = new DateConverter().getDateRange(year, month);
		List<Object> programDataValue = getProgramDataValues(childReports, dhisConfig.getJSONObject("reports"),
				dateRange);

		JSONObject programDataValueSet = new JSONObject();
		programDataValueSet.put("orgUnit", dhisConfig.getString("orgUnit"));
		programDataValueSet.put("dataValues", programDataValue);
		programDataValueSet.put("period", format("%d%02d", year, month));

		ResponseEntity<String> responseEntity = dHISClient.post(SUBMISSION_ENDPOINT, programDataValueSet);
		submission.setPostedData(programDataValueSet);
		submission.setResponse(responseEntity);
		return submission;
	}

	private JSONObject getConfig(String configFile) throws DHISIntegratorException {
		try {
			return new JSONObject(new JSONTokener(new FileReader(configFile)));
		} catch (FileNotFoundException e) {
			throw new DHISIntegratorException(format(CONFIG_FILE_NOT_FOUND, configFile), e);
		}
	}

	private Results getResult(String sql, String type, ReportDateRange dateRange) throws DHISIntegratorException {
		String formattedSql = sql.replaceAll("#startDate#", dateRange.getStartDate()).replaceAll("#endDate#",
				dateRange.getEndDate());
		return databaseDriver.executeQuery(formattedSql, type);
	}

	private String getContent(String filePath) throws DHISIntegratorException {
		try {
			return Files.readAllLines(Paths.get(filePath)).stream().reduce((x, y) -> x + "\n" + y).get();
		} catch (IOException e) {
			throw new DHISIntegratorException(format(FILE_READING_EXCEPTION, filePath), e);
		}
	}

	private List<Object> getProgramDataValues(List<JSONObject> reportSqlConfigs, JSONObject reportDHISConfigs,
			ReportDateRange dateRange) throws DHISIntegratorException, JSONException, IOException {
		ArrayList<Object> programDataValues = new ArrayList<>();

		for (JSONObject report : reportSqlConfigs) {
			JSONArray dataValues = getReportDataElements(reportDHISConfigs, dateRange, report);
			programDataValues.addAll(jsonArrayToList(dataValues));
		}
		return programDataValues;
	}

	private List<Object> getProgramDataValuesAttrOptCombo(List<String> row, JSONObject childReport,
			JSONObject reportDHISConfigs, ReportDateRange dateRange)
			throws DHISIntegratorException, JSONException, SQLException {

		ArrayList<Object> programDataValues = new ArrayList<>();
		JSONArray dataValues = new JSONArray();
		dataValues = reportDHISConfigs.getJSONObject(childReport.getString("name")).getJSONArray("dataValues");
		for (Object dataValue_ : jsonArrayToList(dataValues)) {
			JSONObject dataValue = (JSONObject) dataValue_;
			updateDataElementsAtrOptCombo(row, dataValue);
		}
		programDataValues.addAll(jsonArrayToList(dataValues));
		return programDataValues;
	}

	private JSONArray getReportDataElements(JSONObject reportDHISConfigs, ReportDateRange dateRange, JSONObject report)
			throws DHISIntegratorException, JSONException, IOException {
		JSONArray dataValues = new JSONArray();
		try {
			dataValues = reportDHISConfigs.getJSONObject(report.getString("name")).getJSONArray("dataValues");
		} catch (JSONException e) {
			throw new DHISIntegratorException(e.getMessage(), e);
		}
		String sqlPath = report.getJSONObject("config").getString("sqlPath");
		String type = report.getString("type");
		Results results = getResult(getContent(sqlPath), type, dateRange);

		for (Object dataValue_ : jsonArrayToList(dataValues)) {
			JSONObject dataValue = (JSONObject) dataValue_;
			updateDataElements(results, dataValue);
		}
		return dataValues;
	}

	private List<JSONObject> jsonArrayToList(JSONArray elements) {
		List<JSONObject> list = new ArrayList<>();
		elements.forEach((element) -> list.add((JSONObject) element));
		return list;
	}

	private void updateDataElements(Results results, JSONObject dataElement) throws JSONException {
		String value = results.get(dataElement.getInt("row"), dataElement.getInt("column"));
		dataElement.put("value", value);
	}

	private void updateDataElementsAtrOptCombo(List<String> row, JSONObject dataElement) throws JSONException {
		String value = row.get(dataElement.getInt("column") + 1); // TODO: +1 column size
		dataElement.put("value", value);
	}

	private JSONObject getDHISConfig(String programName) throws DHISIntegratorException {
		String DHISConfigFile = properties.dhisConfigDirectory + programName.replaceAll(" ", "_") + ".json";
		return getConfig(DHISConfigFile);
	}
}
