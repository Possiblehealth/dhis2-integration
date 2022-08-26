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
public class SchedulingConfiguration{

    @Scheduled(fixedRate = 2000L)
	public void scheduleDHISSubmissions()
			throws IOException, JSONException, DHISIntegratorException, Exception {
			System.out.println("Now is"+new Date());

	}

}