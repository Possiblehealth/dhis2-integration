package com.possible.dhis2int.openmrs;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.possible.dhis2int.web.Cookies;
import com.possible.dhis2int.Properties;

@Component(value = "authenticationFilter")
public class AuthenticationFilter extends HandlerInterceptorAdapter {
    
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

        Cookies cookies = new Cookies(request);
        String cookie = cookies.getValue(Cookies.DHIS_INTEGRATION_COOKIE_NAME);
        
        AuthenticationResponse authenticationResponse = request.getRequestURI().contains("submit") ? authenticator.authenticateReportSubmitingPrivilege(cookie) : authenticator.authenticate(cookie);

        switch (authenticationResponse) {
            case AUTHORIZED:
                return true;
            case UNAUTHORIZED:
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Privileges is required to access reports");
                return false;
            case SUBMIT_AUTHORIZED:
            	return true;
            case SUBMIT_UNAUTHORIZED:
            	response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Privileges is required to submit this report");
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
