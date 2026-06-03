package main.patterns.observer;

/**
 * Observer Interface - Observer Design Pattern (Behavioral)
 * 
 * Defines the contract for objects that should be notified
 * when the Subject's state changes.
 * 
 * Generalized to support multiple event types (stock, price, discount).
 * 
 * SOLID: Interface Segregation Principle - minimal, focused interface.
 */
public interface Observer {
    /**
     * Called by the Subject when state changes occur.
     * @param eventType   Type of event (e.g., "STOCK_CHANGED", "PRICE_CHANGED", "DISCOUNT_ADDED")
     * @param productName Name of the affected product
     * @param data        Event-specific data (old/new values, etc.)
     */
    void update(String eventType, String productName, Object data);
}
