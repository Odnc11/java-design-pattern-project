package main.patterns.observer;

/**
 * Observer Interface - Observer Design Pattern (Behavioral)
 * 
 * Defines the contract for objects that should be notified
 * when the Subject's state changes. Follows the Interface
 * Segregation Principle (ISP) by keeping the interface minimal.
 */
public interface Observer {
    /**
     * Called by the Subject when stock changes occur.
     * @param productName Name of the product whose stock changed
     * @param oldStock    Previous stock level
     * @param newStock    New stock level
     */
    void update(String productName, int oldStock, int newStock);
}
