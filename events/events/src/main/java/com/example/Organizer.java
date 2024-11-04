package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;
import org.jdatepicker.impl.DateComponentFormatter;


public class Organizer {
    static ArrayList<Event> events = new ArrayList<>();
    static Connection connection;

    public static void main(String[] args) {
        // Подключение к базе данных SQLite
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:events.db");
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS events (id INTEGER PRIMARY KEY AUTOINCREMENT, eventType TEXT, date TEXT, description TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        loadEventsFromDatabase();

        // Создание основного окна
        JFrame frame = new JFrame("Органайзер событий");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

        // Панель кнопок для управления событиями
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));

        JButton addButton = new JButton("Добавить событие");
        JButton deleteButton = new JButton("Удалить событие");
        JButton viewButton = new JButton("Просмотреть все события");
        JButton editButton = new JButton("Редактировать событие");
        JButton exitButton = new JButton("Выйти");

        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(viewButton);
        panel.add(editButton);
        panel.add(exitButton);

        frame.add(panel, BorderLayout.CENTER);

        // Добавление слушателей событий для кнопок
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEvent();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteEvent();
            }
        });

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewEvents();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editEvent();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

    public static void loadEventsFromDatabase() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM events");

            while (resultSet.next()) {
                String eventType = resultSet.getString("eventType");
                String date = resultSet.getString("date");
                String description = resultSet.getString("description");
                events.add(new Event(eventType, date, description));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addEvent() {
        String[] eventTypes = {"День рождения", "Важная встреча"};
        JComboBox<String> eventTypeField = new JComboBox<>(eventTypes);

        // Создаем компонент выбора даты
        SqlDateModel model = new SqlDateModel();
        Properties p = new Properties();
        p.put("text.today", "Сегодня");
        p.put("text.month", "Месяц");
        p.put("text.year", "Год");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

        JTextField descriptionField = new JTextField();

        Object[] message = {
                "Тип события:", eventTypeField,
                "Дата события:", datePicker,
                "Описание события:", descriptionField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Добавить событие", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String eventType = (String) eventTypeField.getSelectedItem();
            String date = model.getValue() != null ? model.getValue().toString() : "";
            String description = descriptionField.getText();

            events.add(new Event(eventType, date, description));
            saveEventToDatabase(eventType, date, description);
            JOptionPane.showMessageDialog(null, "Событие успешно добавлено!");
        }
    }

    public static void saveEventToDatabase(String eventType, String date, String description) {
        try {
            String sql = "INSERT INTO events (eventType, date, description) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, eventType);
            preparedStatement.setString(2, date);
            preparedStatement.setString(3, description);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteEvent() {
        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Нет событий для удаления.");
            return;
        }

        String[] eventList = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            eventList[i] = (i + 1) + ". " + events.get(i).toString();
        }

        String selectedValue = (String) JOptionPane.showInputDialog(null, "Выберите событие для удаления:", "Удалить событие", JOptionPane.QUESTION_MESSAGE, null, eventList, eventList[0]);
        if (selectedValue != null) {
            int index = Integer.parseInt(selectedValue.split("\\. ")[0]) - 1;
            Event event = events.get(index);
            events.remove(index);
            deleteEventFromDatabase(event);
            JOptionPane.showMessageDialog(null, "Событие успешно удалено!");
        }
    }


    public static void deleteEventFromDatabase(Event event) {
        try {
            String sql = "DELETE FROM events WHERE eventType = ? AND date = ? AND description = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, event.eventType);
            preparedStatement.setString(2, event.date);
            preparedStatement.setString(3, event.description);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewEvents() {
        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Нет событий для отображения.");
        } else {
            StringBuilder eventList = new StringBuilder("Список всех событий:\n");
            for (int i = 0; i < events.size(); i++) {
                eventList.append((i + 1)).append(". ").append(events.get(i).toString()).append("\n");
            }
            JOptionPane.showMessageDialog(null, eventList.toString());
        }
    }

    public static void editEvent() {
        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Нет событий для редактирования.");
            return;
        }

        String[] eventList = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            eventList[i] = (i + 1) + ". " + events.get(i).toString();
        }

        String selectedValue = (String) JOptionPane.showInputDialog(null, "Выберите событие для редактирования:", "Редактировать событие", JOptionPane.QUESTION_MESSAGE, null, eventList, eventList[0]);
        if (selectedValue != null) {
            int index = Integer.parseInt(selectedValue.split("\\. ")[0]) - 1;

            String[] eventTypes = {"День рождения", "Важная встреча"};
            JComboBox<String> eventTypeField = new JComboBox<>(eventTypes);
            eventTypeField.setSelectedItem(events.get(index).eventType);

            // Создаем компонент выбора даты
            SqlDateModel model = new SqlDateModel();
            Properties p = new Properties();
            p.put("text.today", "Сегодня");
            p.put("text.month", "Месяц");
            p.put("text.year", "Год");
            JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
            JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());
            model.setValue(Date.valueOf(events.get(index).date));

            JTextField descriptionField = new JTextField(events.get(index).description);

            Object[] message = {
                    "Тип события:", eventTypeField,
                    "Дата события:", datePicker,
                    "Описание события:", descriptionField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Редактировать событие", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String eventType = (String) eventTypeField.getSelectedItem();
                String date = model.getValue() != null ? model.getValue().toString() : "";
                String description = descriptionField.getText();

                Event oldEvent = events.get(index);
                events.set(index, new Event(eventType, date, description));
                updateEventInDatabase(oldEvent, eventType, date, description);
                JOptionPane.showMessageDialog(null, "Событие успешно отредактировано!");
            }
        }
    }


    public static void updateEventInDatabase(Event oldEvent, String eventType, String date, String description) {
        try {
            String sql = "UPDATE events SET eventType = ?, date = ?, description = ? WHERE eventType = ? AND date = ? AND description = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, eventType);
            preparedStatement.setString(2, date);
            preparedStatement.setString(3, description);
            preparedStatement.setString(4, oldEvent.eventType);
            preparedStatement.setString(5, oldEvent.date);
            preparedStatement.setString(6, oldEvent.description);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
