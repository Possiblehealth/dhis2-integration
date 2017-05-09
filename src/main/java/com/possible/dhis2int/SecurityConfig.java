package com.possible.dhis2int;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.possible.dhis2int.openmrs.AuthenticationFilter;

@Configuration
public class SecurityConfig extends WebMvcConfigurerAdapter {
	
	private final AuthenticationFilter authenticationFilter;
	
	@Autowired
	public SecurityConfig(AuthenticationFilter authenticationFilter) {
		this.authenticationFilter = authenticationFilter;
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authenticationFilter);
	}
	
}
