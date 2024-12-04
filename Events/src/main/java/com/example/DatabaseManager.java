package com.example;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private static Connection connection;

    public static void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:events.db");
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS events (id INTEGER PRIMARY KEY AUTOINCREMENT, eventType TEXT, date TEXT, description TEXT)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Event> loadEvents() {
        ArrayList<Event> events = new ArrayList<>();
        String query = "SELECT * FROM events";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String eventType = resultSet.getString("eventType");
                String date = resultSet.getString("date");
                String description = resultSet.getString("description");
                events.add(new Event(eventType, date, description));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public static void saveEvent(Event event) {
        String query = "INSERT INTO events (eventType, date, description) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, event.getEventType());
            preparedStatement.setString(2, event.getDate());
            preparedStatement.setString(3, event.getDescription());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteEvent(Event event) {
        String query = "DELETE FROM events WHERE eventType = ? AND date = ? AND description = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, event.getEventType());
            preparedStatement.setString(2, event.getDate());
            preparedStatement.setString(3, event.getDescription());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateEvent(Event oldEvent, Event newEvent) {
        String query = "UPDATE events SET eventType = ?, date = ?, description = ? WHERE eventType = ? AND date = ? AND description = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newEvent.getEventType());
            preparedStatement.setString(2, newEvent.getDate());
            preparedStatement.setString(3, newEvent.getDescription());
            preparedStatement.setString(4, oldEvent.getEventType());
            preparedStatement.setString(5, oldEvent.getDate());
            preparedStatement.setString(6, oldEvent.getDescription());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
