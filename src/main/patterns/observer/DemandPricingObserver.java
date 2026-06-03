package main.patterns.observer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import main.patterns.singleton.AppConfig;

/**
 * Concrete Observer - Demand Pricing (Arz-Talep Dinamik Fiyatlandırma)
 * 
 * Monitors STOCK_CHANGED events.
 * Automatically applies price modifiers based on supply/demand principles:
 * - High Demand (Low Stock): Increases price by 20%
 * - Overstock (High Stock): Decreases price by 10%
 * - Normal: Standard price
 * 
 * Works together with DynamicPricingDecorator which fetches these modifiers.
 */
public class DemandPricingObserver implements Observer {
    // Map of productName -> price modifier percentage
    private static final Map<String, Double> priceModifiers = new ConcurrentHashMap<>();
    
    // UI Listeners
    private static final java.util.List<main.patterns.observer.PriceNotificationObserver.NotificationListener> listeners = new java.util.ArrayList<>();
    
    public static void addListener(main.patterns.observer.PriceNotificationObserver.NotificationListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners(String message) {
        for (main.patterns.observer.PriceNotificationObserver.NotificationListener listener : listeners) {
            listener.onNotification(message);
        }
    }

    @Override
    public void update(String eventType, String productName, Object data) {
        if (!"STOCK_CHANGED".equals(eventType)) return;
        if (!(data instanceof int[])) return;

        int[] stockData = (int[]) data;
        int newStock = stockData[1];
        
        int threshold = AppConfig.getInstance().getLowStockThreshold();
        
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        if (newStock <= threshold && newStock > 0) {
            priceModifiers.put(productName, 20.0); // +20% surge
            String msg = "[" + timestamp + "] 📈 YÜKSEK TALEP: " + productName + " için fiyat +%20 arttırıldı.";
            System.out.println("[DemandPricingObserver] " + msg);
            notifyListeners(msg);
        } else if (newStock >= 20) {
            priceModifiers.put(productName, -10.0); // -10% clearance
            String msg = "[" + timestamp + "] 📉 STOK FAZLASI: " + productName + " için fiyat -%10 düşürüldü.";
            System.out.println("[DemandPricingObserver] " + msg);
            notifyListeners(msg);
        } else {
            if (priceModifiers.containsKey(productName) && priceModifiers.get(productName) != 0.0) {
                String msg = "[" + timestamp + "] 🔄 NORMALE DÖNDÜ: " + productName + " fiyatı eski haline geldi.";
                notifyListeners(msg);
            }
            priceModifiers.put(productName, 0.0); // Normal price
        }
    }

    /**
     * Gets the current price modifier percentage for a given product.
     */
    public static double getModifier(String productName) {
        return priceModifiers.getOrDefault(productName, 0.0);
    }
    
    /**
     * Resets all modifiers (used for testing).
     */
    public static void resetAll() {
        priceModifiers.clear();
    }
}
