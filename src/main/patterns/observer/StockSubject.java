package main.patterns.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete Subject - Observer Design Pattern
 * 
 * Manages product stock levels and notifies all registered observers
 * when stock changes occur. Implements the Subject interface for
 * loose coupling with observers.
 * 
 * SOLID Principles Applied:
 * - SRP: Only responsible for stock management and observer notification
 * - OCP: New observers can be added without modifying this class
 * - DIP: Depends on Observer abstraction, not concrete implementations
 */
public class StockSubject implements Subject {
    private final List<Observer> observers;
    private final Map<String, Integer> stockLevels;

    public StockSubject() {
        this.observers = new ArrayList<>();
        this.stockLevels = new HashMap<>();
    }

    @Override
    public void registerObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String productName, int oldStock, int newStock) {
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(productName, oldStock, newStock);
        }
    }

    /**
     * Set stock level for a product. Notifies observers if stock changed.
     */
    public void setStock(String productName, int newStock) {
        int oldStock = stockLevels.getOrDefault(productName, 0);
        stockLevels.put(productName, newStock);
        if (oldStock != newStock) {
            notifyObservers(productName, oldStock, newStock);
        }
    }

    /**
     * Initialize stock without triggering notifications (for setup).
     */
    public void initializeStock(String productName, int stock) {
        stockLevels.put(productName, stock);
    }

    public int getStock(String productName) {
        return stockLevels.getOrDefault(productName, 0);
    }

    /**
     * Decrease stock by a given amount. Triggers observer notification.
     */
    public void decreaseStock(String productName, int amount) {
        int currentStock = getStock(productName);
        int newStock = Math.max(0, currentStock - amount);
        setStock(productName, newStock);
    }

    /**
     * Increase stock by a given amount. Triggers observer notification.
     */
    public void increaseStock(String productName, int amount) {
        int currentStock = getStock(productName);
        setStock(productName, currentStock + amount);
    }

    public Map<String, Integer> getAllStockLevels() {
        return Collections.unmodifiableMap(stockLevels);
    }
}
