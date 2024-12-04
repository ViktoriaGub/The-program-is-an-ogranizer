package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Properties;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;

public class Organizer {

    static ArrayList<Event> events = new ArrayList<>();

    public static void main(String[] args) {
        // Инициализация базы данных
        DatabaseManager.connect();
        events = DatabaseManager.loadEvents();

        // Основное окно приложения
        JFrame frame = new JFrame("Органайзер событий");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

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

        // Назначение обработчиков событий
        addButton.addActionListener(e -> addEvent());
        deleteButton.addActionListener(e -> deleteEvent());
        viewButton.addActionListener(e -> viewEvents());
        editButton.addActionListener(e -> editEvent());
        exitButton.addActionListener(e -> {
            DatabaseManager.disconnect();
            System.exit(0);
        });

        frame.setVisible(true);
    }

    public static void addEvent() {
        String[] eventTypes = {"День рождения", "Важная встреча"};
        JComboBox<String> eventTypeField = new JComboBox<>(eventTypes);

        // Создание выбора даты
        SqlDateModel model = new SqlDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Сегодня");
        properties.put("text.month", "Месяц");
        properties.put("text.year", "Год");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, null);

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

            if (date.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Пожалуйста, заполните все поля.");
                return;
            }

            Event newEvent = new Event(eventType, date, description);
            events.add(newEvent);
            DatabaseManager.saveEvent(newEvent);
            JOptionPane.showMessageDialog(null, "Событие успешно добавлено!");
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

        String selectedValue = (String) JOptionPane.showInputDialog(
                null,
                "Выберите событие для удаления:",
                "Удалить событие",
                JOptionPane.QUESTION_MESSAGE,
                null,
                eventList,
                eventList[0]
        );

        if (selectedValue != null) {
            int index = Integer.parseInt(selectedValue.split("\\. ")[0]) - 1;
            Event event = events.get(index);
            events.remove(index);
            DatabaseManager.deleteEvent(event);
            JOptionPane.showMessageDialog(null, "Событие успешно удалено!");
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

        String selectedValue = (String) JOptionPane.showInputDialog(
                null,
                "Выберите событие для редактирования:",
                "Редактировать событие",
                JOptionPane.QUESTION_MESSAGE,
                null,
                eventList,
                eventList[0]
        );

        if (selectedValue != null) {
            int index = Integer.parseInt(selectedValue.split("\\. ")[0]) - 1;
            Event oldEvent = events.get(index);

            String[] eventTypes = {"День рождения", "Важная встреча"};
            JComboBox<String> eventTypeField = new JComboBox<>(eventTypes);
            eventTypeField.setSelectedItem(oldEvent.getEventType());

            SqlDateModel model = new SqlDateModel();
            model.setValue(java.sql.Date.valueOf(oldEvent.getDate()));
            Properties properties = new Properties();
            properties.put("text.today", "Сегодня");
            properties.put("text.month", "Месяц");
            properties.put("text.year", "Год");
            JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
            JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, null);

            JTextField descriptionField = new JTextField(oldEvent.getDescription());

            Object[] message = {
                    "Тип события:", eventTypeField,
                    "Дата события:", datePicker,
                    "Описание события:", descriptionField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Редактировать событие", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String newEventType = (String) eventTypeField.getSelectedItem();
                String newDate = model.getValue() != null ? model.getValue().toString() : "";
                String newDescription = descriptionField.getText();

                if (newDate.isEmpty() || newDescription.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Пожалуйста, заполните все поля.");
                    return;
                }

                Event newEvent = new Event(newEventType, newDate, newDescription);
                events.set(index, newEvent);
                DatabaseManager.updateEvent(oldEvent, newEvent);
                JOptionPane.showMessageDialog(null, "Событие успешно отредактировано!");
            }
        }
    }
}
