package com.possible.dhis2int.scheduler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.possible.dhis2int.db.DatabaseDriver;
import com.possible.dhis2int.db.Results;
import com.possible.dhis2int.web.DHISIntegrator;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Messages;
import com.possible.dhis2int.web.Schedules;

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
@ConditionalOnProperty(name="scheduling.enabled",matchIfMissing = true)
public class DHISIntegratorScheduler{

	private  final DatabaseDriver databaseDriver;
    private final Logger logger = getLogger(DHISIntegratorScheduler.class);

    @Autowired
	public DHISIntegratorScheduler(DatabaseDriver databaseDriver) {
		this.databaseDriver = databaseDriver;
	}


    @RequestMapping(path = "/get-schedules")
	public JSONArray getIntegrationSchedules(HttpServletRequest clientReq, HttpServletResponse clientRes)
			throws IOException, JSONException, DHISIntegratorException, Exception {
		String sql = "SELECT id, report_name, frequency, last_run, status FROM dhis2_schedules;";
		JSONArray jsonArray = new JSONArray();
		ArrayList<Schedules> list = new ArrayList<Schedules>();
		Results results = new Results();
		String type = "MRSGeneric";
		Schedules schedule;
		ObjectMapper mapper;

		try {
			results = databaseDriver.executeQuery(sql, type);

			for (List<String> row : results.getRows()) {
				logger.info(row);
				schedule = new Schedules();

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
		Schedules newschedule = new Schedules();
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


	@Scheduled(cron="0 0/5 * * * *")
	public void scheduleDailyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
			System.out.println("Firing the daily task. Now is"+new Date());

	}

    @Scheduled(cron="59 59 23 * * 0")
	public void scheduleWeeklyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
			System.out.println("Firing the weekly task. Now is"+new Date());
			

	}

	@Scheduled(cron="59 59 23 28-31 * *")
	public void scheduleMonthlyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
			System.out.println("Firing the monthly task. Now is"+new Date());
			

	}

	@Scheduled(cron="59 59 23 28-31 3,6,9,12 *")
	public void scheduleQuarterlyDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
			System.out.println("Firing the quarterly task. Now is"+new Date());

	}

	/*public ArrayList<Schedules> getDueReports(String period){
		ArrayList<Schedules> list=new ArrayList<Schedules>();
		String sql="SELECT id, report_name FROM dhis2_schedules where frequency='"+period+"';";
		String type="MRSGeneric";
		Schedules schedule;
		Results results=new Results();

		try{
				results = databaseDriver.executeQuery(sql,type);

				for (List<String> row : results.getRows()) {
					logger.info(row);
					schedule=new Schedules();
					
					schedule.setId(Integer.parseInt(row.get(0)));
					schedule.setProgName(row.get(1));
					list.add(schedule);
					
				}
		}
		catch(DHISIntegratorException | JSONException e){
			logger.error(Messages.SQL_EXECUTION_EXCEPTION,e);
		}
		catch(Exception e){
			logger.error(Messages.INTERNAL_SERVER_ERROR,e);
		}
		

		return list;

	}*/



}