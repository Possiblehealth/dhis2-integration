package com.possible.dhis2int.audit;

import java.util.Date;



import com.possible.dhis2int.audit.Submission.Status;

public class Recordlog {
	  
	final static String HEADER = "Event,Time,User,Comment,Status";
	String event;
	Date time;
	String userId;
	String comment;
	Status status;
public Recordlog(String event,Date time, String userId, String comment, Status status) {
	this.event = event;
	this.time = time;
	this.userId = userId;
	this.comment = comment;
	this.status = status;
}

public String getEvent() {
	return event;
}
public void setEvent(String event) {
	this.event = event;
}
public Date getTime() {
	return time;
}
public void setTime(Date time) {
	this.time = time;
}
public String getUserId() {
	return userId;
}
public void setUserId(String userId) {
	this.userId = userId;
}
public String getComment() {
	return comment;
}
public void setComment(String comment) {
	this.comment = comment;
}
public Status getStatus() {
	return status;
}
public void setStatus(Status status) {
	this.status = status;
}
}
