package otaku.info.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    // https://www.delftstack.com/ja/howto/java/java-subtract-dates/
    public static int dateDiff(Date firstDate, Date secondDate) {

        long diff = secondDate.getTime() - firstDate.getTime();

        TimeUnit time = TimeUnit.DAYS;
        return Math.toIntExact(time.convert(diff, TimeUnit.MILLISECONDS));
    }
}
