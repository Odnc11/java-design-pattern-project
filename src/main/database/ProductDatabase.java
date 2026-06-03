package main.database;

import main.models.Product;
import main.models.Seller;
import main.patterns.decorator.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory product database for the e-commerce system.
 * 
 * Pre-loaded with 26 products across 5 sellers.
 * Manages seller-applied decorators (global discounts visible to all buyers).
 * 
 * Decorator Architecture:
 *   - Each product has a "decorated view" stored in decoratedProducts map
 *   - Seller adds/removes decorators → decoratedProducts updated
 *   - Buyers see the decorated price when browsing
 *   - Buyer's own decorators (coupons) are stored in CartItem, not here
 */
public class ProductDatabase {
    private static final List<Product> products = new ArrayList<>();
    /** Product ID → current decorated view (with seller's global decorators) */
    private static final Map<Integer, ProductComponent> decoratedProducts = new ConcurrentHashMap<>();
    /** Product ID → list of decorator descriptions for display */
    private static final Map<Integer, List<String>> decoratorDescriptions = new ConcurrentHashMap<>();

    static {
        initializeMockData();
    }

    private static void initializeMockData() {
        List<Seller> sellers = UserDatabase.getAllSellers();

        // --- Seller 1: TeknoMarket (8 products) ---
        Seller s1 = sellers.get(0);
        addProduct(s1, new Product(1, "Laptop", "Yüksek performanslı dizüstü bilgisayar", 10000.00, 10, "Elektronik", s1.getId()));
        addProduct(s1, new Product(2, "Desktop PC", "Masaüstü bilgisayar", 8000.00, 7, "Elektronik", s1.getId()));
        addProduct(s1, new Product(3, "Monitor", "27 inç 4K monitor", 5000.00, 12, "Elektronik", s1.getId()));
        addProduct(s1, new Product(4, "SSD", "1TB NVMe SSD", 1500.00, 25, "Elektronik", s1.getId()));
        addProduct(s1, new Product(5, "RAM", "16GB DDR5 RAM", 800.00, 30, "Elektronik", s1.getId()));
        addProduct(s1, new Product(6, "GPU", "RTX 4070 ekran kartı", 12000.00, 5, "Elektronik", s1.getId()));
        addProduct(s1, new Product(7, "Motherboard", "Gaming anakart", 4000.00, 8, "Elektronik", s1.getId()));
        addProduct(s1, new Product(8, "PSU", "750W güç kaynağı", 1200.00, 15, "Elektronik", s1.getId()));

        // --- Category: AksesuarDünyası (5 products) ---
        addProduct(s1, new Product(9, "Mouse", "Kablosuz gaming mouse", 250.00, 40, "Aksesuar", s1.getId()));
        addProduct(s1, new Product(10, "Keyboard", "Mekanik RGB klavye", 600.00, 20, "Aksesuar", s1.getId()));
        addProduct(s1, new Product(11, "Mousepad", "XXL gaming mousepad", 100.00, 50, "Aksesuar", s1.getId()));
        addProduct(s1, new Product(12, "USB Cable", "USB-C hızlı şarj kablosu", 50.00, 100, "Aksesuar", s1.getId()));
        addProduct(s1, new Product(13, "Webcam Kapağı", "Gizlilik webcam kapağı", 30.00, 80, "Aksesuar", s1.getId()));

        // --- Category: ÇevreBilişim (5 products) ---
        addProduct(s1, new Product(14, "Printer", "Renkli lazer yazıcı", 3000.00, 6, "Çevre Birimi", s1.getId()));
        addProduct(s1, new Product(15, "Scanner", "Flatbed tarayıcı", 2000.00, 10, "Çevre Birimi", s1.getId()));
        addProduct(s1, new Product(16, "Webcam", "1080p HD webcam", 1500.00, 18, "Çevre Birimi", s1.getId()));
        addProduct(s1, new Product(17, "Drawing Tablet", "Grafik çizim tableti", 4000.00, 4, "Çevre Birimi", s1.getId()));
        addProduct(s1, new Product(18, "External HDD", "2TB harici disk", 1000.00, 15, "Çevre Birimi", s1.getId()));

        // --- Category: NetWorkShop (4 products) ---
        addProduct(s1, new Product(19, "Router", "WiFi 6 router", 1500.00, 12, "Ağ", s1.getId()));
        addProduct(s1, new Product(20, "Switch", "8 portlu gigabit switch", 800.00, 20, "Ağ", s1.getId()));
        addProduct(s1, new Product(21, "Modem", "VDSL modem", 600.00, 15, "Ağ", s1.getId()));
        addProduct(s1, new Product(22, "Ethernet Kablosu", "Cat6 5m ethernet", 100.00, 60, "Ağ", s1.getId()));

        // --- Category: SesSistem (4 products) ---
        addProduct(s1, new Product(23, "Headphones", "Kablosuz bluetooth kulaklık", 900.00, 22, "Ses/Video", s1.getId()));
        addProduct(s1, new Product(24, "Speaker", "2.1 masaüstü hoparlör", 1200.00, 10, "Ses/Video", s1.getId()));
        addProduct(s1, new Product(25, "Microphone", "USB kondenser mikrofon", 2000.00, 8, "Ses/Video", s1.getId()));
        addProduct(s1, new Product(26, "Soundcard", "Harici ses kartı", 1500.00, 6, "Ses/Video", s1.getId()));

        // --- Apply some initial decorators (sample discounts) ---
        applyInitialDecorators();
    }

