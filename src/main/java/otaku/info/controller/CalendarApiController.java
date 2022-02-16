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
import java.util.Collections;
import java.util.List;

import java.io.FileInputStream;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import otaku.info.enums.GColorEnum;

/**
 * デフォルトのservice accountは
 * otakuinfo@otakuinfo-front.iam.gserviceaccount.com
 *
 * https://www.cdatablog.jp/entry/googlecalendarserviceaccount
 */
@RestController
@RequestMapping("/cal")
@AllArgsConstructor
public class CalendarApiController {

    /** Application name. */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String SERVICE_CREDENTIALS_FILE_PATH = "/Users/chiara/Desktop/info/src/main/resources/" + "otakuinfo-front-aef10ca74233.json";

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
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_CREDENTIALS_FILE_PATH))
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
    public Event postEvent(String calendarId, DateTime startDate, DateTime endDate, String summary, String desc, Boolean allDayFlg) throws IOException, GeneralSecurityException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_CREDENTIALS_FILE_PATH))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        EventDateTime startEventDateTime;
        EventDateTime endEventDateTime;

        if (allDayFlg) {
            startEventDateTime = new EventDateTime().setDate(startDate).setTimeZone("Japan/Tokyo");
            endEventDateTime = new EventDateTime().setDate(endDate).setTimeZone("Japan/Tokyo");
        } else {
            startEventDateTime = new EventDateTime().setDateTime(startDate).setTimeZone("Japan/Tokyo");
            endEventDateTime = new EventDateTime().setDateTime(endDate).setTimeZone("Japan/Tokyo");
        }

        Event event = new Event()
                .setSummary(summary)
                .setDescription(desc)
                .setColorId(GColorEnum.GREEN.toString())
                .setStart(startEventDateTime)
                .setEnd(endEventDateTime);

        event = service.events().insert(calendarId, event).execute();
        System.out.println("Event status:" + event.getStatus());
        return event;
    }
}