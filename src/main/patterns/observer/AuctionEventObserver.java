package main.patterns.observer;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete AuctionObserver - Price Change Logger
 * 
 * Monitors demand-driven price changes and auction events.
 * Logs all events and supports listener callbacks for UI integration.
 * 
 * Demonstrates the Observer pattern applied to the auction/demand system:
 *   - Receives notifications when product prices change due to demand
 *   - Receives notifications when new bids are placed
 *   - Bridges events to UI via AuctionEventListener
 * 
 * SOLID Principles:
 *   - SRP: Only responsible for logging auction events and notifying UI
 *   - OCP: New event types can be added by extending AuctionObserver
 */
public class AuctionEventObserver implements AuctionObserver {
    private final List<String> eventLog;
    private final List<AuctionEventListener> listeners;
    private final DateTimeFormatter formatter;

    /**
     * Functional interface for UI auction event callbacks.
     */
    public interface AuctionEventListener {
        void onAuctionEvent(String message, String eventType);
    }

    public AuctionEventObserver() {
        this.eventLog = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    @Override
    public void onPriceChanged(String productName, double oldPrice, double newPrice,
                               int demandCount, int currentStock) {
        String timestamp = LocalDateTime.now().format(formatter);
        String direction = newPrice > oldPrice ? "📈" : "📉";
        double changePercent = ((newPrice - oldPrice) / oldPrice) * 100;

        String message = String.format("[%s] %s %s: %.2f₺ → %.2f₺ (%+.1f%%) | Talep: %d | Stok: %d",
                timestamp, direction, productName, oldPrice, newPrice,
                changePercent, demandCount, currentStock);

        eventLog.add(message);
        System.out.println("[AuctionObserver] " + message);

        // Notify UI listeners
        String eventType = newPrice > oldPrice ? "PRICE_UP" : "PRICE_DOWN";
        for (AuctionEventListener listener : new ArrayList<>(listeners)) {
            listener.onAuctionEvent(message, eventType);
        }
    }

    @Override
    public void onNewBid(String productName, String bidderName, 
                         double bidAmount, boolean isHighest) {
        String timestamp = LocalDateTime.now().format(formatter);
        String highestTag = isHighest ? " ★ EN YÜKSEK TEKLİF" : "";

        String message = String.format("[%s] 🏷️ YENİ TEKLİF: %s → %s: %.2f₺%s",
                timestamp, bidderName, productName, bidAmount, highestTag);

        eventLog.add(message);
        System.out.println("[AuctionObserver] " + message);

        for (AuctionEventListener listener : new ArrayList<>(listeners)) {
            listener.onAuctionEvent(message, "NEW_BID");
        }
    }

    // --- Listener Management ---

    public void addListener(AuctionEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(AuctionEventListener listener) {
        listeners.remove(listener);
    }

    // --- Log Access ---

    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    public String getLastEvent() {
        return eventLog.isEmpty() ? "" : eventLog.get(eventLog.size() - 1);
    }

    public void clearLog() {
        eventLog.clear();
    }
}
