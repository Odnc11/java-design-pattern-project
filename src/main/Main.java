package main;

import main.ui.MainFrame;
import javax.swing.*;

/**
 * E-Commerce Shopping System — Design Patterns Demo
 * 
 * Entry point for the application.
 * Initializes the Swing UI on the Event Dispatch Thread.
 * 
 * Design Patterns Used:
 *   1. Singleton: ShoppingCart (per-user instance)
 *   2. Decorator: ProductComponent → PercentageDiscount, FixedAmount, Coupon, FreeShipping, FlashSale
 *   3. Observer: StockSubject → StockObserver, PriceNotificationObserver, AnalyticsObserver
 */
public class Main {
    public static void main(String[] args) {
        // Set look and feel for better visuals
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
