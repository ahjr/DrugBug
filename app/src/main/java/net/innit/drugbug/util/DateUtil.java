package net.innit.drugbug.util;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * ** NOT IN USE **
 * Here in case we want to add support for date input in the future
 * Class to enable more options for date input
 */
public class DateUtil {
    // If I want to make this truly generic, I'd need to add support for locale
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{2}$", "MM-dd-yy");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "MM-dd-yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^\\d{1,2}/\\d{1,2}/\\d{2}$", "MM/dd/yy");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^[a-z]{3}\\s\\d{1,2}\\s\\d{4}$", "MMM dd yyyy");
        put("^[a-z]{3}\\s\\d{1,2},\\s\\d{4}$", "MMM dd, yyyy");
        put("^[a-z]{4,}\\s\\d{1,2}\\s\\d{4}$", "MMMM dd yyyy");
        put("^[a-z]{4,}\\s\\d{1,2},\\s\\d{4}$", "MMMM dd, yyyy");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "MM-dd-yyyy HH:mm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "MM-dd-yyyy HH:mm a");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "yyyy-MM-dd HH:mm a");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "MM/dd/yyyy HH:mm a");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "yyyy/MM/dd HH:mm a");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "dd MMM yyyy HH:mm a");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "dd MMMM yyyy HH:mm a");
        put("^[a-z]{3}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "MMM dd yyyy HH:mm");
        put("^[a-z]{3}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "MMM dd yyyy HH:mm a");
        put("^[a-z]{3}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "MMM dd, yyyy HH:mm");
        put("^[a-z]{3}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "MMM dd, yyyy HH:mm a");
        put("^[a-z]{4,}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "MMMM dd yyyy HH:mm");
        put("^[a-z]{4,}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "MMMM dd yyyy HH:mm a");
        put("^[a-z]{4,}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "MMMM dd, yyyy HH:mm");
        put("^[a-z]{4,}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}\\s[APap][Mm]$", "MMMM dd, yyyy HH:mm a");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM-dd-yyyy HH:mm:ss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "MM-dd-yyyy HH:mm:ss a");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "yyyy-MM-dd HH:mm:ss a");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "MM/dd/yyyy HH:mm:ss a");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "yyyy/MM/dd HH:mm:ss a");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "dd MMM yyyy HH:mm:ss a");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "dd MMMM yyyy HH:mm:ss a");
        put("^[a-z]{3}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MMM dd yyyy HH:mm:ss");
        put("^[a-z]{3}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "MMM dd yyyy HH:mm:ss a");
        put("^[a-z]{3}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MMM dd, yyyy HH:mm:ss");
        put("^[a-z]{3}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "MMM dd, yyyy HH:mm:ss a");
        put("^[a-z]{4,}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MMMM dd yyyy HH:mm:ss");
        put("^[a-z]{4,}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "MMMM dd yyyy HH:mm:ss a");
        put("^[a-z]{4,}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MMMM dd, yyyy HH:mm:ss");
        put("^[a-z]{4,}\\s\\d{1,2},\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[APap][Mm]$", "MMMM dd, yyyy HH:mm:ss a");
    }};

    /**
     * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
     * format is unknown. You can simply extend DateUtil with more formats if needed.
     *
     * @param dateString The date string to determine the SimpleDateFormat pattern for.
     * @return The matching SimpleDateFormat pattern, or null if format is unknown.
     * @see SimpleDateFormat
     */
    public static String determineDateFormat(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }
        return null; // Unknown format.
    }
}
