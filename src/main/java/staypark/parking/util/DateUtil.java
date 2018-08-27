package staypark.parking.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtil {
	private static SimpleDateFormat DATE_FORMAT_WITH_DAY = new SimpleDateFormat("EEEE MMMM dd, yyyy", Locale.US);
	private static SimpleDateFormat DATE_FORMAT_YEAR_LAST = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

	public final static String[] PARKING_TIMES = {
			"12:00 AM", "12:15 AM", "12:30 AM", "12:45 AM", 
			"01:00 AM", "01:15 AM", "01:30 AM", "01:45 AM",
			"02:00 AM", "02:15 AM", "02:30 AM", "02:45 AM",
			"03:00 AM", "03:15 AM", "03:30 AM", "03:45 AM",
			"04:00 AM", "04:15 AM", "04:30 AM", "04:45 AM",
			"05:00 AM", "05:15 AM", "05:30 AM", "05:45 AM",
			"06:00 AM", "06:15 AM", "06:30 AM", "06:45 AM",
			"07:00 AM", "07:15 AM", "07:30 AM", "07:45 AM",
			"08:00 AM", "08:15 AM", "08:30 AM", "08:45 AM",
			"09:00 AM", "09:15 AM", "09:30 AM", "09:45 AM",
			"10:00 AM", "10:15 AM", "10:30 AM", "10:45 AM",
			"11:00 AM", "11:15 AM", "11:30 AM", "11:45 AM",
			"12:00 PM", "12:15 PM", "12:30 PM", "12:45 PM", 
			"01:00 PM", "01:15 PM", "01:30 PM", "01:45 PM",
			"02:00 PM", "02:15 PM", "02:30 PM", "02:45 PM",
			"03:00 PM", "03:15 PM", "03:30 PM", "03:45 PM",
			"04:00 PM", "04:15 PM", "04:30 PM", "04:45 PM",
			"05:00 PM", "05:15 PM", "05:30 PM", "05:45 PM",
			"06:00 PM", "06:15 PM", "06:30 PM", "06:45 PM",
			"07:00 PM", "07:15 PM", "07:30 PM", "07:45 PM",
			"08:00 PM", "08:15 PM", "08:30 PM", "08:45 PM",
			"09:00 PM", "09:15 PM", "09:30 PM", "09:45 PM",
			"10:00 PM", "10:15 PM", "10:30 PM", "10:45 PM",
			"11:00 PM", "11:15 PM", "11:30 PM", "11:45 PM"
	};
	
	public static Date unknownDateStringFormatToDate(final String dateStr) {
		Date date = null;
		try {
			date = stringMDYToDate(dateStr);
			return date;
		} catch(Exception e) {}
		
		try {
			date = longAssDateStringToDate(dateStr);
			return date;
		} catch(Exception e) {}
		
		return null;
	}
	
	public static Date stringMDYToDate(final String dateStr) throws ParseException {
		if (dateStr == null) {
			return null;
		}
		
		Date date = new SimpleDateFormat("MM-dd-yyyy").parse(dateStr);
		return date;
	}

	public static Date longAssDateStringToDate(final String dateStr) throws ParseException {
		if (dateStr == null) {
			return null;
		}
		
		Date date = new SimpleDateFormat("EEEE MMMM dd, yyyy").parse(dateStr);
		return date;
	}
	
	public static java.sql.Date utilDateToSqlDate(final Date date) {
		if (date == null) {
			return null;
		}
		
		return new java.sql.Date(date.getTime());
	}
	
	public static String formatDate(final Date date) {
		if (date != null) {
			return DATE_FORMAT_WITH_DAY.format(date);
		}
		return "";
	}

	public static String formatDateWithYearAtEnd(final Date date) {
		if (date != null) {
			return DATE_FORMAT_YEAR_LAST.format(date);
		}
		return "";
	}
	
	public static int getDaysDifference(final Date date1, final Date date2) { 
		if (date1 == null || date2 == null) {
			return 0;
		}
		long diff = date2.getTime() - date1.getTime();
	    return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	public static Date addDaysToDate(final Date date, final int days) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

	public static String getDayOfWeek(final Date date) {
		if (date == null) {
			return "";
		}
		DateFormat format = new SimpleDateFormat("EEEE");
		return format.format(date);
	}
	
	public static int getNumericalDayOfWeek(final Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek;
	}

	public static String parseTimeFromDateTimeString(final String dateTime) {
		if (dateTime == null || dateTime.trim().isEmpty()) {
			return "08:00 AM";
		}

		String[] arr = dateTime.split(" ");
		String time = arr[arr.length-2] + " " + arr[arr.length-1];

		return time;
	}

	public static String parseDateFromDateTimeString(final String dateTime) {
		if (dateTime == null || dateTime.trim().isEmpty()) {
			return "";
		}

		String[] arr = dateTime.split("-");
		String date = arr[1] + "-"+ arr[2] + "-" + arr[0];

		return date;
	}
}
