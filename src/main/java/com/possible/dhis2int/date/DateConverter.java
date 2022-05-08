package com.possible.dhis2int.date;

import java.util.Calendar;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Copied from https://github.com/keyrunHORNET/date_picker_converter
 * /blob/master/library/src/main/java/com/hornet/dateconverter/DateConverter.java
 */
public class DateConverter {
	
	HashMap<Integer, int[]> daysInMonthMap = new HashMap<>();
	
	public DateConverter() {
	    /*
	    The 0s at index 0 are dummy values so as to make the int array of
        days in months seems more intuitive that index 1 refers to first
        month "Baisakh", index 2 refers to second month "Jesth" and so on.
         */

		daysInMonthMap.put(2065, new int[] { 0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31 });
		daysInMonthMap.put(2066, new int[] { 0, 31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31 });
		daysInMonthMap.put(2067, new int[] { 0, 31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2068, new int[] { 0, 31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2069, new int[] { 0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31 });
		daysInMonthMap.put(2070, new int[] { 0, 31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30 });
		daysInMonthMap.put(2071, new int[] { 0, 31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2072, new int[] { 0, 31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2073, new int[] { 0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31 });
		daysInMonthMap.put(2074, new int[] { 0, 31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2075, new int[] { 0, 31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2076, new int[] { 0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30 });
		daysInMonthMap.put(2077, new int[] { 0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31 });
		daysInMonthMap.put(2078, new int[] { 0, 31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2079, new int[] { 0, 31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30 });
		daysInMonthMap.put(2080, new int[] { 0, 31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30 });
		daysInMonthMap.put(2081, new int[] { 0, 31, 31, 32, 32, 31, 30, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2082, new int[] { 0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2083, new int[] { 0, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2084, new int[] { 0, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2085, new int[] { 0, 31, 32, 31, 32, 30, 31, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2086, new int[] { 0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2087, new int[] { 0, 31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2088, new int[] { 0, 30, 31, 32, 32, 30, 31, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2089, new int[] { 0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30 });
		daysInMonthMap.put(2090, new int[] { 0, 30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30 });
		
	}
	
	/*check if english date is in the range of conversion*/
	public static boolean isEngDateInRange(int yy, int mm, int dd) {
		return (yy >= 2009 && yy <= 2033) && (mm >= 1 && mm <= 12) && (dd >= 1 && dd <= 31);
	}
	
	/*check if nepali date is in the range of conversion*/
	public static boolean isNepDateInRange(int yy, int mm, int dd) {
		return (yy >= 2065 && yy <= 2090) && (mm >= 1 && mm <= 12) && (dd >= 1 && dd <= 32);
	}
	
	/*calculate whether english year is leap year or not*/
	public static boolean isLeapYear(int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
	}
	
	public NepalDate getNepalDate(DateTime date) {
		return getNepaliDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
	}
	
	public DateTime getStartDateOfMonth(DateTime date) {
		NepalDate nepalDate = getNepalDate(date);
		int[] months = daysInMonthMap.get(nepalDate.getYear());
		int days = months[nepalDate.getMonth()];
		return date.minusDays(days);
	}
	
	/*convert english date into nepali date*/
	private NepalDate getNepaliDate(int engYY, int engMM, int engDD) {
		
		if (isEngDateInRange(engYY, engMM, engDD)) {
			
			int startingEngYear = 2009;
			int startingEngMonth = 1;
			int startingEngDay = 1;
			
			int startingDayOfWeek = Calendar.THURSDAY;
			
			int startingNepYear = 2065;
			int startingNepMonth = 9;
			int startingNepDay = 17;
			
			int nepYY, nepMM, nepDD;
			int dayOfWeek = startingDayOfWeek;
			
			NepalDate tempNepalDate;
			
			//Calendar currentEngDate = new GregorianCalendar();
			//currentEngDate.set(engYY, engMM, engDD);
			
			//Calendar baseEngDate = new GregorianCalendar();
			//baseEngDate.set(startingEngYear, startingEngMonth, startingEngDay);
			
			// long totalEngDaysCount = daysBetween(baseEngDate, currentEngDate);
			// Log.d("KG: DateConverter", "TotalDaysCount: " + totalEngDaysCount);

           /*calculate the days between two english date*/
			DateTime base = new DateTime(startingEngYear, startingEngMonth, startingEngDay, 0, 0); // June 20th, 2010
			DateTime newDate = new DateTime(engYY, engMM, engDD, 0, 0); // July 24th
			long totalEngDaysCount = Days.daysBetween(base, newDate).getDays();
			
			//Log.d("KG: DateConverter", "TotalDaysCount: JODA " + totalEngDaysCount);
			
			nepYY = startingNepYear;
			nepMM = startingNepMonth;
			nepDD = startingNepDay;
			
			while (totalEngDaysCount != 0) {
				int daysInMonth = daysInMonthMap.get(nepYY)[nepMM];
				nepDD++;
				if (nepDD > daysInMonth) {
					nepMM++;
					nepDD = 1;
				}
				if (nepMM > 12) {
					nepYY++;
					nepMM = 1;
				}
				dayOfWeek++;
				if (dayOfWeek > 7) {
					dayOfWeek = 1;
				}
				totalEngDaysCount--;
			}
			tempNepalDate = new NepalDate(nepYY, nepMM, nepDD);
			
			return tempNepalDate;
		} else {
			throw new IllegalArgumentException("Out of Range: Date is out of range to Convert");
		}
	}
	
	public ReportDateRange getDateRange(Integer year, Integer month) {
		int lastDay = daysInMonthMap.get(year)[month];
		DateTime startDate = getEnglishDate(year, month, 1);
		DateTime endDate = getEnglishDate(year, month, lastDay);
		return new ReportDateRange(startDate, endDate);
	}
	
	public ReportDateRange getDateRangeForFiscalYear(Integer startYear, Integer startMonth, Integer endYear, Integer endMonth) {
		int lastDay = daysInMonthMap.get(endYear)[endMonth];
		DateTime startDate = getEnglishDate(startYear, startMonth, 1);
		DateTime endDate = getEnglishDate(endYear, endMonth, lastDay);
		return new ReportDateRange(startDate, endDate);
	}
	
	/*convert nepali date into english date*/
	public DateTime getEnglishDate(int nepYY, int nepMM, int nepDD) {
		
		if (isNepDateInRange(nepYY, nepMM, nepDD)) {
			
			int startingEngYear = 2008;
			int startingEngMonth = 4;
			int startingEngDay = 13;
			
			int startingDayOfWeek = Calendar.SUNDAY; // 2000/1/1 is Wednesday
			
			int startingNepYear = 2065;
			int startingNepMonth = 1;
			int startingNepDay = 1;
			
			int engYY, engMM, engDD;
			long totalNepDaysCount = 0;
			
			//count total no of days in nepali year from our starting range
			for (int i = startingNepYear; i < nepYY; i++) {
				for (int j = 1; j <= 12; j++) {
					totalNepDaysCount = totalNepDaysCount + daysInMonthMap.get(i)[j];
				}
			}
			
			//Log.d("KG: BS->AD :toYDayCount", "" + totalNepDaysCount);
			
			//count total days in terms of month
			for (int j = startingNepMonth; j < nepMM; j++) {
				totalNepDaysCount = totalNepDaysCount + daysInMonthMap.get(nepYY)[j];
			}
			//Log.d("KG: BS->AD :toMDayCount", "" + totalNepDaysCount);
			
			//count total days in terms of date
			totalNepDaysCount += nepDD - startingNepDay;
			//Log.d("KG: BS->AD :toDDayCount", "" + totalNepDaysCount);
			
			int[] daysInMonth = new int[] { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
			int[] daysInMonthOfLeapYear = new int[] { 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
			engYY = startingEngYear;
			engMM = startingEngMonth;
			engDD = startingEngDay;
			int endDayOfMonth = 0;
			int dayOfWeek = startingDayOfWeek;
			while (totalNepDaysCount != 0) {
				if (isLeapYear(engYY)) {
					endDayOfMonth = daysInMonthOfLeapYear[engMM];
				} else {
					endDayOfMonth = daysInMonth[engMM];
				}
				engDD++;
				dayOfWeek++;
				
				//Log.d("KG: BS->AD :engDD",""+engDD);
				if (engDD > endDayOfMonth) {
					engMM++;
					engDD = 1;
					//Log.d("KG: BS->AD :engMM", "" + engMM);
					if (engMM > 12) {
						engYY++;
						engMM = 1;
					}
				}
				//Log.d("KG: BS->AD :engYY",""+engYY);
				if (dayOfWeek > 7) {
					dayOfWeek = 1;
				}
				//Log.d("KG: BS->AD :totDayCount",""+totalNepDaysCount);
				totalNepDaysCount--;
			}
			return new DateTime(engYY, engMM, engDD, 0, 0);
		} else {
			throw new IllegalArgumentException("Out of Range: Date is out of range to Convert");
		}
	}
	
}