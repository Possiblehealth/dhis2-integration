package com.possible.dhis2int.security;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.possible.dhis2int.Properties;

@Component(value = "authenticationFilter")
public class AuthenticationFilter extends HandlerInterceptorAdapter {

    private static final String DHIS_INTEGRATION_COOKIE_NAME = "reporting_session";
    private OpenMRSAuthenticator authenticator;
    private Properties properties;

    @Autowired
    public AuthenticationFilter(OpenMRSAuthenticator authenticator,
                                Properties properties) {
        this.authenticator = authenticator;
        this.properties = properties;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Reports application cannot handle url " + request.getRequestURI());
            return false;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return redirectToLogin(request, response);
        }
        AuthenticationResponse authenticationResponse = AuthenticationResponse.NOT_AUTHENTICATED;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(DHIS_INTEGRATION_COOKIE_NAME)) {
                authenticationResponse = authenticator.authenticate(cookie.getValue());
            }
        }

        switch (authenticationResponse) {
            case AUTHORIZED:
                return true;
            case UNAUTHORIZED:
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Privileges is required to access reports");
                return false;
            default:
                return redirectToLogin(request, response);
        }
    }

    private boolean redirectToLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        httpServletResponse.getWriter().write("Please login to continue");

        StringBuffer redirectUrl = new StringBuffer();
        redirectUrl.append(properties.bahmniLoginUrl);
        char paramChar = '?';
        if(redirectUrl.toString().contains("?")) {
            paramChar = '&';
        }
        redirectUrl.append(paramChar)
                .append("from=")
                .append(URLEncoder.encode(httpServletRequest.getRequestURL().toString(), "UTF-8"));
        httpServletResponse.sendRedirect(redirectUrl.toString());
        return false;
    }
}
