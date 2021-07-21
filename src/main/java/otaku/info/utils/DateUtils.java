package otaku.info.utils;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class DateUtils {

    // https://www.delftstack.com/ja/howto/java/java-subtract-dates/
    public int dateDiff(Date firstDate, Date secondDate) {

        long diff = secondDate.getTime() - firstDate.getTime();

        TimeUnit time = TimeUnit.DAYS;
        return Math.toIntExact(time.convert(diff, TimeUnit.MILLISECONDS));
    }

    public Date daysAfterToday(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
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
}
