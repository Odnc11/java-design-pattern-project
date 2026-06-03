package main.patterns.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete Subject - Observer Design Pattern
 * 
 * Manages product stock levels and prices, and notifies all registered
 * observers when changes occur. Implements the Subject interface for
 * loose coupling with observers.
 * 
 * Supports event types:
 *   - STOCK_CHANGED: stock level modified
 *   - PRICE_CHANGED: price modified (by seller)
 *   - DISCOUNT_ADDED: decorator applied (by seller)
 *   - DISCOUNT_REMOVED: decorator removed (by seller)
 * 
 * SOLID Principles Applied:
 * - SRP: Only responsible for state management and observer notification
 * - OCP: New observers can be added without modifying this class
 * - DIP: Depends on Observer abstraction, not concrete implementations
 */
public class StockSubject implements Subject {
    private final List<Observer> observers;
    private final Map<String, Integer> stockLevels;
    private final Map<String, Double> priceLevels;

    public StockSubject() {
        this.observers = new ArrayList<>();
        this.stockLevels = new HashMap<>();
        this.priceLevels = new HashMap<>();
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
    public void notifyObservers(String eventType, String productName, Object data) {
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(eventType, productName, data);
        }
    }

    // --- Stock Management ---

    /**
     * Set stock level for a product. Notifies observers if stock changed.
     */
    public void setStock(String productName, int newStock) {
        int oldStock = stockLevels.getOrDefault(productName, 0);
        stockLevels.put(productName, newStock);
        if (oldStock != newStock) {
            int[] stockData = {oldStock, newStock};
            notifyObservers("STOCK_CHANGED", productName, stockData);
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

    // --- Price Management ---

    /**
     * Set price for a product. Notifies observers if price changed.
     */
    public void setPrice(String productName, double newPrice) {
        double oldPrice = priceLevels.getOrDefault(productName, 0.0);
        priceLevels.put(productName, newPrice);
        if (Math.abs(oldPrice - newPrice) > 0.01) {
            double[] priceData = {oldPrice, newPrice};
            notifyObservers("PRICE_CHANGED", productName, priceData);
        }
    }

    /**
     * Initialize price without triggering notifications (for setup).
     */
    public void initializePrice(String productName, double price) {
        priceLevels.put(productName, price);
    }

    public double getPrice(String productName) {
        return priceLevels.getOrDefault(productName, 0.0);
    }

    // --- Discount Notifications ---

    /**
     * Notify observers that a discount/decorator was added to a product.
     * @param discountDescription Description of the applied discount
     */
    public void notifyDiscountAdded(String productName, String discountDescription) {
        notifyObservers("DISCOUNT_ADDED", productName, discountDescription);
    }

    /**
     * Notify observers that discounts were removed from a product.
     */
    public void notifyDiscountRemoved(String productName) {
        notifyObservers("DISCOUNT_REMOVED", productName, null);
    }

    // --- Accessors ---

    public Map<String, Integer> getAllStockLevels() {
        return Collections.unmodifiableMap(stockLevels);
    }

    public Map<String, Double> getAllPriceLevels() {
        return Collections.unmodifiableMap(priceLevels);
    }
}
