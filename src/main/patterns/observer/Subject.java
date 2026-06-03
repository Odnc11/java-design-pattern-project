package main.patterns.observer;

/**
 * Subject Interface - Observer Design Pattern (Behavioral)
 * 
 * Defines the contract for observable objects that maintain
 * a list of observers and notify them of state changes.
 * 
 * Generalized to support multiple event types.
 * 
 * SOLID: Dependency Inversion Principle - depends on Observer abstraction.
 */
public interface Subject {
    /**
     * Register an observer to receive notifications.
     * @param observer The observer to register
     */
    void registerObserver(Observer observer);

    /**
     * Remove an observer from notifications.
     * @param observer The observer to remove
     */
    void removeObserver(Observer observer);

    /**
     * Notify all registered observers of a state change.
     * @param eventType   Type of event (e.g., "STOCK_CHANGED", "PRICE_CHANGED")
     * @param productName Name of the affected product
     * @param data        Event-specific data
     */
    void notifyObservers(String eventType, String productName, Object data);
}
