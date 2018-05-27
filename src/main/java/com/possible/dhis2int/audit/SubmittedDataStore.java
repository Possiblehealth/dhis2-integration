package com.possible.dhis2int.audit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;


@Service
public class SubmittedDataStore {
	
	private Properties properties;
	
	@Autowired
	public SubmittedDataStore(Properties properties) {
		this.properties = properties;
	}
	
	public String write(Submission submission) throws IOException, JSONException {
		File file = new File(getAbsolutePath(submission));
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(submission.toStrings());
		fileWriter.close();
		return file.getAbsolutePath();
	}
	
	public String write(List<Submission> submissions) throws IOException, JSONException {
		Submission submission = new Submission();
		if(submissions.size() > 0) {
			submission = submissions.get(submissions.size()-1);
		}
		File file = new File(getAbsolutePath(submission));
		FileWriter fileWriter = new FileWriter(file);
		for (Submission submis : submissions) {
			fileWriter.write(submis.toStrings());
		}
		fileWriter.close();
		return file.getAbsolutePath();
	}
	
	public String getAbsolutePath(Submission submission) {
		String fileName = submission.getFileName();
		return properties.submissionAuditFolder + "/" + fileName;
	}
	
}
