package com.possible.dhis2int.date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class ReportDateRange {
	
	private final DateTime startDate;
	
	private final DateTime endDate;
	
	public ReportDateRange(DateTime startDate, DateTime endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public String getStartDate() {
		return DateTimeFormat.forPattern("yyyy-MM-dd").print(startDate);
	}
	
	public String getEndDate() {
		return DateTimeFormat.forPattern("yyyy-MM-dd").print(endDate);
	}

	@Override
	public String toString() {
		return "ReportDateRange [startDate=" + startDate + ", endDate=" + endDate + "]";
	}
	
}
