package com.possible.dhis2int;

import java.io.IOException;
import java.util.Date;

import org.json.JSONException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.possible.dhis2int.web.DHISIntegrator;
import com.possible.dhis2int.web.DHISIntegratorException;

import ch.qos.logback.classic.Logger;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="scheduling.enabled",matchIfMissing = true)
public class DHISIntegratorScheduler{

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



}