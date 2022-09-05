package com.possible.dhis2int.scheduler;

public class AuthResponse {
	private String sessionId;
	private String sessionUser;

	AuthResponse(String sessionId, String sessionUser) {
		this.sessionId = sessionId;
		this.sessionUser = sessionUser;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setSessionUser(String sessionUser) {
		this.sessionUser = sessionUser;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public String getSessionUser() {
		return this.sessionUser;
	}
}
