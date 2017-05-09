package com.possible.dhis2int.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class Cookies {
	
	public final static String BAHMNI_USER = "bahmni.user";
	
	public final static String DHIS_INTEGRATION_COOKIE_NAME = "reporting_session";
	
	private final Cookie[] cookies;
	
	public Cookies(HttpServletRequest request) {
		this.cookies = request.getCookies();
	}
	
	public String getValue(Object key) {
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(key)) {
				try {
					return URLDecoder.decode(cookie.getValue(), "UTF-8");
				}
				catch (UnsupportedEncodingException e) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}
