package main;

import main.models.Product;
import main.models.User;
import main.patterns.singleton.ShoppingCart;
import main.patterns.observer.*;
import main.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the E-Commerce Shopping System.
 * 
 * This application demonstrates three design patterns:
 * 1. Singleton Pattern - ShoppingCart (single instance per session)
 * 2. Decorator Pattern - Dynamic discounts, coupons, and free shipping
 * 3. Observer Pattern  - Stock change notifications + Demand-driven auction pricing
 * 
 * AUCTION SYSTEM (Observer Pattern Extension):
 *   - DemandTracker tracks product clicks/views
 *   - When demand is high + stock is low → price increases automatically
 *   - Users can bid against each other once auction activates
 *   - AuctionEventObserver logs all price changes and bids
 * 
 * Course: SEN3006 - Software Architecture
 * Semester: Spring 2026
 */
public class Main {

    public static void main(String[] args) {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Customize UI defaults for dark theme
            UIManager.put("OptionPane.background", new Color(30, 41, 59));
            UIManager.put("Panel.background", new Color(30, 41, 59));
            UIManager.put("OptionPane.messageForeground", new Color(241, 245, 249));
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        // --- Initialize Observer Pattern (Stock) ---
        StockSubject stockSubject = new StockSubject();
        StockObserver stockObserver = new StockObserver();
        NotificationObserver notificationObserver = new NotificationObserver();

        // Register stock observers with the subject
        stockSubject.registerObserver(stockObserver);
        stockSubject.registerObserver(notificationObserver);

        System.out.println("✅ Observer Pattern: StockObserver and NotificationObserver registered.");

        // --- Initialize Observer Pattern (Auction/Demand) ---
        DemandTracker demandTracker = new DemandTracker(stockSubject);
        AuctionEventObserver auctionEventObserver = new AuctionEventObserver();

        // Register auction observer
        demandTracker.registerAuctionObserver(auctionEventObserver);

        System.out.println("✅ Observer Pattern: AuctionEventObserver registered for demand tracking.");

        // --- Create Sample Products ---
        List<Product> products = createSampleProducts();

        // Initialize stock levels and demand tracking for all products
        for (Product product : products) {
            stockSubject.initializeStock(product.getName(), product.getStock());
            demandTracker.initializeProduct(product.getName(), product.getPrice());
        }

        System.out.println("✅ " + products.size() + " products loaded into catalog.");
        System.out.println("✅ DemandTracker initialized for all products.");

        // --- Verify Singleton Pattern ---
        ShoppingCart cart1 = ShoppingCart.getInstance();
        ShoppingCart cart2 = ShoppingCart.getInstance();
        System.out.println("✅ Singleton Pattern: cart1 == cart2 → " + (cart1 == cart2));

        // --- Create User ---
        User currentUser = new User(1, "Demo Kullanıcı", "demo@example.com");
        System.out.println("✅ User created: " + currentUser);

        // --- Launch UI ---
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(products, stockSubject, stockObserver,
                    notificationObserver, demandTracker, auctionEventObserver);
            frame.setVisible(true);
            System.out.println("✅ UI launched successfully.");
            System.out.println("✅ Auction system ready - click products to increase demand!");
        });
    }

    /**
     * Create sample product catalog with diverse categories.
     */
    private static List<Product> createSampleProducts() {
        List<Product> products = new ArrayList<>();

        // Elektronik
        products.add(new Product(1, "Laptop", "Yüksek performanslı dizüstü bilgisayar", 15999.99, 10, "Elektronik"));
        products.add(new Product(2, "Akıllı Telefon", "Son model akıllı telefon", 12499.99, 15, "Elektronik"));
        products.add(new Product(3, "Tablet", "10 inç ekranlı tablet", 7999.99, 8, "Elektronik"));
        products.add(new Product(4, "Kulaklık", "Kablosuz bluetooth kulaklık", 899.99, 25, "Elektronik"));
        products.add(new Product(5, "Akıllı Saat", "Fitness takipli akıllı saat", 3499.99, 12, "Elektronik"));

        // Giyim
        products.add(new Product(6, "Tişört", "Pamuklu basic tişört", 199.99, 50, "Giyim"));
        products.add(new Product(7, "Kot Pantolon", "Slim fit kot pantolon", 599.99, 30, "Giyim"));
        products.add(new Product(8, "Spor Ayakkabı", "Günlük spor ayakkabı", 1299.99, 20, "Giyim"));
        products.add(new Product(9, "Mont", "Su geçirmez kışlık mont", 2499.99, 5, "Giyim"));

        // Ev & Yaşam
        products.add(new Product(10, "Kahve Makinesi", "Otomatik espresso makinesi", 4999.99, 7, "Ev & Yaşam"));
        products.add(new Product(11, "Robot Süpürge", "Akıllı robot süpürge", 6999.99, 4, "Ev & Yaşam"));
        products.add(new Product(12, "Masa Lambası", "LED masa lambası", 349.99, 35, "Ev & Yaşam"));

        // Kitap
        products.add(new Product(13, "Design Patterns (GoF)", "Gang of Four tasarım kalıpları", 249.99, 18, "Kitap"));
        products.add(new Product(14, "Clean Code", "Robert C. Martin temiz kod", 199.99, 22, "Kitap"));
        products.add(new Product(15, "Head First Design Patterns", "Görsel tasarım kalıpları", 279.99, 3, "Kitap"));

        return products;
    }
}
