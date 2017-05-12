package com.possible.dhis2int.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.possible.dhis2int.Properties;

@Service
public class SubmissionLog {
	
	private final File logFile;
	
	private final String FILE_NAME = "'DHIS2_submission_log' dd-MM-yyyy HH-mm'.csv'";
	
	private PrintWriter writer;
	
	@Autowired
	public SubmissionLog(Properties properties) throws IOException {
		logFile = new File(properties.submissionLogFileName);
		writer = new PrintWriter(new FileWriter(logFile, true), true);
		ensureHeaderExists();
	}
	
	public String getFileNameTimeStamp() {
		return DateTimeFormat.forPattern(FILE_NAME).print(new DateTime());
	}
	
	public FileSystemResource getFile() {
		return new FileSystemResource(logFile);
	}
	
	public void failure(String report, String userId, String comment, String dataSent) {
		writer.println(new Record(report + " Report Submission", new Date(), userId, comment, Status.Failure, dataSent));
	}
	
	public void success(String report, String userId, String comment, String dataSent) {
		writer.println(new Record(report + " Report Submission", new Date(), userId, comment, Status.Success, dataSent));
	}
	
	private void ensureHeaderExists() throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(logFile));
		String firstLine = bufferedReader.readLine();
		if (firstLine == null || firstLine.isEmpty()) {
			writer.println(Record.HEADER);
		}
		bufferedReader.close();
	}
	
	public enum Status {
		Success,
		Failure
	}
	
	public static class Record {
		
		final static String HEADER = "Event,Time,User,Comment,Status,DataFile";
		
		String event;
		
		Date time;
		
		String userId;
		
		String comment;
		
		Status status;
		
		String dataFile;
		
		public Record(String event, Date time, String userId, String comment, Status status, String dataFile) {
			this.event = event;
			this.time = time;
			this.userId = userId;
			this.comment = comment;
			this.status = status;
			this.dataFile = dataFile;
		}
		
		@Override
		public String toString() {
			return event + ',' + time + ',' + userId + ',' + comment + ',' + status + ',' + dataFile;
		}
	}
}
