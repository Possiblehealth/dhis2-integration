package com.possible.dhis2int;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dhis-integration")
public class DHISIntegrator {
	
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public String sample(HttpServletResponse response) {
		return "hello";
	}
	
	@RequestMapping(path = "/months", method = RequestMethod.GET)
	public String months(HttpServletResponse response) {
		return "hello all";
	}
	
}
