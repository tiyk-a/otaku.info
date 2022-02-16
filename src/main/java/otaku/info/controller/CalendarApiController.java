package otaku.info.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.paging.Page;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.gax.paging.Page;
import com.google.auth.appengine.AppEngineCredentials;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cal")
@AllArgsConstructor
public class CalendarApiController {

    /** Application name. */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /** Directory to store authorization tokens for this application. */
    private static final String TOKENS_DIRECTORY_PATH = "/Users/chiara/Desktop/info/src/main/resources";

    private static final String SERVICE_CREDENTIALS_FILE_PATH = "/Users/chiara/Desktop/info/src/main/resources/" + "otakuinfo-front-aef10ca74233.json";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = CalendarApiController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    /**
     * Service account authorize
     *
     * @throws IOException
     */
    @GetMapping("/tmp")
//    static void authExplicit(String jsonPath) throws IOException {
    static void authExplicit() throws IOException, GeneralSecurityException {
        String jsonPath = SERVICE_CREDENTIALS_FILE_PATH;
        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
//        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
//                .createScoped(Lists.newArrayList(CalendarScopes.CALENDAR_READONLY));
//        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//
//        System.out.println("Buckets:");
//        Page<Bucket> buckets = storage.list();
//        for (Bucket bucket : buckets.iterateAll()) {
//            System.out.println(bucket.toString());
//        }

        System.out.println("0");
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_CREDENTIALS_FILE_PATH))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        System.out.println("1");
//        Events events = service.events().list("1sb8fb0nlu2l7t8hc1fsncau2g@group.calendar.google.com").execute();
//        List<Event> items = events.getItems();
//        for (Event event : items) {
//            DateTime start = event.getStart().getDateTime();
//            DateTime end = event.getEnd().getDateTime();
//            System.out.printf(event.getSummary() + " (" + start + " - " + end + ")");
//        }

        EventDateTime startEventDateTime = new EventDateTime().setDateTime(new DateTime("2022-02-16T09:00:00-07:00")); // イベント開始日時
        EventDateTime endEventDateTime = new EventDateTime().setDateTime(new DateTime("2022-02-16T09:00:00-07:00")); // イベント終了日時

        Double d = Math.random();
        String summary = "テスト" + d;
        String description = "テスト";

        System.out.println("2");
        Event event = new Event()
                .setSummary(summary)
                .setDescription(description)
                .setColorId("2") // green
                .setStart(startEventDateTime)
                .setEnd(endEventDateTime);

        System.out.println("3");
        event = service.events().insert("1sb8fb0nlu2l7t8hc1fsncau2g@group.calendar.google.com", event).execute();
        System.out.println("4");
    }

    @GetMapping("")
    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());

        // ここの.list()引数が、どのカレンダーのレコードを取ってくるか、になる
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }


    private static Credential getCredentials2(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        System.out.println("*1");
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_CREDENTIALS_FILE_PATH))
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR_EVENTS));

        System.out.println("*2");
        return credential;
    }

    @GetMapping("/test")
    public static String addEvent() throws GeneralSecurityException, IOException {
        Double d = Math.random();
        System.out.println("1");
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials2(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        EventDateTime startEventDateTime = new EventDateTime().setDateTime(new DateTime("2022-02-16T09:00:00-07:00")); // イベント開始日時
        EventDateTime endEventDateTime = new EventDateTime().setDateTime(new DateTime("2022-02-16T09:00:00-07:00")); // イベント終了日時

        String summary = "テスト" + d;
        String description = "テスト";

        System.out.println("2");
        Event event = new Event()
                .setSummary(summary)
                .setDescription(description)
                .setColorId("2") // green
                .setStart(startEventDateTime)
                .setEnd(endEventDateTime);

        System.out.println("3");
        event = service.events().insert("1sb8fb0nlu2l7t8hc1fsncau2g@group.calendar.google.com", event).execute();
        System.out.println("4");
        return event.getId();
    }
}