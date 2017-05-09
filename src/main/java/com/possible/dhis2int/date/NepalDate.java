package com.possible.dhis2int.date;

public class NepalDate {
	
	private int day;
	
	private int year;
	
	private int month;
	
	public NepalDate(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getDay() {
		return day;
	}
	
	public void setDay(int day) {
		this.day = day;
	}
	
	public int getMonth() {
		return month;
	}
	
	public void setMonth(int month) {
		this.month = month;
	}
	
	public String period() {
		return String.valueOf(year) + String.valueOf(month);
	}
}