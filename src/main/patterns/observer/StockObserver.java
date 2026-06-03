package main.patterns.observer;

import java.util.ArrayList;
import java.util.List;
import main.patterns.singleton.AppConfig;

/**
 * Concrete Observer - Stock Monitor
 * 
 * Monitors stock levels and logs all changes.
 * Generates warnings for low-stock and out-of-stock situations.
 * Only handles STOCK_CHANGED events.
 * 
 * Uses AppConfig (Singleton) to read lowStockThreshold setting.
 */
public class StockObserver implements Observer {
    private final List<String> stockLog;

    public StockObserver() {
        this.stockLog = new ArrayList<>();
    }

    @Override
    public void update(String eventType, String productName, Object data) {
        if (!"STOCK_CHANGED".equals(eventType)) return;
        if (!(data instanceof int[])) return;

        int[] stockData = (int[]) data;
        int oldStock = stockData[0];
        int newStock = stockData[1];

        // Threshold değerini Singleton AppConfig'den oku
        int threshold = AppConfig.getInstance().getLowStockThreshold();

        String logEntry;

        if (newStock == 0) {
            logEntry = "⚠️ STOK TÜKENDI: " + productName + " (Önceki: " + oldStock + ")";
        } else if (newStock <= threshold) {
            logEntry = "⚡ DÜŞÜK STOK: " + productName + " - Kalan: " + newStock + " adet";
        } else if (newStock > oldStock) {
            logEntry = "📦 STOK GÜNCELLENDİ: " + productName + " (" + oldStock + " → " + newStock + ")";
        } else {
            logEntry = "📉 STOK AZALDI: " + productName + " (" + oldStock + " → " + newStock + ")";
        }

        stockLog.add(logEntry);
        System.out.println("[StockObserver] " + logEntry);
    }

    public List<String> getStockLog() {
        return new ArrayList<>(stockLog);
    }

    public String getLastLog() {
        return stockLog.isEmpty() ? "" : stockLog.get(stockLog.size() - 1);
    }

    public void clearLog() {
        stockLog.clear();
    }
}
