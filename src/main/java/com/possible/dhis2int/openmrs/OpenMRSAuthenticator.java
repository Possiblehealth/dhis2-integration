package com.possible.dhis2int.openmrs;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class OpenMRSAuthenticator {

    private static final String WHOAMI_URL = "/bahmnicore/whoami";
    public static final String OPENMRS_SESSION_ID_COOKIE_NAME = "JSESSIONID";

    private final OpenmrsClient openmrsClient;
    
    @Autowired
    public OpenMRSAuthenticator(OpenmrsClient openmrsClient) {
        this.openmrsClient = openmrsClient;
    }
    
    public AuthenticationResponse authenticate(String sessionId) {
        if(sessionId == null){
            return AuthenticationResponse.NOT_AUTHENTICATED;
        }
        ResponseEntity<Privileges> response = openmrsClient.get(sessionId, WHOAMI_URL, Privileges.class);
        HttpStatus status = response.getStatusCode();

        if (status.series() == HttpStatus.Series.SUCCESSFUL) {
            return response.getBody().hasReportingPrivilege()?
                    AuthenticationResponse.AUTHORIZED:
                    AuthenticationResponse.UNAUTHORIZED;
        }

        return AuthenticationResponse.NOT_AUTHENTICATED;
    }
    
    public AuthenticationResponse authenticateReportSubmitingPrivilege(String sessionId) {
    	if(sessionId == null){
            return AuthenticationResponse.NOT_AUTHENTICATED;
        }
        ResponseEntity<Privileges> response = openmrsClient.get(sessionId, WHOAMI_URL, Privileges.class);
        HttpStatus status = response.getStatusCode();

        if (status.series() == HttpStatus.Series.SUCCESSFUL && response.getBody().hasReportingPrivilege()) {
            return response.getBody().hasReportSubmitingPrivilege()?
                    AuthenticationResponse.SUBMIT_AUTHORIZED:
                    AuthenticationResponse.SUBMIT_UNAUTHORIZED;
        }
        return AuthenticationResponse.NOT_AUTHENTICATED;
    }
    

    private static class Privileges extends ArrayList<Privilege> {
        boolean hasReportingPrivilege() {
            for (Privilege privilege : this) {
                if (privilege.isReportingPrivilege()) return true;
            }
            return false;
        }
        boolean hasReportSubmitingPrivilege() {
            for (Privilege privilege : this) {
                if (privilege.isReportSubmitingPrivilege()) return true;
            }
            return false;
        }
    }

    private static class Privilege {
        static final String VIEW_REPORTS_PRIVILEGE = "app:reports";
        static final String REPORT_SUBMIT_PRIVILEGE = "Submit DHIS Report";
        private String name;
        private void setName(String name) {
            this.name = name;
        }

        boolean isReportingPrivilege() {
            return name.equals(VIEW_REPORTS_PRIVILEGE);
        }
        
        boolean isReportSubmitingPrivilege() {
            return name.equals(REPORT_SUBMIT_PRIVILEGE);
        }
    }
}
