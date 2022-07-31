package otaku.info.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.service.BlogTagService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class DateUtils {

    private final String[] dayOfWeek = {"", "日", "月", "火", "水", "木", "金", "土"};

    private final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    BlogTagService blogTagService;

    // https://www.delftstack.com/ja/howto/java/java-subtract-dates/
    public int dateDiff(Date firstDate, Date secondDate) {

        long diff = secondDate.getTime() - firstDate.getTime();

        TimeUnit time = TimeUnit.DAYS;
        return Math.toIntExact(time.convert(diff, TimeUnit.MILLISECONDS));
    }

    public Date daysAfterToday(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getToday());
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    /**
     *
     * @param strDate 2021/07/16 21:00
     * @return Date
     */
    public LocalDateTime stringToLocalDateTime(String strDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return LocalDateTime.parse(strDate, formatter);
    }

    public static Date getFirstDate(Date date) {

        if (date==null) return new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int first = calendar.getActualMinimum(Calendar.DATE);
        calendar.set(Calendar.DATE, first);

        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);
        calendar.set(Calendar.MILLISECOND, 000);

        return calendar.getTime();
    }

    // 月末日を返す
    public static Date getLastDate(Date date) {

        if (date==null) return new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int last = calendar.getActualMaximum(Calendar.DATE);
        calendar.set(Calendar.DATE, last);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    public Date getLatestDate(List<Date> dateList) {
        Date today = new Date();
        dateList.removeIf(d -> d.before(today));
        int i = 0;
        Date latest = dateList.get(i);
        for (Date d : dateList) {
            i++;
            latest = latest.before(dateList.get(i)) ? latest : dateList.get(i);
        }
        return latest;
    }

    public String getDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int index = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek[index];
    }

    public String getDay(String dateStr) throws ParseException {
        Date date = null;
        if (dateStr.matches("^\\d{8}$")) {
            date = sdf2.parse(dateStr);
        } else if (dateStr.matches("\\d{8}")) {
            String tmp = dateStr.replaceAll("[^\\d{8}]", "");
            if (tmp.length() > 8) {
                String tmp2 = tmp.substring(0,7);
                date = sdf2.parse(tmp2);
            }
        }
        if (date == null) {
            return "-";
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            int index = c.get(Calendar.DAY_OF_WEEK);
            return dayOfWeek[index];
        }
    }

    /**
     * 今日の日付（日にちだけ）を返します
     *
     * @return
     */
    public int getDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DATE);
    }

    public String getNextYYYYMM() {
        Calendar calendar = Calendar.getInstance();
        calendar.get(Calendar.MONTH);
        Date date = calendar.getTime();
        return sdf1.format(date);
    }

    public String getYYYYMM(Date date) {
        return sdf1.format(date);
    }

    /**
     * LocalDateTimeをDateに変換します
     *
     * @param localDateTime
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zone);
        Instant instant = zonedDateTime.toInstant();
        return Date.from(instant);
    }

    /**
     * 今日の0:00を返却します
     *
     * @return
     */
    public Date getToday() {
        // 商品を集めるため今日の日付を取得
        Date today = org.apache.commons.lang3.time.DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        return today;
    }

    /**
     * Unix timestampをDate型にして返却します
     *
     * @param unix10digits
     * @return
     */
    public static Date unixToDate(Long unix10digits) {
        return new Date(unix10digits * 1000);
    }

    public Long ldtToMilliseconds(LocalDateTime ldt) {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Dateオブジェクトからstringの日付を返します
     *
     * @param date
     * @return
     */
    public String getStringDateByDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    /**
     * StringからDateに変換して返す
     *
     * @param strDate
     * @param format
     * @return
     * @throws ParseException
     */
    public Date stringToDate(String strDate, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(strDate);
    }

    public LocalDateTime stringToLocalDateTime(String strDate, String format) {
        //DateTimeFormatterクラスのオブジェクトを生成
        DateTimeFormatter dtFt = DateTimeFormatter.ofPattern(format);
        //String型の日付からLocalDateTimeクラスのオブジェクトを生成
        LocalDateTime datePar = LocalDateTime.parse(strDate, dtFt);
        return datePar;
    }
}
