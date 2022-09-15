package com.possible.dhis2int.audit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.ResponseEntity;

public class Submission {

	public static final String FILE_NAME = "'data_submitted_on_'yyyyMMddHHmmss'.json'";

	private final String fileName;

	private JSONObject postedData;

	private ResponseEntity<String> response;

	private Exception exception;

	private Status status;

	private final static Integer INDENT_FACTOR = 1;

	public Submission() {
		this.fileName = DateTimeFormat.forPattern(FILE_NAME).print(new DateTime());
	}

	public String toStrings() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("response", response == null ? null : response.getBody());
		jsonObject.put("exception", exception);
		jsonObject.put("request", postedData);
		return jsonObject.toString(INDENT_FACTOR);
	}

	public ResponseEntity<String> getResponseEntity() {
		return response;
	}

	public String getFileName() {
		return fileName;
	}

	public JSONObject getPostedData() {
		return postedData;
	}

	public void setPostedData(JSONObject postedData) {
		this.postedData = postedData;
	}

	public void setResponse(ResponseEntity<String> response) {
		this.response = response;
	}

	public void setStatus(Status stat) {
		this.status = stat;
	}

	public Status retrieveStatus() {
		return status;
	}

	public Status getStatus() throws JSONException {
		if (response == null || response.getStatusCodeValue() != 200) {
			return Status.Failure;
		}
		JSONObject responseBody;
		try {
			responseBody = new JSONObject(new JSONTokener(response.getBody()));
		} catch (JSONException e) {
			return Status.Failure;
		}
		if (isServerError(responseBody) || isIgnored(responseBody) || hasConflicts(responseBody)) {
			return Status.Failure;
		}
		return Status.Success;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getInfo() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", getStatus());
		jsonObject.put("exception", exception);
		jsonObject.put("response", response == null ? null : response.getBody());
		return jsonObject.toString(INDENT_FACTOR);
	}

	private boolean hasConflicts(JSONObject responseBody) {
		// return responseBody.has("conflicts");
		return responseBody.getJSONArray("conflicts").length() > 0;
	}

	private boolean isIgnored(JSONObject responseBody) throws JSONException {
		return responseBody.getJSONObject("importCount").getInt("ignored") > 0;
	}

	private boolean isServerError(JSONObject responseBody) throws JSONException {
		return "ERROR".equals(responseBody.getString("status"));
	}

	public enum Status {
		Success,
		Failure,
		Complete,
		Incomplete
	}
}