    /**
     * Apply sample decorators to some products for demo purposes.
     */
    private static void applyInitialDecorators() {
        // Laptop: %15 indirim + Ücretsiz kargo
        addSellerDecorator(1, new PercentageDiscountDecorator(getDecoratedProduct(1), 15));
        addSellerDecorator(1, new FreeShippingDecorator(getDecoratedProduct(1)));

        // Mouse: %10 indirim
        addSellerDecorator(9, new PercentageDiscountDecorator(getDecoratedProduct(9), 10));

        // GPU: Flash Sale %10
        addSellerDecorator(6, new FlashSaleDecorator(getDecoratedProduct(6)));

        // Drawing Tablet: 500₺ sabit indirim
        addSellerDecorator(17, new FixedAmountDiscountDecorator(getDecoratedProduct(17), 500));
    }

    // --- Product Management ---

    private static void addProduct(Seller seller, Product product) {
        products.add(product);
        seller.addProduct(product);
        // Initialize undecorated view, wrapped with dynamic pricing
        ProductComponent base = new ConcreteProduct(product);
        ProductComponent dynamicBase = new DynamicPricingDecorator(base, product.getName());
        decoratedProducts.put(product.getId(), dynamicBase);
        decoratorDescriptions.put(product.getId(), new ArrayList<>());
    }

    /**
     * Add a new product to the database and assign to seller.
     */
    public static synchronized void createProduct(Seller seller, Product product) {
        addProduct(seller, product);
    }

    // --- Decorator Management (Seller operations) ---

    /**
     * Apply a seller decorator to a product (global, all buyers see it).
     * The new decorator wraps the current decorated view.
     */
    public static synchronized void addSellerDecorator(int productId, ProductComponent decorator) {
        decoratedProducts.put(productId, decorator);
        // Track description
        String desc = decorator.getDescription();
        Product base = findById(productId);
        if (base != null) {
            String baseDesc = base.getDescription();
            if (desc.length() > baseDesc.length()) {
                String added = desc.substring(baseDesc.length()).trim();
                List<String> descs = decoratorDescriptions.get(productId);
                if (descs != null) {
                    descs.add(added);
                }
            }
        }
    }

    /**
     * Remove all seller decorators from a product (reset to base + dynamic pricing).
     */
    public static synchronized void clearSellerDecorators(int productId) {
        Product base = findById(productId);
        if (base != null) {
            ProductComponent concrete = new ConcreteProduct(base);
            ProductComponent dynamicBase = new DynamicPricingDecorator(concrete, base.getName());
            decoratedProducts.put(productId, dynamicBase);
            decoratorDescriptions.put(productId, new ArrayList<>());
        }
    }

    /**
     * Get the current decorated view of a product (with seller's decorators).
     */
    public static ProductComponent getDecoratedProduct(int productId) {
        ProductComponent dec = decoratedProducts.get(productId);
        if (dec != null) return dec;
        Product base = findById(productId);
        return base != null ? new ConcreteProduct(base) : null;
    }

    /**
     * Get the current price after seller's decorators.
     */
    public static double getDecoratedPrice(int productId) {
        ProductComponent dec = getDecoratedProduct(productId);
        return dec != null ? dec.getPrice() : 0;
    }

    /**
     * Check if a product has free shipping (from seller decorator).
     */
    public static boolean hasFreeShipping(int productId) {
        ProductComponent dec = getDecoratedProduct(productId);
        return dec != null && dec.hasFreeShipping();
    }

    /**
     * Get the list of decorator descriptions for a product.
     */
    public static List<String> getDecoratorDescriptions(int productId) {
        List<String> descs = decoratorDescriptions.get(productId);
        return descs != null ? Collections.unmodifiableList(descs) : Collections.emptyList();
    }

    // --- Queries ---

    /**
     * Find a product by ID.
     */
    public static Product findById(int id) {
        for (Product p : products) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    /**
     * Get all products.
     */
    public static List<Product> getAllProducts() {
        return Collections.unmodifiableList(products);
    }

    /**
     * Get products by seller ID.
     */
    public static List<Product> getProductsBySeller(int sellerId) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getSellerId() == sellerId) result.add(p);
        }
        return result;
    }

    /**
     * Get products by category.
     */
    public static List<Product> getProductsByCategory(String category) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategory().equals(category)) result.add(p);
        }
        return result;
    }

    /**
     * Get all unique categories.
     */
    public static List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        for (Product p : products) {
            if (!categories.contains(p.getCategory())) {
                categories.add(p.getCategory());
            }
        }
        return categories;
    }

    /**
     * Get the next available product ID.
     */
    public static int getNextProductId() {
        int maxId = 0;
        for (Product p : products) {
            if (p.getId() > maxId) maxId = p.getId();
        }
        return maxId + 1;
    }
}
