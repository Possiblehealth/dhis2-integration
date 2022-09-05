package com.possible.dhis2int.scheduler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.possible.dhis2int.Properties;
import com.possible.dhis2int.db.DatabaseDriver;
import com.possible.dhis2int.db.Results;
import com.possible.dhis2int.web.DHISIntegrator;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Messages;
import com.possible.dhis2int.scheduler.Schedule;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.spi.DirStateFactory.Result;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", matchIfMissing = true)
@RestController
public class DHISIntegratorScheduler {

	private final DatabaseDriver databaseDriver;
	private final Logger logger = getLogger(DHISIntegratorScheduler.class);
	private final Properties properties;
	private final String SUBMISSION_ENDPOINT = "/dhis-integration/submit-to-dhis";
	private final String OPENMRS_LOGIN_ENDPOINT = "/session";

	@Autowired
	public DHISIntegratorScheduler(DatabaseDriver databaseDriver, Properties properties) {
		this.databaseDriver = databaseDriver;
		this.properties = properties;
	}

	@RequestMapping(path = "/get-schedules")
	public JSONArray getIntegrationSchedules(HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException, DHISIntegratorException, Exception {
		String sql = "SELECT id, report_name, frequency, last_run, status FROM dhis2_schedules;";
		JSONArray jsonArray = new JSONArray();
		ArrayList<Schedule> list = new ArrayList<Schedule>();
		Results results = new Results();
		String type = "MRSGeneric";
		Schedule schedule;
		ObjectMapper mapper;

		try {
			results = databaseDriver.executeQuery(sql, type);

			for (List<String> row : results.getRows()) {
				logger.info(row);
				schedule = new Schedule();

				schedule.setId(Integer.parseInt(row.get(0)));
				schedule.setProgName(row.get(1));
				schedule.setFrequency(row.get(2));
				schedule.setLastRun(row.get(3));
				schedule.setStatus(row.get(4));
				list.add(schedule);

			}
			mapper = new ObjectMapper();
			String jsonstring = mapper.writeValueAsString(list);
			jsonArray.put(jsonstring);
			logger.info("Inside loadIntegrationSchedules...");
		} catch (DHISIntegratorException | JSONException e) {
			// logger.info("Inside loadIntegrationSchedules...");
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return jsonArray;

	}

	@RequestMapping(path = "/create-schedule")
	public Results createIntegrationSchedule(@RequestParam("programName") String progName,
			@RequestParam("scheduleFrequency") String schedFrequency,
			@RequestParam("scheduleTime") String schedTime, HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		Schedule newschedule = new Schedule();
		newschedule.setProgName(progName);
		newschedule.setFrequency(schedFrequency);
		newschedule.setCreatedBy("Test");

		LocalDate created_date = LocalDate.now();
		LocalDate target_time = LocalDate.now();
		newschedule.setCreatedDate(created_date);
		newschedule.setTargetTime(target_time);

		Results results = new Results();
		logger.info("Inside saveIntegrationSchedules...");
		try {
			databaseDriver.executeUpdateQuery(newschedule);
			logger.info("Executed insert query successfully...");

		} catch (DHISIntegratorException | JSONException e) {
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return results;
	}

	@RequestMapping(path = "/delete-schedule")
	public Results deletIntegrationSchedule(@RequestParam(value = "scheduleIds[]") String scheduleIds[],
			HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException {
		Results results = new Results();
		logger.info("Inside deleteIntegrationSchedules..., schedule_id_0=" + scheduleIds[0]);
		try {
			for (String schedule_id : scheduleIds) {
				databaseDriver.executeDeleteQuery(Integer.parseInt(schedule_id));
				logger.info("Executed delete query successfully...");
			}

		} catch (DHISIntegratorException | JSONException e) {
			logger.error(Messages.SQL_EXECUTION_EXCEPTION, e);
		} catch (Exception e) {
			logger.error(Messages.INTERNAL_SERVER_ERROR, e);
		}

		return results;
	}

	private String getAuthToken(String username, String password) {
		Charset UTF_8 = Charset.forName("UTF-8");
		String authToken = Base64Utils.encodeToString((username + ":" + password).getBytes(UTF_8));
		return authToken;
	}

	/*
	 * This method builds the DHISIntegrator url (rest api endpoint and parameters)
	 */
	private String buildDHISIntegratorUrl(String reportName, Integer month, Integer year, String comment) {
		StringBuilder DHISIntegratorRequestUrl = new StringBuilder(
				properties.dhisIntegratorRootUrl + SUBMISSION_ENDPOINT);
		DHISIntegratorRequestUrl.append("?name=").append(reportName).append("&month=").append(month).append("&year=")
				.append(year)
				.append("&isImam=false&isFamily=false").append("&comment=").append(comment);
		return DHISIntegratorRequestUrl.toString();
	}

	/*
	 * Method to authenticate/login daemon user against OpenMRS
	 * Return an empty Authentication response (sessionid and session user) if auth
	 * fails
	 * Else it returns a valid sessionid and session username
	 */
	private AuthResponse authenticate(String openmrsUrl) {
		String authToken = getAuthToken(properties.dhisIntegratorSchedulerUser,
				properties.dhisIntegratorSchedulerPassword);

		HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(openmrsUrl,
				HttpMethod.GET, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		// logger.info("Openmrs get session response: " + responseEntity.toString());
		// logger.info("Response headers: " + responseEntity.getHeaders().toString());
		Boolean authenticated = new JSONObject(new JSONTokener(responseEntity.getBody()))
				.getBoolean("authenticated");
		AuthResponse authResponse = new AuthResponse("", "");
		if (authenticated) {
			authResponse.setSessionId(new JSONObject(new JSONTokener(responseEntity.getBody())).getString("sessionId"));
			authResponse.setSessionUser(new JSONObject(new JSONTokener(responseEntity.getBody())).getJSONObject("user")
					.getString("username"));
			logger.info("Daemon user " + properties.dhisIntegratorSchedulerUser + " autheticated successfully");
		} else {
			logger.warn("Daemon user " + properties.dhisIntegratorSchedulerUser
					+ " NOT autheticated. Correct the credentials on the properties file (application.yml)");
		}
		return authResponse;
	}

	/*
	 * Log out deamon user from OpenMRS
	 */
	private void logout(String openmrsUrl) {
		String authToken = getAuthToken(properties.dhisIntegratorSchedulerUser,
				properties.dhisIntegratorSchedulerPassword);

		HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(openmrsUrl,
				HttpMethod.DELETE, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		logger.info("Openmrs delete session response: " + responseEntity.toString());
	}

	/*
	 * This method makes a rest call to DHISIntegrator which in turn submits to
	 * DHIS2 by making
	 * a rest call to the DHIS2 rest api
	 */
	private ResponseEntity<String> submitToDHISIntegrator(String DHISIntegratorUrl, AuthResponse authResponse) {
		HttpHeaders DHISIntegratorAuthHeaders = new HttpHeaders();
		DHISIntegratorAuthHeaders.add("Cookie", "JSESSIONID=" + authResponse.getSessionId());
		DHISIntegratorAuthHeaders.add("Cookie", "reporting_session=" + authResponse.getSessionId());
		DHISIntegratorAuthHeaders.add("Cookie", "bahmni.user=" + authResponse.getSessionUser());

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(DHISIntegratorUrl, HttpMethod.GET,
				new HttpEntity<String>(DHISIntegratorAuthHeaders),
				String.class);

		logger.info("Status code: " + responseEntity.getStatusCode() + "when firing query - GET: " + DHISIntegratorUrl);
		return responseEntity;
	}

	@Scheduled(fixedDelay = 30000)
	public void processSchedules() {
		// read from table
		// for each row, is this report due
		// if due => call submitToDHIS(x,y,z)
		logger.info("Executing schedule at :" + new Date().toString());

		// get from DB

		Integer month = 6;
		Integer year = 2020;
		String reportName = "TESTS-01 DHIS Integration App Sync Test";
		String comment = "Submitted by daemon";

		String DHISIntegratorUrl = buildDHISIntegratorUrl(reportName, month, year, comment);
		String openmrsUrl = properties.openmrsRootUrl + OPENMRS_LOGIN_ENDPOINT;
		System.out.println("DHISIntegrator url: " + DHISIntegratorUrl);

		AuthResponse authResponse = authenticate(openmrsUrl);

		//
		if (authResponse.getSessionId() != "") {
			submitToDHISIntegrator(DHISIntegratorUrl, authResponse);
		}
		logout(openmrsUrl);
		// logout when done
	}

	@Scheduled(cron = "0 0/5 * * * *")
	public void runDailyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the daily task. Now is" + new Date());

	}

	@Scheduled(cron = "59 59 23 * * 0")
	public void runWeeklyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the weekly task. Now is" + new Date());

	}

	@Scheduled(cron = "59 59 23 28-31 * *")
	public void runMonthlyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the monthly task. Now is" + new Date());

	}

	@Scheduled(cron = "59 59 23 28-31 3,6,9,12 *")
	public void runQuarterlyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
		System.out.println("Firing the quarterly task. Now is" + new Date());

	}

	/*
	 * public ArrayList<Schedules> getDueReports(String period){
	 * ArrayList<Schedules> list=new ArrayList<Schedules>();
	 * String
	 * sql="SELECT id, report_name FROM dhis2_schedules where frequency='"+period+
	 * "';";
	 * String type="MRSGeneric";
	 * Schedules schedule;
	 * Results results=new Results();
	 * 
	 * try{
	 * results = databaseDriver.executeQuery(sql,type);
	 * 
	 * for (List<String> row : results.getRows()) {
	 * logger.info(row);
	 * schedule=new Schedules();
	 * 
	 * schedule.setId(Integer.parseInt(row.get(0)));
	 * schedule.setProgName(row.get(1));
	 * list.add(schedule);
	 * 
	 * }
	 * }
	 * catch(DHISIntegratorException | JSONException e){
	 * logger.error(Messages.SQL_EXECUTION_EXCEPTION,e);
	 * }
	 * catch(Exception e){
	 * logger.error(Messages.INTERNAL_SERVER_ERROR,e);
	 * }
	 * 
	 * 
	 * return list;
	 * 
	 * }
	 */

}