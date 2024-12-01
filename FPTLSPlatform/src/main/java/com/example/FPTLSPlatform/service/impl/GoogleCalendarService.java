package com.example.FPTLSPlatform.service.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Tạo Credential từ file JSON đã tải về từ Google Developer Console
    public static Credential getCredentials() throws IOException {
        // Đọc credentials.json từ thư mục resources
        FileInputStream credentialStream = new FileInputStream("src/main/resources/credentials.json");

        // Sử dụng GoogleCredential để tạo đối tượng xác thực
        GoogleCredential credential = GoogleCredential.fromStream(credentialStream)
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

        return credential;
    }

    // Tạo và trả về Calendar service
    public static Calendar getCalendarService() throws IOException {
        Credential credential = getCredentials();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Lấy sự kiện từ Google Calendar
    public static Event getEventByGoogleMeetLink(String googleMeetLink) throws IOException {
        Calendar service = getCalendarService();
        // Lọc sự kiện theo link Google Meet
        Events events = service.events().list("primary")
                .setQ(googleMeetLink)  // Tìm kiếm sự kiện theo link Google Meet
                .execute();

        List<Event> eventList = events.getItems();
        if (eventList.isEmpty()) {
            return null; // Không tìm thấy sự kiện
        }
        return eventList.get(0); // Giả sử chỉ có một sự kiện chứa Google Meet Link
    }

    // Thêm học viên vào Google Meet (làm ví dụ thêm attendee vào sự kiện)
    public static void addAttendeeToGoogleMeet(String googleMeetLink, String attendeeEmail) throws IOException {
        Event event = getEventByGoogleMeetLink(googleMeetLink);
        if (event != null) {
            // Tạo danh sách người tham gia (attendee)
            EventAttendee attendee = new EventAttendee();
            attendee.setEmail(attendeeEmail);

            // Thêm người tham gia vào sự kiện
            event.getAttendees().add(attendee);

            // Cập nhật sự kiện với người tham gia mới
            Calendar service = getCalendarService();
            service.events().update("primary", event.getId(), event).execute();
        }
    }
}
