package main.patterns.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete Observer - Analytics Logger
 * 
 * Logs ALL events (stock, price, discount) for analytics purposes.
 * Provides data for the Seller Dashboard's sales analytics panel.
 * 
 * Unlike StockObserver and PriceNotificationObserver which filter events,
 * AnalyticsObserver captures everything for comprehensive logging.
 */
public class AnalyticsObserver implements Observer {
    private final List<AnalyticsEntry> entries;
    private final DateTimeFormatter formatter;

    /**
     * Represents a single analytics log entry.
     */
    public static class AnalyticsEntry {
        private final String eventType;
        private final String productName;
        private final String details;
        private final LocalDateTime timestamp;

        public AnalyticsEntry(String eventType, String productName, String details) {
            this.eventType = eventType;
            this.productName = productName;
            this.details = details;
            this.timestamp = LocalDateTime.now();
        }

        public String getEventType() { return eventType; }
        public String getProductName() { return productName; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            String time = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            return String.format("[%s] [%s] %s: %s", time, eventType, productName, details);
        }
    }

    public AnalyticsObserver() {
        this.entries = new ArrayList<>();
        this.formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    @Override
    public void update(String eventType, String productName, Object data) {
        String details;

        switch (eventType) {
            case "STOCK_CHANGED":
                if (data instanceof int[]) {
                    int[] stockData = (int[]) data;
                    details = String.format("Stok: %d → %d", stockData[0], stockData[1]);
                } else {
                    details = "Stok değişti";
                }
                break;

            case "PRICE_CHANGED":
                if (data instanceof double[]) {
                    double[] priceData = (double[]) data;
                    details = String.format("Fiyat: %.2f₺ → %.2f₺", priceData[0], priceData[1]);
                } else {
                    details = "Fiyat değişti";
                }
                break;

            case "DISCOUNT_ADDED":
                details = "İndirim eklendi" + (data != null ? ": " + data.toString() : "");
                break;

            case "DISCOUNT_REMOVED":
                details = "İndirimler kaldırıldı";
                break;

            default:
                details = "Bilinmeyen olay" + (data != null ? ": " + data.toString() : "");
                break;
        }

        AnalyticsEntry entry = new AnalyticsEntry(eventType, productName, details);
        entries.add(entry);
        System.out.println("[AnalyticsObserver] " + entry);
    }

    /**
     * Get all analytics entries.
     */
    public List<AnalyticsEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Get formatted log as strings (for UI display).
     */
    public List<String> getFormattedLog() {
        List<String> log = new ArrayList<>();
        for (AnalyticsEntry entry : entries) {
            log.add(entry.toString());
        }
        return log;
    }

    /**
     * Get entries filtered by event type.
     */
    public List<AnalyticsEntry> getEntriesByType(String eventType) {
        List<AnalyticsEntry> filtered = new ArrayList<>();
        for (AnalyticsEntry entry : entries) {
            if (entry.getEventType().equals(eventType)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    /**
     * Get entries for a specific product.
     */
    public List<AnalyticsEntry> getEntriesByProduct(String productName) {
        List<AnalyticsEntry> filtered = new ArrayList<>();
        for (AnalyticsEntry entry : entries) {
            if (entry.getProductName().equals(productName)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public int getTotalEventCount() {
        return entries.size();
    }

    public void clearLog() {
        entries.clear();
    }
}
