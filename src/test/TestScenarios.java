package test;

import main.models.*;
import main.database.*;
import main.patterns.singleton.AppConfig;
import main.patterns.singleton.ShoppingCart;
import main.patterns.decorator.*;
import main.patterns.observer.*;
import main.utils.CouponValidator;

/**
 * Test Scenarios for all three design patterns.
 * Demonstrates and verifies correct implementation.
 * 
 * Test 1: Singleton Pattern — AppConfig (true GoF Singleton)
 * Test 2: Decorator Pattern (5 types, order matters)
 * Test 3: Observer Pattern (Stock, PriceNotification, Analytics)
 * Test 4: Integration Tests
 * Test 5: Decorator Order Proof (mathematical difference)
 */
public class TestScenarios {
    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   E-Commerce System — Test Scenarios         ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testSingletonPattern();
        testDecoratorPattern();
        testObserverPattern();
        testIntegration();
        testDecoratorOrderProof();
        testDynamicPricing();

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("Sonuçlar: " + passCount + " geçti, " + failCount + " başarısız");
        System.out.println("══════════════════════════════════════════════");
    }

    // ========================================
    // TEST 1: SINGLETON PATTERN — AppConfig
    // ========================================
    private static void testSingletonPattern() {
        System.out.println("─── TEST 1: Singleton Pattern — AppConfig ───");
        AppConfig.resetInstance();

        // Test 1.1: getInstance() her zaman AYNI instance döner
        AppConfig config1 = AppConfig.getInstance();
        AppConfig config2 = AppConfig.getInstance();
        assertTest("1.1 Her zaman AYNI instance (config1 == config2)", config1 == config2);

        // Test 1.2: Default değerler doğru
        assertTest("1.2 Default dil: Türkçe", "Türkçe".equals(config1.getLanguage()));
        assertTest("1.2 Default tema: Dark", "Dark".equals(config1.getTheme()));
        assertTest("1.2 Default currency: ₺", "₺".equals(config1.getCurrency()));
        assertTest("1.2 Default kargo: 29.99", config1.getDefaultShippingCost() == 29.99);
        assertTest("1.2 Default lowStock threshold: 5", config1.getLowStockThreshold() == 5);

        // Test 1.3: config1 üzerinden değişiklik → config2'de de görünür
        config1.setLanguage("English");
        config1.setTheme("Light");
        assertTest("1.3 config1 dil: English", "English".equals(config1.getLanguage()));
        assertTest("1.3 config2 DİL DE AYNI (English)", "English".equals(config2.getLanguage()));
        assertTest("1.3 config2 TEMA DA AYNI (Light)", "Light".equals(config2.getTheme()));

        // Test 1.4: Singleton tüm sistem genelinde paylaşılır
        // (Lab manual'deki AppConfig örneğinin aynısı)
        AppConfig config3 = AppConfig.getInstance();
        assertTest("1.4 Üçüncü referans da AYNI obje", config1 == config3);
        assertTest("1.4 config3 de güncel dili görür (English)",
                "English".equals(config3.getLanguage()));

        // Test 1.5: Ayar değişiklikleri anlık yansır
        config2.setDefaultShippingCost(19.99);
        assertTest("1.5 Kargo config1'den okunur: 19.99",
                config1.getDefaultShippingCost() == 19.99);

        // Test 1.6: formatPrice utility
        assertTest("1.6 formatPrice doğru", "100.00₺".equals(config1.formatPrice(100.0)));
        config1.setCurrency("$");
        assertTest("1.6 Currency değişince format da değişir",
                "100.00$".equals(config1.formatPrice(100.0)));

        // Test 1.7: notificationsEnabled
        assertTest("1.7 Bildirimler varsayılan açık", config1.isNotificationsEnabled());
        config1.setNotificationsEnabled(false);
        assertTest("1.7 Bildirimler kapatıldı", !config1.isNotificationsEnabled());
        assertTest("1.7 config2 de kapalı görür", !config2.isNotificationsEnabled());

        // Reset for subsequent tests
        AppConfig.resetInstance();

        System.out.println();

        // --- ShoppingCart: Per-User Scoped (NOT Singleton) ---
        System.out.println("─── TEST 1B: ShoppingCart — Per-User Scoped ───");
        ShoppingCart.resetAllInstances();

        ShoppingCart cart1a = ShoppingCart.getInstance(10);
        ShoppingCart cart1b = ShoppingCart.getInstance(10);
        assertTest("1B.1 Aynı buyer aynı sepet döner", cart1a == cart1b);

        ShoppingCart cart2 = ShoppingCart.getInstance(11);
        assertTest("1B.2 Farklı buyer FARKLI sepet", cart1a != cart2);

        assertTest("1B.3 Sepet boş başlar", cart1a.isEmpty());

        Product laptop = new Product(1, "Laptop", "Test laptop", 1000.0, 10, "Elektronik");
        ProductComponent decorated = new ConcreteProduct(laptop);
        cart1a.addItem(decorated, laptop);
        assertTest("1B.4 Buyer10 sepetine eklendi", cart1a.getItemCount() == 1);
        assertTest("1B.5 cart1b de aynı veriyi görür", cart1b.getItemCount() == 1);
        assertTest("1B.6 Buyer11 sepeti BOŞ (farklı scope)", cart2.isEmpty());

        ShoppingCart.clearInstance(10);
        ShoppingCart newCart = ShoppingCart.getInstance(10);
        assertTest("1B.7 Clear sonrası yeni sepet boş", newCart.isEmpty());

        ShoppingCart.resetAllInstances();
        System.out.println();
    }

    // ========================================
    // TEST 2: DECORATOR PATTERN (5 types)
    // ========================================
    private static void testDecoratorPattern() {
        System.out.println("─── TEST 2: Decorator Pattern (5 Tür) ───");

        Product phone = new Product(2, "Telefon", "Akıllı telefon", 100.0, 5, "Elektronik");

        // Test 2.1: ConcreteProduct
        ProductComponent base = new ConcreteProduct(phone);
        assertTest("2.1 Base fiyat 100₺", base.getPrice() == 100.0);
        assertTest("2.1 Base isim doğru", "Telefon".equals(base.getName()));
        assertTest("2.1 Free shipping yok", !base.hasFreeShipping());

        // Test 2.2: PercentageDiscountDecorator (%10)
        ProductComponent pctDiscount = new PercentageDiscountDecorator(base, 10);
        assertTest("2.2 %10 indirim: 90₺", pctDiscount.getPrice() == 90.0);
        assertTest("2.2 Açıklama indirim içerir",
                pctDiscount.getDescription().contains("%10 indirim"));

        // Test 2.3: FixedAmountDiscountDecorator (20₺)
        ProductComponent fixedDiscount = new FixedAmountDiscountDecorator(base, 20);
        assertTest("2.3 20₺ sabit indirim: 80₺", fixedDiscount.getPrice() == 80.0);
        assertTest("2.3 Açıklama indirim içerir",
                fixedDiscount.getDescription().contains("-20₺ indirim"));

        // Test 2.4: CouponDecorator (30₺)
        ProductComponent withCoupon = new CouponDecorator(pctDiscount, 30, "TEST30");
        assertTest("2.4 Kupon sonrası: 60₺", withCoupon.getPrice() == 60.0);
        assertTest("2.4 Kupon kodu açıklamada",
                withCoupon.getDescription().contains("TEST30"));

        // Test 2.5: FreeShippingDecorator
        ProductComponent withShipping = new FreeShippingDecorator(withCoupon);
        assertTest("2.5 Fiyat değişmedi: 60₺", withShipping.getPrice() == 60.0);
        assertTest("2.5 Ücretsiz kargo aktif", withShipping.hasFreeShipping());
        assertTest("2.5 Kargo maliyeti 0", withShipping.getShippingCost() == 0);
        assertTest("2.5 Kargo açıklamada",
                withShipping.getDescription().contains("Ücretsiz Kargo"));

        // Test 2.6: FlashSaleDecorator (%10)
        ProductComponent flashSale = new FlashSaleDecorator(base);
        assertTest("2.6 Flash sale: 90₺", flashSale.getPrice() == 90.0);
        assertTest("2.6 Flash açıklamada",
                flashSale.getDescription().contains("Flash Sale"));

        // Test 2.7: Coupon doesn't go below 0
        ProductComponent cheapProduct = new ConcreteProduct(
                new Product(3, "Ucuz", "Test", 10.0, 1, "Test"));
        ProductComponent bigCoupon = new CouponDecorator(cheapProduct, 50, "BIG");
        assertTest("2.7 Fiyat 0'ın altına düşmez", bigCoupon.getPrice() == 0);

        // Test 2.8: Multiple percentage discounts stack
        ProductComponent doubleDiscount = new PercentageDiscountDecorator(
                new PercentageDiscountDecorator(base, 10), 20);
        // 100 * 0.9 * 0.8 = 72
        assertTest("2.8 Çoklu indirim: 72₺", doubleDiscount.getPrice() == 72.0);

        // Test 2.9: Full decorator chain
        // 100₺ → %10 = 90₺ → -30₺ kupon = 60₺ → free shipping = 60₺
        System.out.println("  Decorator Zinciri:");
        System.out.println("    Orijinal:         100.00₺");
        System.out.println("    %10 indirim:       90.00₺");
        System.out.println("    Kupon -30₺:        60.00₺");
        System.out.println("    + Ücretsiz Kargo");
        assertTest("2.9 Zincir sonucu doğru", withShipping.getPrice() == 60.0);

        System.out.println();
    }

    // ========================================
    // TEST 3: OBSERVER PATTERN
    // ========================================
    private static void testObserverPattern() {
        System.out.println("─── TEST 3: Observer Pattern (3 Observer) ───");

        StockSubject subject = new StockSubject();
        StockObserver stockObs = new StockObserver();
        PriceNotificationObserver priceObs = new PriceNotificationObserver();
        AnalyticsObserver analyticsObs = new AnalyticsObserver();

        // Test 3.1: Register observers
        subject.registerObserver(stockObs);
        subject.registerObserver(priceObs);
        subject.registerObserver(analyticsObs);
        assertTest("3.1 Observer'lar kaydedildi", true);

        // Test 3.2: Initialize stock (no notification)
        subject.initializeStock("Laptop", 10);
        assertTest("3.2 Stok başlatıldı", subject.getStock("Laptop") == 10);
        assertTest("3.2 StockObserver bildirim yok (init)", stockObs.getStockLog().isEmpty());

        // Test 3.3: Stock decrease triggers all observers
        subject.decreaseStock("Laptop", 1);
        assertTest("3.3 Stok azaldı: 9", subject.getStock("Laptop") == 9);
        assertTest("3.3 StockObserver bildirim aldı", !stockObs.getStockLog().isEmpty());
        assertTest("3.3 AnalyticsObserver bildirim aldı", analyticsObs.getTotalEventCount() > 0);

        // Test 3.4: Low stock warning
        subject.setStock("Laptop", 3);
        String lastLog = stockObs.getLastLog();
        assertTest("3.4 Düşük stok uyarısı", lastLog.contains("DÜŞÜK STOK"));

        // Test 3.5: Out of stock
        subject.setStock("Laptop", 0);
        lastLog = stockObs.getLastLog();
        assertTest("3.5 Stok tükendi uyarısı", lastLog.contains("STOK TÜKENDI"));

        // Test 3.6: Price change notification
        subject.initializePrice("Laptop", 10000.0);
        subject.setPrice("Laptop", 8500.0);
        assertTest("3.6 PriceNotification bildirim aldı", !priceObs.getNotifications().isEmpty());
        assertTest("3.6 Analytics fiyat kaydı",
                analyticsObs.getEntriesByType("PRICE_CHANGED").size() > 0);

        // Test 3.7: Discount notification
        subject.notifyDiscountAdded("Laptop", "%15 indirim");
        assertTest("3.7 İndirim bildirimi alındı",
                analyticsObs.getEntriesByType("DISCOUNT_ADDED").size() > 0);

        // Test 3.8: Remove observer
        int logSizeBefore = stockObs.getStockLog().size();
        subject.removeObserver(stockObs);
        subject.setStock("Laptop", 20);
        assertTest("3.8 Çıkarılan observer bildirim almaz",
                stockObs.getStockLog().size() == logSizeBefore);

        // Test 3.9: Stock increase
        subject.increaseStock("Laptop", 5);
        assertTest("3.9 Stok arttı: 25", subject.getStock("Laptop") == 25);

        System.out.println();
    }

    // ========================================
    // TEST 4: INTEGRATION
    // ========================================
    private static void testIntegration() {
        System.out.println("─── TEST 4: Entegrasyon Testleri ───");
        ShoppingCart.resetAllInstances();

        StockSubject subject = new StockSubject();
        StockObserver stockObs = new StockObserver();
        subject.registerObserver(stockObs);

        Product product = new Product(10, "Test Ürün", "Entegrasyon testi", 200.0, 10, "Test");
        subject.initializeStock(product.getName(), product.getStock());

        // Test 4.1: Add to cart (Buyer 10) + decrease stock
        ShoppingCart cart = ShoppingCart.getInstance(10);
        ProductComponent comp = new ConcreteProduct(product);
        cart.addItem(comp, product);
        subject.decreaseStock(product.getName(), 1);
        assertTest("4.1 Sepet + Stok uyumlu",
                cart.getItemCount() == 1 && subject.getStock("Test Ürün") == 9);

        // Test 4.2: Apply decorator chain and check cart total
        ProductComponent decorated = new PercentageDiscountDecorator(comp, 10); // 200 * 0.9 = 180
        decorated = new CouponDecorator(decorated, 30, "TEST30");     // 180 - 30 = 150
        decorated = new FreeShippingDecorator(decorated);

        cart.clear();
        cart.addItem(decorated, product, 2);
        assertTest("4.2 Decorator zinciri toplam: 300₺", cart.getTotal() == 300.0);
        assertTest("4.2 Kargo ücretsiz", cart.getShippingCost() == 0);
        assertTest("4.2 Grand total: 300₺", cart.getGrandTotal() == 300.0);

        // Test 4.3: Different buyers have separate carts
        ShoppingCart cart2 = ShoppingCart.getInstance(11);
        assertTest("4.3 Buyer11 sepeti boş", cart2.isEmpty());
        assertTest("4.3 Buyer10 sepeti dolu", !cart.isEmpty());

        // Test 4.4: User model
        Seller seller = new Seller(1, "TestSeller", "test@seller.com", "123", "TestStore");
        assertTest("4.4 Seller oluşturuldu", "TestSeller".equals(seller.getName()));
        assertTest("4.4 Seller role doğru", seller.getRole() == main.models.User.Role.SELLER);

        Buyer buyer = new Buyer(10, "TestBuyer", "test@buyer.com", "123");
        assertTest("4.4 Buyer oluşturuldu", "TestBuyer".equals(buyer.getName()));
        assertTest("4.4 Buyer role doğru", buyer.getRole() == main.models.User.Role.BUYER);

        // Test 4.5: CouponValidator
        CouponValidator.CouponInfo coupon = CouponValidator.validate("WELCOME10");
        assertTest("4.5 WELCOME10 kupon geçerli", coupon != null);
        assertTest("4.5 Kupon type PERCENTAGE",
                coupon != null && coupon.getType() == CouponValidator.CouponType.PERCENTAGE);

        CouponValidator.CouponInfo invalid = CouponValidator.validate("INVALID");
        assertTest("4.5 Geçersiz kupon null", invalid == null);

        ShoppingCart.resetAllInstances();
        System.out.println();
    }

    // ========================================
    // TEST 5: DECORATOR ORDER PROOF
    // ========================================
    private static void testDecoratorOrderProof() {
        System.out.println("─── TEST 5: Decorator Sırası Kanıtı ───");

        Product laptop = new Product(1, "Laptop", "Test", 10000.0, 10, "Elektronik");
        ProductComponent base = new ConcreteProduct(laptop);

        // Order 1: %15 → 500₺
        ProductComponent order1 = new PercentageDiscountDecorator(base, 15);  // 10000 * 0.85 = 8500
        order1 = new FixedAmountDiscountDecorator(order1, 500);               // 8500 - 500 = 8000
        double price1 = order1.getPrice();

        // Order 2: 500₺ → %15
        ProductComponent order2 = new FixedAmountDiscountDecorator(base, 500); // 10000 - 500 = 9500
        order2 = new PercentageDiscountDecorator(order2, 15);                  // 9500 * 0.85 = 8075
        double price2 = order2.getPrice();

        System.out.println("  Laptop: 10000₺");
        System.out.println("  Sıra 1 (%15 → 500₺): " + String.format("%.2f₺", price1));
        System.out.println("  Sıra 2 (500₺ → %15): " + String.format("%.2f₺", price2));
        System.out.println("  Fark: " + String.format("%.2f₺", Math.abs(price1 - price2)));

        assertTest("5.1 Sıra 1: %15 → 500₺ = 8000₺", price1 == 8000.0);
        assertTest("5.2 Sıra 2: 500₺ → %15 = 8075₺", price2 == 8075.0);
        assertTest("5.3 Decorator sırası MATEMATİKSEL FARK oluşturur", price1 != price2);
        assertTest("5.4 Fark = 75₺", Math.abs(price1 - price2) == 75.0);

        // More complex chain
        // 10000 → %15 → -500₺ → Flash(%10) → Free Shipping
        ProductComponent complex = new PercentageDiscountDecorator(base, 15);      // 8500
        complex = new FixedAmountDiscountDecorator(complex, 500);                   // 8000
        complex = new FlashSaleDecorator(complex);                                  // 8000 * 0.9 = 7200
        complex = new FreeShippingDecorator(complex);                               // 7200 + ücretsiz kargo

        System.out.println("\n  Kompleks Zincir:");
        System.out.println("    10000₺ → %15 = 8500₺");
        System.out.println("    8500₺ → -500₺ = 8000₺");
        System.out.println("    8000₺ → Flash %10 = 7200₺");
        System.out.println("    + Ücretsiz Kargo");
        System.out.println("    Final: " + String.format("%.2f₺", complex.getPrice()));

        assertTest("5.5 Kompleks zincir: 7200₺", complex.getPrice() == 7200.0);
        assertTest("5.6 Ücretsiz kargo aktif", complex.hasFreeShipping());
        assertTest("5.7 Kargo maliyeti 0", complex.getShippingCost() == 0);

        System.out.println();
    }

    // ========================================
    // TEST 6: DYNAMIC PRICING (ARZ-TALEP)
    // ========================================
    private static void testDynamicPricing() {
        System.out.println("─── TEST 6: Dinamik Fiyatlandırma (Arz-Talep) ───");
        
        main.patterns.observer.DemandPricingObserver.resetAll();
        main.patterns.observer.StockSubject subject = new main.patterns.observer.StockSubject();
        main.patterns.observer.DemandPricingObserver observer = new main.patterns.observer.DemandPricingObserver();
        subject.registerObserver(observer);
        
        Product product = new Product(99, "Dinamik Ürün", "Arz-Talep Testi", 1000.0, 30, "Test");
        subject.initializeStock(product.getName(), product.getStock());
        
        main.patterns.decorator.ProductComponent base = new main.patterns.decorator.ConcreteProduct(product);
        main.patterns.decorator.DynamicPricingDecorator dynamicProduct = new main.patterns.decorator.DynamicPricingDecorator(base, product.getName());
        
        // Test 6.1: Overstock (Stock = 30 >= 20) -> -10% -> 900.0
        subject.decreaseStock(product.getName(), 1); // Trigger event
        assertTest("6.1 Stok 29 -> %10 İndirim (900₺)", dynamicProduct.getPrice() == 900.0);
        assertTest("6.2 Açıklamada Stok Fazlası etiketi var", dynamicProduct.getDescription().contains("Stok Fazlası"));
        
        // Test 6.3: Normal Stock (Stock = 15) -> 1000.0
        subject.decreaseStock(product.getName(), 14); // 29 - 14 = 15
        assertTest("6.3 Stok 15 -> Normal Fiyat (1000₺)", dynamicProduct.getPrice() == 1000.0);
        
        // Test 6.4: Low Stock / High Demand (Stock = 5) -> +20% -> 1200.0
        subject.decreaseStock(product.getName(), 10); // 15 - 10 = 5
        assertTest("6.4 Stok 5 (Yüksek Talep) -> +%20 Fiyat (1200₺)", dynamicProduct.getPrice() == 1200.0);
        assertTest("6.5 Açıklamada Yüksek Talep etiketi var", dynamicProduct.getDescription().contains("Yüksek Talep"));
        
        System.out.println();
    }

    // ========================================
    // HELPER
    // ========================================
    private static void assertTest(String testName, boolean condition) {
        if (condition) {
            System.out.println("  ✅ PASS: " + testName);
            passCount++;
        } else {
            System.out.println("  ❌ FAIL: " + testName);
            failCount++;
        }
    }
}
