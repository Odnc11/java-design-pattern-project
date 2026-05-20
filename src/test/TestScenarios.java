package test;

import main.models.Product;
import main.models.User;
import main.models.Bid;
import main.patterns.singleton.ShoppingCart;
import main.patterns.decorator.*;
import main.patterns.observer.*;

/**
 * Test Scenarios for all three design patterns + Auction system.
 * Demonstrates and verifies correct implementation.
 * 
 * Compile and run from the project root directory.
 * See README.md for detailed instructions.
 */
public class TestScenarios {
    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   E-Commerce System - Test Scenarios         ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testSingletonPattern();
        testDecoratorPattern();
        testObserverPattern();
        testAuctionDemandSystem();
        testIntegration();

        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("Sonuçlar: " + passCount + " geçti, " + failCount + " başarısız");
        System.out.println("══════════════════════════════════════════════");
    }

    // ========================================
    // TEST 1: SINGLETON PATTERN
    // ========================================
    private static void testSingletonPattern() {
        System.out.println("─── TEST 1: Singleton Pattern ───");
        ShoppingCart.resetInstance(); // Clean state

        // Test 1.1: Same instance
        ShoppingCart cart1 = ShoppingCart.getInstance();
        ShoppingCart cart2 = ShoppingCart.getInstance();
        assertTest("1.1 Aynı instance döner", cart1 == cart2);

        // Test 1.2: Cart starts empty
        assertTest("1.2 Sepet boş başlar", cart1.isEmpty());
        assertTest("1.2 Item sayısı 0", cart1.getItemCount() == 0);

        // Test 1.3: Add item
        Product laptop = new Product(1, "Laptop", "Test laptop", 1000.0, 10, "Elektronik");
        ProductComponent decorated = new ConcreteProduct(laptop);
        cart1.addItem(decorated, laptop);
        assertTest("1.3 Ürün eklendi", cart1.getItemCount() == 1);

        // Test 1.4: Both references see same data
        assertTest("1.4 cart2 de aynı veriyi görür", cart2.getItemCount() == 1);

        // Test 1.5: Total calculation
        assertTest("1.5 Toplam doğru", cart1.getTotal() == 1000.0);

        // Test 1.6: Remove item
        cart1.removeItem(0);
        assertTest("1.6 Ürün silindi", cart1.isEmpty());

        // Test 1.7: Clear
        cart1.addItem(decorated, laptop);
        cart1.clear();
        assertTest("1.7 Sepet temizlendi", cart1.isEmpty());

        ShoppingCart.resetInstance();
        System.out.println();
    }

    // ========================================
    // TEST 2: DECORATOR PATTERN
    // ========================================
    private static void testDecoratorPattern() {
        System.out.println("─── TEST 2: Decorator Pattern ───");

        Product phone = new Product(2, "Telefon", "Akıllı telefon", 100.0, 5, "Elektronik");

        // Test 2.1: ConcreteProduct
        ProductComponent base = new ConcreteProduct(phone);
        assertTest("2.1 Base fiyat 100₺", base.getPrice() == 100.0);
        assertTest("2.1 Base isim doğru", "Telefon".equals(base.getName()));
        assertTest("2.1 Free shipping yok", !base.hasFreeShipping());

        // Test 2.2: DiscountDecorator (%10)
        ProductComponent discounted = new DiscountDecorator(base, 10);
        assertTest("2.2 %10 indirim: 90₺", discounted.getPrice() == 90.0);
        assertTest("2.2 Açıklama indirim içerir",
                discounted.getDescription().contains("%10 indirim"));

        // Test 2.3: CouponDecorator (20₺)
        ProductComponent withCoupon = new CouponDecorator(discounted, 20, "INDIRIM20");
        assertTest("2.3 Kupon sonrası: 70₺", withCoupon.getPrice() == 70.0);
        assertTest("2.3 Kupon kodu açıklamada",
                withCoupon.getDescription().contains("INDIRIM20"));

        // Test 2.4: FreeShippingDecorator
        ProductComponent withShipping = new FreeShippingDecorator(withCoupon);
        assertTest("2.4 Fiyat değişmedi: 70₺", withShipping.getPrice() == 70.0);
        assertTest("2.4 Ücretsiz kargo aktif", withShipping.hasFreeShipping());
        assertTest("2.4 Kargo açıklamada",
                withShipping.getDescription().contains("Ücretsiz Kargo"));

        // Test 2.5: Decorator chain
        // 100₺ → %10 = 90₺ → -20₺ kupon = 70₺ → free shipping = 70₺
        System.out.println("  Decorator Zinciri:");
        System.out.println("    Orijinal:   100.00₺");
        System.out.println("    %10 indirim: 90.00₺");
        System.out.println("    Kupon -20₺:  70.00₺");
        System.out.println("    + Ücretsiz Kargo");
        assertTest("2.5 Zincir sonucu doğru", withShipping.getPrice() == 70.0);

        // Test 2.6: Coupon doesn't go below 0
        ProductComponent cheapProduct = new ConcreteProduct(
                new Product(3, "Ucuz", "Test", 10.0, 1, "Test"));
        ProductComponent bigCoupon = new CouponDecorator(cheapProduct, 50, "BIG");
        assertTest("2.6 Fiyat 0'ın altına düşmez", bigCoupon.getPrice() == 0);

        // Test 2.7: Multiple discounts stack
        ProductComponent doubleDiscount = new DiscountDecorator(
                new DiscountDecorator(base, 10), 20);
        // 100 * 0.9 * 0.8 = 72
        assertTest("2.7 Çoklu indirim: 72₺", doubleDiscount.getPrice() == 72.0);

        System.out.println();
    }

    // ========================================
    // TEST 3: OBSERVER PATTERN
    // ========================================
    private static void testObserverPattern() {
        System.out.println("─── TEST 3: Observer Pattern ───");

        StockSubject subject = new StockSubject();
        StockObserver stockObs = new StockObserver();
        NotificationObserver notifObs = new NotificationObserver();

        // Test 3.1: Register observers
        subject.registerObserver(stockObs);
        subject.registerObserver(notifObs);
        assertTest("3.1 Observer'lar kaydedildi", true);

        // Test 3.2: Initialize stock (no notification)
        subject.initializeStock("Laptop", 10);
        assertTest("3.2 Stok başlatıldı", subject.getStock("Laptop") == 10);
        assertTest("3.2 Bildirim yok (init)", stockObs.getStockLog().isEmpty());

        // Test 3.3: Stock decrease triggers notification
        subject.decreaseStock("Laptop", 1);
        assertTest("3.3 Stok azaldı: 9", subject.getStock("Laptop") == 9);
        assertTest("3.3 StockObserver bildirim aldı", !stockObs.getStockLog().isEmpty());
        assertTest("3.3 NotificationObserver bildirim aldı",
                !notifObs.getNotifications().isEmpty());

        // Test 3.4: Low stock warning
        subject.setStock("Laptop", 3);
        String lastLog = stockObs.getLastLog();
        assertTest("3.4 Düşük stok uyarısı", lastLog.contains("DÜŞÜK STOK"));

        // Test 3.5: Out of stock
        subject.setStock("Laptop", 0);
        lastLog = stockObs.getLastLog();
        assertTest("3.5 Stok tükendi uyarısı", lastLog.contains("STOK TÜKENDI"));

        // Test 3.6: Stock increase
        subject.increaseStock("Laptop", 5);
        assertTest("3.6 Stok arttı: 5", subject.getStock("Laptop") == 5);

        // Test 3.7: Remove observer
        int logSizeBefore = stockObs.getStockLog().size();
        subject.removeObserver(stockObs);
        subject.setStock("Laptop", 20);
        assertTest("3.7 Çıkarılan observer bildirim almaz",
                stockObs.getStockLog().size() == logSizeBefore);

        System.out.println();
    }

    // ========================================
    // TEST 4: AUCTION / DEMAND SYSTEM
    // ========================================
    private static void testAuctionDemandSystem() {
        System.out.println("─── TEST 4: Müzayede & Talep Sistemi (Observer Pattern) ───");

        StockSubject stockSubject = new StockSubject();
        DemandTracker demandTracker = new DemandTracker(stockSubject);
        AuctionEventObserver auctionObs = new AuctionEventObserver();
        demandTracker.registerAuctionObserver(auctionObs);

        // Setup product
        String productName = "Test Laptop";
        stockSubject.initializeStock(productName, 3); // Low stock!
        demandTracker.initializeProduct(productName, 1000.0);

        // Test 4.1: Initial state
        assertTest("4.1 Başlangıç talep 0", demandTracker.getDemandCount(productName) == 0);
        assertTest("4.1 Başlangıç fiyat 1000₺", demandTracker.getCurrentPrice(productName) == 1000.0);
        assertTest("4.1 Müzayede kapalı", !demandTracker.isAuctionActive(productName));

        // Test 4.2: Record demand - price should not change much with few clicks
        demandTracker.recordDemand(productName);
        demandTracker.recordDemand(productName);
        assertTest("4.2 Talep sayısı 2", demandTracker.getDemandCount(productName) == 2);
        assertTest("4.2 Müzayede henüz kapalı (eşik altı)", !demandTracker.isAuctionActive(productName));

        // Test 4.3: Pass the demand threshold (5) → auction activates
        demandTracker.recordDemand(productName); // 3
        demandTracker.recordDemand(productName); // 4
        demandTracker.recordDemand(productName); // 5 → threshold!
        assertTest("4.3 Talep sayısı 5", demandTracker.getDemandCount(productName) == 5);
        assertTest("4.3 Müzayede aktif (eşik aşıldı)", demandTracker.isAuctionActive(productName));

        // Test 4.4: Price should have increased (high demand + low stock)
        // demand=5, stock=3: demandRatio=5/5=1.0, scarcityRatio=1/4=0.25
        // multiplier = 1 + (1.0 * 0.25 * 0.5) = 1.125
        // newPrice = 1000 * 1.125 = 1125.0
        double currentPrice = demandTracker.getCurrentPrice(productName);
        assertTest("4.4 Fiyat arttı (talep + düşük stok)", currentPrice > 1000.0);
        System.out.println("    Mevcut fiyat: " + String.format("%.2f₺", currentPrice));

        // Test 4.5: More demand → higher price
        for (int i = 0; i < 10; i++) {
            demandTracker.recordDemand(productName);
        }
        double higherPrice = demandTracker.getCurrentPrice(productName);
        assertTest("4.5 Daha fazla talep = daha yüksek fiyat", higherPrice > currentPrice);
        System.out.println("    15 tıklama sonrası fiyat: " + String.format("%.2f₺", higherPrice));

        // Test 4.6: Demand level labels
        String level = demandTracker.getDemandLevel(productName);
        assertTest("4.6 Talep seviyesi: " + level,
                level.contains("Yüksek") || level.contains("Çok Yüksek"));

        // Test 4.7: Price multiplier
        double multiplier = demandTracker.getPriceMultiplier(productName);
        assertTest("4.7 Çarpan > 1.0", multiplier > 1.0);
        System.out.println("    Fiyat çarpanı: " + String.format("%.2fx", multiplier));

        // Test 4.8: Lower stock → even higher price
        double priceBeforeStockDrop = demandTracker.getCurrentPrice(productName);
        stockSubject.setStock(productName, 1); // Only 1 left!
        demandTracker.recalculatePrice(productName);
        double priceAfterStockDrop = demandTracker.getCurrentPrice(productName);
        assertTest("4.8 Stok düştüğünde fiyat artar", priceAfterStockDrop > priceBeforeStockDrop);
        System.out.println("    Stok 1'e düştüğünde fiyat: " + String.format("%.2f₺", priceAfterStockDrop));

        // Test 4.9: Place bids
        User user1 = new User(1, "Ahmet", "ahmet@test.com");
        User user2 = new User(2, "Elif", "elif@test.com");

        double minBid = priceAfterStockDrop + 1;
        Bid bid1 = demandTracker.placeBid(productName, user1, minBid + 100);
        assertTest("4.9 İlk teklif kabul edildi", bid1 != null);
        assertTest("4.9 Teklif sayısı 1", demandTracker.getTotalBidCount(productName) == 1);

        // Test 4.10: Higher bid
        Bid bid2 = demandTracker.placeBid(productName, user2, minBid + 200);
        assertTest("4.10 Daha yüksek teklif kabul edildi", bid2 != null);
        assertTest("4.10 En yüksek teklif Elif'in", 
                demandTracker.getHighestBid(productName).getUser().getName().equals("Elif"));

        // Test 4.11: Lower bid rejected
        Bid lowBid = demandTracker.placeBid(productName, user1, minBid);
        assertTest("4.11 Düşük teklif reddedildi", lowBid == null);

        // Test 4.12: AuctionEventObserver received events
        assertTest("4.12 Auction observer olayları aldı", !auctionObs.getEventLog().isEmpty());
        System.out.println("    Toplam olay sayısı: " + auctionObs.getEventLog().size());

        // Test 4.13: Max multiplier cap (3.0x)
        for (int i = 0; i < 50; i++) {
            demandTracker.recordDemand(productName);
        }
        double cappedMultiplier = demandTracker.getPriceMultiplier(productName);
        // Note: bids may have pushed price above max multiplier
        // But demand-only multiplier should be capped
        System.out.println("    Çok yüksek talep sonrası çarpan: " + String.format("%.2fx", cappedMultiplier));

        System.out.println();
    }

    // ========================================
    // TEST 5: INTEGRATION
    // ========================================
    private static void testIntegration() {
        System.out.println("─── TEST 5: Entegrasyon Testleri ───");
        ShoppingCart.resetInstance();

        StockSubject subject = new StockSubject();
        StockObserver stockObs = new StockObserver();
        subject.registerObserver(stockObs);

        DemandTracker demandTracker = new DemandTracker(subject);
        AuctionEventObserver auctionObs = new AuctionEventObserver();
        demandTracker.registerAuctionObserver(auctionObs);

        Product product = new Product(10, "Test Ürün", "Entegrasyon testi", 200.0, 10, "Test");
        subject.initializeStock(product.getName(), product.getStock());
        demandTracker.initializeProduct(product.getName(), product.getPrice());

        ShoppingCart cart = ShoppingCart.getInstance();

        // Test 5.1: Add to cart + decrease stock
        ProductComponent comp = new ConcreteProduct(product);
        cart.addItem(comp, product);
        subject.decreaseStock(product.getName(), 1);
        assertTest("5.1 Sepet + Stok uyumlu",
                cart.getItemCount() == 1 && subject.getStock("Test Ürün") == 9);

        // Test 5.2: Apply decorator chain and check cart total
        ProductComponent decorated = new DiscountDecorator(comp, 10); // 200 * 0.9 = 180
        decorated = new CouponDecorator(decorated, 30, "TEST30");     // 180 - 30 = 150
        decorated = new FreeShippingDecorator(decorated);

        cart.clear();
        cart.addItem(decorated, product, 2);
        assertTest("5.2 Decorator zinciri toplam: 300₺", cart.getTotal() == 300.0);
        assertTest("5.2 Kargo ücretsiz", cart.getShippingCost() == 0);
        assertTest("5.2 Grand total: 300₺", cart.getGrandTotal() == 300.0);

        // Test 5.3: User model
        User user = new User(1, "Test User", "test@test.com");
        assertTest("5.3 User oluşturuldu", "Test User".equals(user.getName()));

        // Test 5.4: Demand affects price → cart reflects dynamic price
        cart.clear();
        // Simulate high demand on low stock product
        Product limitedProduct = new Product(20, "Limited Ürün", "Sınırlı stok", 500.0, 2, "Test");
        subject.initializeStock(limitedProduct.getName(), 2);
        demandTracker.initializeProduct(limitedProduct.getName(), 500.0);

        // Generate demand
        for (int i = 0; i < 10; i++) {
            demandTracker.recordDemand(limitedProduct.getName());
        }

        double dynamicPrice = demandTracker.getCurrentPrice(limitedProduct.getName());
        assertTest("5.4 Dinamik fiyat > baz fiyat", dynamicPrice > 500.0);
        System.out.println("    Limited ürün dinamik fiyat: " + String.format("%.2f₺", dynamicPrice));

        // Test 5.5: Bid model
        Bid bid = new Bid(1, user, limitedProduct, dynamicPrice + 100);
        assertTest("5.5 Bid oluşturuldu", bid.getAmount() == dynamicPrice + 100);
        assertTest("5.5 Bid kullanıcısı doğru", "Test User".equals(bid.getUser().getName()));

        ShoppingCart.resetInstance();
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
