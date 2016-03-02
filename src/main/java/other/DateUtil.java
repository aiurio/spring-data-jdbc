package other;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A utility class related to dates. All new dates should
 * call {@code DateUtil#now()}.
 *
 */
public class DateUtil {

    private static DateTime stagedDate;

    public static void setStagedDate(DateTime date){
        stagedDate = date;
    }

    public static DateTime currentDateTime(){
        return new DateTime(now());
    }

    public static DateTime todayAtMidnight(DateTimeZone timezone){
        DateTime local = new DateTime(now(), timezone)
                .withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);

        return new DateTime(local, DateTimeZone.UTC);
    }

    public static DateTime now(){
        return stagedDate == null ? new DateTime() : stagedDate;
    }

    public static DateTime localNow(DateTimeZone timezone){
        timezone = timezone == null ? DateTimeZone.getDefault() : timezone;
        return stagedDate == null
                ? new DateTime(timezone)
                : stagedDate;
    }

    public static DateTime getLocalDateTime(DateTimeZone timezone){
        timezone = timezone == null ? DateTimeZone.getDefault() : timezone;
        return stagedDate == null
                ? new DateTime(timezone)
                : new DateTime(stagedDate);
    }

    public static DateTime startOfToday(DateTimeZone dateTimeZone){
        return new LocalDate(now()).toDateTimeAtStartOfDay();
    }

    public static LocalTime currentTime(){
        return new LocalTime();
    }


    public static LocalDate localDate(DateTimeZone timezone) {
        return new LocalDate(localNow(timezone));
    }


    public static LocalTime localTime(DateTimeZone timezone) {
        return new LocalTime(localNow(timezone));
    }

    public static final Parser parser = new Parser();
    public static final Formatter formatter = new Formatter();

    public static class Parser {
        public Date ymd(String rawDate){
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(rawDate);
            } catch (ParseException e) {
                throw new RuntimeException("Could not parse date: " + rawDate);
            }
        }
    }

    public static class Formatter {
        public String ymd(Date date){
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
    }
}
