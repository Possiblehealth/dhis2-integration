package com.possible.dhis2int;

import java.util.Calendar;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * Copied from https://github.com/keyrunHORNET/date_picker_converter/find/master
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
	
	public NepalDate getNepalDate(DateTime date){
		return getNepaliDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
	}
	
	public DateTime getStartDateOfMonth(DateTime date){
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
			
			int startingDayOfWeek = Calendar.SATURDAY; // 2009/1/1 is Saturday
			
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
	
}