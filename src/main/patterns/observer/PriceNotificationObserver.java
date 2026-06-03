package main.patterns.observer;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import main.patterns.singleton.AppConfig;

/**
 * Concrete Observer - Price & Discount Notification System
 * 
 * Creates user-facing notifications for price and discount events.
 * Handles PRICE_CHANGED and DISCOUNT_ADDED events, plus STOCK_CHANGED
 * for stock warnings.
 * 
 * Supports listener callbacks for real-time UI integration.
 */
public class PriceNotificationObserver implements Observer {
    private final List<String> notifications;
    private final List<NotificationListener> listeners;
    private final DateTimeFormatter formatter;

    /**
     * Functional interface for UI notification callbacks.
     */
    public interface NotificationListener {
        void onNotification(String message);
    }

    public PriceNotificationObserver() {
        this.notifications = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    @Override
    public void update(String eventType, String productName, Object data) {
        // AppConfig Singleton'dan bildirim ayarını kontrol et
        if (!AppConfig.getInstance().isNotificationsEnabled()) return;

        String timestamp = LocalDateTime.now().format(formatter);
        String notification = null;

        switch (eventType) {
            case "PRICE_CHANGED":
                if (data instanceof double[]) {
                    double[] priceData = (double[]) data;
                    double oldPrice = priceData[0];
                    double newPrice = priceData[1];
                    if (newPrice < oldPrice) {
                        notification = "[" + timestamp + "] 🔴 " + productName +
                                " fiyatı düştü! " + String.format("%.2f₺ → %.2f₺", oldPrice, newPrice);
                    } else {
                        notification = "[" + timestamp + "] 🟢 " + productName +
                                " fiyatı güncellendi: " + String.format("%.2f₺ → %.2f₺", oldPrice, newPrice);
                    }
                }
                break;

            case "DISCOUNT_ADDED":
                notification = "[" + timestamp + "] 🎉 " + productName +
                        " indirime girdi! " + (data != null ? data.toString() : "");
                break;

            case "DISCOUNT_REMOVED":
                notification = "[" + timestamp + "] ℹ️ " + productName +
                        " indirimleri kaldırıldı.";
                break;

            case "STOCK_CHANGED":
                if (data instanceof int[]) {
                    int[] stockData = (int[]) data;
                    int newStock = stockData[1];
                    if (newStock == 0) {
                        notification = "[" + timestamp + "] 🔴 " + productName +
                                " tükendi! Stokta kalmadı.";
                    } else if (newStock <= AppConfig.getInstance().getLowStockThreshold()) {
                        notification = "[" + timestamp + "] 🟡 " + productName +
                                " stok azaldı: " + newStock + " adet kaldı.";
                    }
                }
                break;
        }

        if (notification != null) {
            notifications.add(notification);
            System.out.println("[PriceNotificationObserver] " + notification);

            // Notify UI listeners
            for (NotificationListener listener : new ArrayList<>(listeners)) {
                listener.onNotification(notification);
            }
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
