package com.example;
public class Event {
    String eventType;
    String date;
    String description;

    public Event(String eventType, String date, String description) {
        this.eventType = eventType;
        this.date = date;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Тип: " + eventType + ", Дата: " + date + ", Описание: " + description;
    }
}
