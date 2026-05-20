package main.patterns.observer;

/**
 * Subject Interface - Observer Design Pattern (Behavioral)
 * 
 * Defines the contract for observable objects that maintain
 * a list of observers and notify them of state changes.
 * Follows Dependency Inversion Principle (DIP) - depends on Observer abstraction.
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
     * Notify all registered observers of a stock change.
     * @param productName Name of the product
     * @param oldStock    Previous stock level
     * @param newStock    New stock level
     */
    void notifyObservers(String productName, int oldStock, int newStock);
}
