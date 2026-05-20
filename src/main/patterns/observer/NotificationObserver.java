package main.patterns.observer;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete Observer - Notification System
 * 
 * Creates user-facing notifications for stock events.
 * Supports listener callbacks for UI integration.
 * 
 * Uses the Listener pattern internally to bridge Observer events to UI.
 */
public class NotificationObserver implements Observer {
    private final List<String> notifications;
    private final List<NotificationListener> listeners;
    private final DateTimeFormatter formatter;

    /**
     * Functional interface for UI notification callbacks.
     */
    public interface NotificationListener {
        void onNotification(String message);
    }

    public NotificationObserver() {
        this.notifications = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    @Override
    public void update(String productName, int oldStock, int newStock) {
        String timestamp = LocalDateTime.now().format(formatter);
        String notification;

        if (newStock == 0) {
            notification = "[" + timestamp + "] 🔴 " + productName + " tükendi! Stokta kalmadı.";
        } else if (newStock < oldStock) {
            notification = "[" + timestamp + "] 🟡 " + productName + " stok azaldı: " + newStock + " adet kaldı.";
        } else {
            notification = "[" + timestamp + "] 🟢 " + productName + " stok güncellendi: " + newStock + " adet mevcut.";
        }

        notifications.add(notification);
        System.out.println("[NotificationObserver] " + notification);

        // Notify UI listeners
        for (NotificationListener listener : new ArrayList<>(listeners)) {
            listener.onNotification(notification);
        }
    }

    public void addListener(NotificationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }
}
