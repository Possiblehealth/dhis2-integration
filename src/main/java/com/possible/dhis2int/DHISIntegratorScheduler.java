package com.possible.dhis2int;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.possible.dhis2int.db.DatabaseDriver;
import com.possible.dhis2int.db.Results;
import com.possible.dhis2int.web.DHISIntegrator;
import com.possible.dhis2int.web.DHISIntegratorException;
import com.possible.dhis2int.web.Schedules;

import ch.qos.logback.classic.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.spi.DirStateFactory.Result;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="scheduling.enabled",matchIfMissing = true)
public class DHISIntegratorScheduler{

	//private final DatabaseDriver databaseDriver;

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