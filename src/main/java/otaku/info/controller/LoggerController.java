package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import otaku.info.setting.Log4jUtils;

@Controller
@AllArgsConstructor
public class LoggerController {

    final Logger blogCatchupTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("BlogCatchupTasklet");

    final Logger blogUpdateTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("BlogUpdateTasklet");

    final Logger futureItemReminderTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("FutureItemReminderTasklet");

    final Logger itemSearchMemberTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("ItemSearchMemberTasklet");

    final Logger itemSearchTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("ItemSearchTasklet");

    final Logger publishAnnounceTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("PublishAnnounceTasklet");

    final Logger tvAlertTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("TvAlertTasklet");

    final Logger tvPostTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("TvPostTasklet");

    final Logger tvTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("TvTasklet");

    final Logger twFavTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("TwFavTasklet");

    final Logger twFolBTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("TwFolBTasklet");

    final Logger updateUrlTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("UpdateUrlTasklet");

    final Logger yahooItemSearchTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("YahooItemSearchTasklet");

    final Logger calendarCatchupTaskletLogger = Log4jUtils.newConsoleCsvAllLogger("CalendarCatchupTasklet");

    public void printBlogCatchupTaskletLogger(String arg) {
        blogCatchupTaskletLogger.info(arg);
    }

    public void printBlogUpdateTasklet(String arg) {
        blogUpdateTaskletLogger.info(arg);
    }

    public void printFutureItemReminderTasklet(String arg) {
        futureItemReminderTaskletLogger.info(arg);
    }

    public void printItemSearchMemberTasklet(String arg) {
        itemSearchMemberTaskletLogger.info(arg);
    }

    public void printItemSearchTasklet(String arg) {
        itemSearchTaskletLogger.info(arg);
    }

    public void printPublishAnnounceTasklet(String arg) {
        publishAnnounceTaskletLogger.info(arg);
    }

    public void printTvAlertTasklet(String arg) {
        tvAlertTaskletLogger.info(arg);
    }

    public void printTvPostTasklet(String arg) {
        tvPostTaskletLogger.info(arg);
    }

    public void printTvTasklet(String arg) {
        tvTaskletLogger.info(arg);
    }

    public void printTwFavTasklet(String arg) {
        twFavTaskletLogger.info(arg);
    }

    public void printTwFolBTasklet(String arg) {
        twFolBTaskletLogger.info(arg);
    }

    public void printUpdateUrlTasklet(String arg) {
        updateUrlTaskletLogger.info(arg);
    }

    public void printYahooItemSearchTasklet(String arg) {
        yahooItemSearchTaskletLogger.info(arg);
    }

    public void printCalendarCatchupTaskletLogger(String arg) {
        calendarCatchupTaskletLogger.info(arg);
    }
}
