package main.patterns.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Observer - Stock Monitor
 * 
 * Monitors stock levels and logs all changes.
 * Generates warnings for low-stock and out-of-stock situations.
 */
public class StockObserver implements Observer {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private final List<String> stockLog;

    public StockObserver() {
        this.stockLog = new ArrayList<>();
    }

    @Override
    public void update(String productName, int oldStock, int newStock) {
        String logEntry;

        if (newStock == 0) {
            logEntry = "⚠️ STOK TÜKENDI: " + productName + " (Önceki: " + oldStock + ")";
        } else if (newStock <= LOW_STOCK_THRESHOLD) {
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
