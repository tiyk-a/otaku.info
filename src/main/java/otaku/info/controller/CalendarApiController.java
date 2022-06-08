package otaku.info.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import java.io.FileInputStream;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import otaku.info.enums.TeamEnum;
import otaku.info.setting.Setting;
import otaku.info.utils.DateUtils;

/**
 * Google Calendarにイベントを追加したりするコントローラ
 *
 * デフォルトのservice accountは
 * otakuinfo@otakuinfo-front.iam.gserviceaccount.com
 *
 * https://www.cdatablog.jp/entry/googlecalendarserviceaccount
 */
@RestController
@RequestMapping("/cal")
@AllArgsConstructor
public class CalendarApiController {

    @Autowired
    Setting setting;

    @Autowired
    DateUtils dateUtils;

    /** Application name. */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Service account authorize Get Event
     *
     * @param calendarId "1sb8fb0nlu2l7t8hc1fsncau2g@group.calendar.google.com"こんな形のcalendarId
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @GetMapping("/get")
    public void getEvents(@RequestParam String calendarId) throws IOException, GeneralSecurityException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(setting.getCalendarCredential()))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        Events events = service.events().list(calendarId).execute();
        List<Event> items = events.getItems();
        for (Event event : items) {
            DateTime start = event.getStart().getDateTime();
            DateTime end = event.getEnd().getDateTime();
            String id = event.getId();
            String iCalUID = event.getICalUID();
            String recurringEventId = event.getRecurringEventId();
            System.out.print(event.getSummary() + " " + id + " " + iCalUID + " " + recurringEventId);
        }
    }

    /**
     * Service account authorize Post Event
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @GetMapping("/post")
    public Event postEvent(String calendarId, LocalDateTime startDate, LocalDateTime endDate, String summary, String desc, Boolean allDayFlg) throws IOException, GeneralSecurityException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(setting.getCalendarCredential()))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        EventDateTime startEventDateTime;
        EventDateTime endEventDateTime;

        if (allDayFlg) {
            Date startDateObj = DateUtils.localDateTimeToDate(startDate);
            Date endDateObj = DateUtils.localDateTimeToDate(endDate);
            DateTime startDateTime = new DateTime(dateUtils.getStringDateByDate(startDateObj));
            DateTime endDateTime = new DateTime(dateUtils.getStringDateByDate(endDateObj));
            startEventDateTime = new EventDateTime().setDate(startDateTime).setTimeZone("Asia/Tokyo");
            endEventDateTime = new EventDateTime().setDate(endDateTime).setTimeZone("Asia/Tokyo");
        } else {
            DateTime startDateTime = new DateTime(startDate.toString());
            DateTime endDateTime = new DateTime(endDate.toString());
            startEventDateTime = new EventDateTime().setDateTime(startDateTime).setTimeZone("Asia/Tokyo");
            endEventDateTime = new EventDateTime().setDateTime(endDateTime).setTimeZone("Asia/Tokyo");
        }

        Event event = new Event()
                .setSummary(summary)
                .setDescription(desc)
//                .setColorId(GColorEnum.GREEN.getId())
                .setStart(startEventDateTime)
                .setEnd(endEventDateTime);

        try {
            event = service.events().insert(calendarId, event).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Event status:" + event.getStatus());
        return event;
    }

    /**
     * 日付をDateで受け取る
     * 日付型変換して本メソッドに飛ばす
     * all-dayタイプしかうまくいかないかも
     *
     * @param calendarId
     * @param startDate
     * @param endDate
     * @param summary
     * @param desc
     * @param allDayFlg
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Event postEvent(String calendarId, Date startDate, Date endDate, String summary, String desc, Boolean allDayFlg) throws IOException, GeneralSecurityException {
        LocalDateTime startLdt = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime endLdt = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        Event event = postEvent(calendarId, startLdt, endLdt, summary, desc, allDayFlg);
        return event;
    }

    /**
     * 日付をDateで受け取る
     * 日付型変換して本メソッドに飛ばす
     * all-dayタイプしかうまくいかないかも
     *
     * @param calendarId
     * @param startDate
     * @param endDate
     * @param summary
     * @param desc
     * @param allDayFlg
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Event postEventTime(String calendarId, LocalDateTime startDate, LocalDateTime endDate, String summary, String desc, Boolean allDayFlg) throws IOException, GeneralSecurityException {
//        LocalDateTime startLdt = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
//        LocalDateTime endLdt = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        Event event = postEvent(calendarId, startDate, endDate, summary, desc, allDayFlg);
        return event;
    }

    /**
     * Service account authorize Post Event
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @GetMapping("/update")
    public Event updateEvent(String calendarId, String eventId, LocalDateTime startDate, LocalDateTime endDate, String summary, String desc, Boolean allDayFlg) throws IOException, GeneralSecurityException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(setting.getCalendarCredential()))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        EventDateTime startEventDateTime;
        EventDateTime endEventDateTime;

        if (allDayFlg) {
            Date startDateObj = DateUtils.localDateTimeToDate(startDate);
            Date endDateObj = DateUtils.localDateTimeToDate(endDate);
            DateTime startDateTime = new DateTime(dateUtils.getStringDateByDate(startDateObj));
            DateTime endDateTime = new DateTime(dateUtils.getStringDateByDate(endDateObj));
            startEventDateTime = new EventDateTime().setDate(startDateTime).setTimeZone("Asia/Tokyo");
            endEventDateTime = new EventDateTime().setDate(endDateTime).setTimeZone("Asia/Tokyo");
        } else {
            DateTime startDateTime = new DateTime(startDate.toString());
            DateTime endDateTime = new DateTime(endDate.toString());
            startEventDateTime = new EventDateTime().setDateTime(startDateTime).setTimeZone("Asia/Tokyo");
            endEventDateTime = new EventDateTime().setDateTime(endDateTime).setTimeZone("Asia/Tokyo");
        }

        Event event = new Event()
                .setId(eventId)
                .setSummary("Updated:" + summary)
                .setDescription(desc)
//                .setColorId(GColorEnum.GREEN.getId())
                .setStart(startEventDateTime)
                .setEnd(endEventDateTime);

        try {
            event = service.events().update(calendarId, eventId, event).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Event status:" + event.getStatus() + " id:" + event.getId());
        return event;
    }

    /**
     * 日付をDateで受け取る
     * 日付型変換して本メソッドに飛ばす
     * all-dayタイプしかうまくいかないかも
     *
     * @param calendarId
     * @param startDate
     * @param endDate
     * @param summary
     * @param desc
     * @param allDayFlg
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Event updateEvent(String calendarId, String eventId, Date startDate, Date endDate, String summary, String desc, Boolean allDayFlg) throws IOException, GeneralSecurityException {
        LocalDateTime startLdt = LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime endLdt = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        Event event = updateEvent(calendarId, eventId, startLdt, endLdt, summary, desc, allDayFlg);
        return event;
    }

    /**
     * visibilityをprivateにしてイベントを外から見えないようにする
     *
     * @param teamId
     * @param eventId
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Event hideEvent(Long teamId, String eventId) throws IOException, GeneralSecurityException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(setting.getCalendarCredential()))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        Event event = new Event()
                .setId(eventId)
                .setVisibility("private");

        try {
            event = service.events().update(TeamEnum.get(teamId).getCalendarId(), eventId, event).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Event status:" + event.getStatus() + " id:" + event.getId());
        return event;
    }
}