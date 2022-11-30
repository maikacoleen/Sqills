package nl.utwente.di.sqills.misc;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import nl.utwente.di.sqills.dao.EmployeeDAO;
import nl.utwente.di.sqills.dao.RoomDAO;
import nl.utwente.di.sqills.model.Employee;
import nl.utwente.di.sqills.model.Reservation;
import nl.utwente.di.sqills.model.Room;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarClient {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String CALENDAR_ID = "sqills.com_32353339353033353331@resource.calendar.google.com";
    public static final CalendarClient INSTANCE = new CalendarClient();
    private Calendar calendar;

    private CalendarClient() {
        try {
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            calendar = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * @param event event object to be parsed into reservation object
     * @return reservation object from event object or null
     */
    @Nullable
    private Reservation getReservation(@NotNull Event event) {
        Reservation reservation = new Reservation();
        reservation.setId(event.getId());
        Room room = RoomDAO.INSTANCE.getRoomByName(event.getLocation());
        if (room == null) {
            return null;
        }
        reservation.setRoom(room);
        reservation.setStartTime(event.getStart().getDateTime().getValue());
        reservation.setEndTime(event.getEnd().getDateTime().getValue());
        String employeeEmail = event.getCreator().getEmail();
        boolean fromCalendar = true;
        List<EventAttendee> eventAttendees = event.getAttendees();
        if (eventAttendees != null) {
            eventAttendees.removeIf(eventAttendee -> {
                Boolean organizer = eventAttendee.getOrganizer();
                return organizer != null && organizer;
            });
            EventAttendee creator = eventAttendees.stream()
                    .filter(eventAttendee -> Objects.equals(eventAttendee.getComment(), "Creator")).findFirst()
                    .orElse(null);
            if (creator != null) {
                employeeEmail = creator.getEmail();
                fromCalendar = false;
            }
            eventAttendees.remove(creator);
            reservation.setAttendees(eventAttendees.stream().map(EventAttendee::getEmail).collect(Collectors.toSet()));
        }
        Employee employee = EmployeeDAO.INSTANCE.getEmployeeByEmail(employeeEmail);
        if (employee == null) {
            return null;
        }
        reservation.setEmployee(employee);
        String title = event.getSummary();
        reservation.setTitle(title != null && !title.isEmpty() ? title : "Meeting");
        String visibility = event.getVisibility();
        reservation.setVisible(visibility == null || visibility.equals("default") || visibility.equals("public"));
        reservation.setFromCalendar(fromCalendar);
        return reservation;
    }

    /**
     * @param reservation reservation object to be parsed into event object
     * @return event object from reservation object
     */
    @NotNull
    private Event getEvent(@NotNull Reservation reservation) {
        Event event = new Event().setLocation(reservation.getRoom().getName())
                .setStart(new EventDateTime().setDateTime(new DateTime(reservation.getStartTime())))
                .setEnd(new EventDateTime().setDateTime(new DateTime(reservation.getEndTime())));
        List<EventAttendee> attendees = new ArrayList<>();
        if (!reservation.isFromCalendar()) {
            attendees.add(new EventAttendee().setComment("Creator").setEmail(reservation.getEmployee().getEmail())
                    .setResponseStatus("accepted"));
        }
        attendees.addAll(reservation.getAttendees().stream()
                .map(attendee -> new EventAttendee().setEmail(attendee).setResponseStatus("accepted"))
                .collect(Collectors.toList()));
        return event.setAttendees(attendees).setSummary(reservation.getTitle())
                .setVisibility(reservation.isVisible() ? "default" : "private");
    }

    /**
     * @return all reservations from the calendar
     */
    @NotNull
    public Map<String, Reservation> getReservations() {
        Map<String, Reservation> reservations = new HashMap<>();
        try {
            List<String> ids = new ArrayList<>();
            calendar.events().list(CALENDAR_ID).setOrderBy("startTime").setSingleEvents(true)
                    .setTimeMin(new DateTime(System.currentTimeMillis())).execute().getItems().forEach(event -> {
                Reservation reservation = getReservation(event);
                if (reservation == null) {
                    ids.add(event.getId());
                    return;
                }
                reservations.put(event.getId(), reservation);
            });
            ids.forEach(id -> {
                try {
                    calendar.events().delete(CALENDAR_ID, id).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * Inserts reservation into the calendar.
     *
     * @param reservation reservation to be inserted
     * @return inserted reservation or null
     */
    @Nullable
    public Reservation insertReservation(@NotNull Reservation reservation) {
        try {
            return reservation.setId(calendar.events().insert(CALENDAR_ID, getEvent(reservation)).execute().getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Patches reservation in the calendar.
     *
     * @param reservation reservation to be patched
     * @return patched reservation or null
     */
    @Nullable
    public Reservation patchReservation(@NotNull Reservation reservation) {
        try {
            return getReservation(calendar.events().patch(CALENDAR_ID, reservation.getId(), getEvent(reservation))
                    .execute());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes reservation from the calendar.
     *
     * @param id id of reservation to be deleted
     */
    public void deleteReservation(@NotNull String id) {
        try {
            calendar.events().delete(CALENDAR_ID, id).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
