package com.possible.dhis2int.scheduler;

public class MonthlyPeriod {
	private Integer month;
	private Integer year;

	MonthlyPeriod(Integer month, Integer year) {
		this.month = month;
		this.year = year;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMonth() {
		return this.month;
	}

	public Integer getYear() {
		return this.year;
	}
}
